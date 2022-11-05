package jogUtil.data;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeRegistry
{
	private static final HashMap<String, RegisteredType> nameMap = new HashMap<>();
	private static final HashMap<Class<?>, RegisteredType> classMap = new HashMap<>();
	private static final RequiredMethod[] plainArgumentMethods = plainArgumentMethods();
	private static final RequiredMethod[] compoundArgumentMethods = compoundArgumentMethods();
	private static final ReturnResult<Result[]> defaultValueStatus = registerDefaults();
	
	private static ReturnResult<Result[]> registerDefaults()
	{
		Object[][] typeClasses = {
				{"Byte", ByteValue.class},
				{"Boolean", BooleanValue.class},
				{"Short", ShortValue.class},
				{"Character", CharacterValue.class},
				{"Integer", IntegerValue.class},
				{"Float", FloatValue.class},
				{"Long", LongValue.class},
				{"Double", DoubleValue.class},
				{"UUID", UUIDValue.class},
				{"String", StringValue.class},
				
				{"List", ListValue.class},
				{"Data", DataValue.class}
		};
		
		Result[] registrationResults = new Result[typeClasses.length];
		for (int index = 0; index < typeClasses.length; index++)
		{
			Class<? extends Value<?, ?>> typeClass = (Class<? extends Value<?, ?>>)typeClasses[index][1];
			String name = (String)typeClasses[index][0];
			Result result = register(name, typeClass);
			if (!result.success())
				throw new RuntimeException("Could not register default value: " + result.description());
			registrationResults[index] = result;
		}
		
		return new ReturnResult<>(registrationResults);
	}
	
	public static Result register(String name, Class<? extends Value<?, ?>> typeClass)
	{
		//make sure nothing has already been registered with this name or class
		if (classMap.containsKey(typeClass))
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Already registered.");
		if (nameMap.containsKey(name))
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Another type is already registered under that name.");
		
		//verify that the implementation is valid, and create the registration
		ReturnResult<RegisteredType> creationResult = RegisteredType.create(name, typeClass);
		if (!creationResult.success())
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Not a valid value type implementation: " + creationResult.description());
		classMap.put(typeClass, creationResult.value());
		nameMap.put(name, creationResult.value());
		
		//validate that the implementation behaves as expected
		Result validationResult = validate(creationResult.value());
		if (!validationResult.success())
		{
			nameMap.remove(name);
			classMap.remove(typeClass);
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Validation Failure: " + validationResult.description());
		}
		else
			return new Result(typeClass.getName() + " registered as " + name, true);
	}
	
	private static Result validate(RegisteredType type)
	{
		//retrieve the array of validation values and make sure it has at least one entry
		Value<?, ?>[] values;
		try
		{
			values = (Value<?, ?>[])type.validationValues.invoke(null);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred getting validation values, " + Result.describeThrowableFull(e));
		}
		if (values == null)
			return new Result("Validation values can not be null.");
		if (values.length == 0)
			return new Result("There must be at least one validation value.");
		
		//check validation values
		Result validationResult;
		for (int index = 0; index < values.length; index++)
		{
			validationResult = values[index].validate();
			if (!validationResult.success())
				return new Result(RichStringBuilder.start("Validation value #" + index + " failed: ").append(validationResult.description()).build());
		}
		
		return new Result();
	}
	
	public static final class RegisteredType
	{
		Class<? extends Value<?, ?>> typeClass;
		String name;
		Type valueType;
		Type consumptionResult;
		
		boolean compoundArgument;
		
		Method byteConsumer;
		Method characterConsumer;
		Method validationValues;
		Method argumentList;
		Method buildValue;
		
		static ReturnResult<RegisteredType> create(String name, Class<? extends Value<?, ?>> typeClass)
		{
			//make sure that this is at the very least a subclass of Value
			Type plainType = stepUpToImplementation(typeClass.getGenericSuperclass(), Value.class);
			Type compoundType = stepUpToImplementation(typeClass.getGenericSuperclass(), CompoundArgumentValue.class);
			if (plainType == null && compoundType == null)
				return new ReturnResult<>("Does not implement " + Value.class.getName() + " or " + CompoundArgumentValue.class.getName());
			
			RequiredMethod[] requiredMethods;
			Type[] parameters;
			if (compoundType == null)
			{
				requiredMethods = TypeRegistry.plainArgumentMethods;
				parameters = ((ParameterizedType)plainType).getActualTypeArguments();
			}
			else
			{
				requiredMethods = TypeRegistry.compoundArgumentMethods;
				parameters = ((ParameterizedType)compoundType).getActualTypeArguments();
			}
			
			RegisteredType registration = new RegisteredType();
			registration.typeClass = typeClass;
			registration.name = name;
			registration.valueType = parameters[0];
			registration.consumptionResult = parameters[1];
			registration.compoundArgument = plainType == null;
			
			return checkForRequiredMethods(typeClass, requiredMethods, registration);
		}
		
		private static ReturnResult<RegisteredType> checkForRequiredMethods(Class<? extends Value<?, ?>> typeClass, RequiredMethod[] requiredMethods, RegisteredType registration)
		{
			try
			{
				Method[] methods = typeClass.getMethods();
				for (Method value : methods)
				{
					for (RequiredMethod method : requiredMethods)
					{
						//iterating through all the methods in the class, as well as all the annotations
						//we are looking for we single out the methods that have the proper annotations
						if (value.isAnnotationPresent(method.annotation))
						{
							//make sure the method has the correct modifiers
							int modifiers = value.getModifiers();
							if (!Modifier.isPublic(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName() + " method must be public.");
							if (Modifier.isAbstract(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName() + " method must not be abstract.");
							if (!Modifier.isStatic(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName() + " method must be static.");
							
							//now we check to make sure there is only one method for each annotation
							if (method.field.get(registration) != null)
								return new ReturnResult<>("Only one " + method.annotation.getSimpleName() + " method can be provided.");
							
							//validate that this method has the correct return type and arguments, given the annotation
							//and value type
							Result result = method.methodValidator.validate(value.getGenericReturnType(), registration.valueType, registration.consumptionResult, value.getGenericParameterTypes());
							if (!result.success())
								return new ReturnResult<>(RichStringBuilder.start(method.annotation.getSimpleName() + " method does not have the correct return type: ")
																		   .append(result.description()).build());
							//if the method is properly defined, then we can set the field
							method.field.set(registration, value);
						}
					}
				}
				
				//lastly, we check to make sure we found a method for each annotation, and if everything
				//is in order then we can return a valid result
				for (RequiredMethod method : requiredMethods)
				{
					if (method.field.get(registration) == null)
						return new ReturnResult<>(method.annotation.getSimpleName() + " method was not provided.");
				}
				return new ReturnResult<>(registration);
			}
			catch (IllegalAccessException e)
			{
				return new ReturnResult<>("Fields could not be accessed.");
			}//these exceptions should in theory never occur but the IDE complains if we don't handle them
			catch (IllegalArgumentException e)
			{
				return new ReturnResult<>("Fields could not be assigned.");
			}
		}
		
		public String name()
		{
			return name;
		}
		
		public Class<? extends Value<?, ?>> typeClass()
		{
			return typeClass;
		}
		
		public Consumer<Value<?, ?>, Byte> byteConsumer()
		{
			try
			{
				return (Consumer<Value<?, ?>, Byte>) byteConsumer.invoke(null);
			}
			catch (IllegalAccessException | InvocationTargetException | ClassCastException e)
			{
				return null;
			}
		}
		
		public Consumer<Value<?, ?>, Character> characterConsumer()
		{
			if (compoundArgument)
			{
				return CompoundArgumentValue.compoundCharacterConsumer(this);
			}
			else
			{
				try
				{
					return (Consumer<Value<?, ?>, Character>) characterConsumer.invoke(null);
				}
				catch (IllegalAccessException | InvocationTargetException | ClassCastException e)
				{
					return null;
				}
			}
		}
		
		public Value<?, ?>[] validationValues()
		{
			try
			{
				return (Value<?, ?>[]) validationValues.invoke(null);
			}
			catch (IllegalAccessException | InvocationTargetException | ClassCastException e)
			{
				return null;
			}
		}
		
		AdaptiveArgumentList argumentList(Object[] initData)
		{
			if (compoundArgument)
			{
				try
				{
					return (AdaptiveArgumentList) argumentList.invoke(null, (Object)initData);
				}
				catch (IllegalAccessException | InvocationTargetException | ClassCastException e)
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		
		Object buildValue(AdaptiveInterpretation result, Executor executor)
		{
			if (compoundArgument)
			{
				try
				{
					return buildValue.invoke(null, result, executor);
				}
				catch (IllegalAccessException | ClassCastException e)
				{
					e.printStackTrace();
					return null;
				}
				catch (InvocationTargetException e)
				{
					throw new RuntimeException("Exception occurred while building value: " + Result.describeThrowableFull(e.getTargetException()));
				}
			}
			else
			{
				return null;
			}
		}
	}
	
	/**
	 * Gets a type's registration
	 * <p>
	 *     If the given class isn't a registered type, null is returned.
	 * </p>
	 * @param typeClass
	 * @return
	 */
	public static RegisteredType get(Class<?> typeClass)
	{
		return classMap.get(typeClass);
	}
	
	/**
	 * Gets a registered type
	 * <p>
	 *     If no type is registered with the given name, null is returned.
	 * </p>
	 * @param name
	 * @return
	 */
	public static RegisteredType get(String name)
	{
		return nameMap.get(name);
	}
	
	/**
	 * Returns an array of the registration results from the default value types
	 * @return
	 */
	public static Result[] defaultValueStatus()
	{
		return defaultValueStatus.value(true);
	}
	
	private static class ValidationValuesMethod extends RequiredMethod
	{
		private ValidationValuesMethod()
		{
			super(ValidationValues.class, "validationValues",
			(returnType, valueType, consumptionType, arguments) ->
			{
				if (arguments.length > 0)
					return new Result("Must require no arguments.");
				
				if (returnType instanceof GenericArrayType)
				{
					Type type = ((GenericArrayType) returnType).getGenericComponentType();
					type = stepUpToImplementation(type, Value.class);
					if (type != null)
					{
						Type[] parameters = ((ParameterizedType)type).getActualTypeArguments();
						if (parameters[1].equals(consumptionType))
							return new Result();
					}
				}
				return new Result("Must return " + Value.class.getName() + "<?, " + consumptionType.getTypeName() + ">[]");
			});
		}
	}
	
	private static class ByteConsumerMethod extends RequiredMethod
	{
		private ByteConsumerMethod()
		{
			super(ByteConsumer.class, "byteConsumer",
			(returnType, valueType, consumptionType, arguments) ->
			{
				if (arguments.length > 0)
					return new Result("Must require no arguments.");
				
				returnType = stepUpToImplementation(returnType, Consumer.class);
				if (returnType != null)
				{
					Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
					Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
					if (resultType != null)
					{
						Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
						if (resultParameters[0] instanceof WildcardType && resultParameters[1].equals(consumptionType) && consumerParameters[1].equals(Byte.class))
							return new Result();
					}
				}
				return new Result("Must return " + Consumer.class.getName() + "<" + Value.class.getName() + "<?, " + consumptionType.getTypeName() + ">, " + Byte.class.getName() + ">");
			});
		}
	}
	
	private static RequiredMethod[] plainArgumentMethods()
	{
		return new RequiredMethod[] {new ValidationValuesMethod(), new ByteConsumerMethod(),
		
		new RequiredMethod(CharacterConsumer.class, "characterConsumer",
		(returnType, valueType, consumptionType, arguments) ->
		{
			if (arguments.length > 0)
				return new Result("Must require no arguments.");
			
			returnType = stepUpToImplementation(returnType, Consumer.class);
			if (returnType != null)
			{
				Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
				Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
				if (resultType != null)
				{
					Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
					if (resultParameters[0] instanceof WildcardType && resultParameters[1].equals(consumptionType) && consumerParameters[1].equals(Character.class))
					{
						return new Result();
					}
				}
			}
			return new Result("Must return " + Consumer.class.getName() + "<" + Value.class.getName() + "<?, " + consumptionType.getTypeName() + ">, " + Character.class.getName() + ">");
		})};
	}
	
	private static RequiredMethod[] compoundArgumentMethods()
	{
		return new RequiredMethod[] {new ValidationValuesMethod(), new ByteConsumerMethod(),
		
		new RequiredMethod(ArgumentList.class, "argumentList",
		(returnType, valueType, consumptionType, arguments) ->
		{
			if (!returnType.equals(AdaptiveArgumentList.class))
				return new Result("Must return " + AdaptiveArgumentList.class.getName());
			if (arguments.length != 1 || !arguments[0].equals(Object[].class))
				return new Result("Must accept an Object array as it's only argument.");
			
			return new Result();
		}),
		
		new RequiredMethod(BuildValue.class, "buildValue",
		(returnType, valueType, consumptionType, arguments) ->
		{
			if (arguments.length == 2 && arguments[0].equals(AdaptiveInterpretation.class))
			{
				Type executor = stepUpToImplementation(arguments[1], Executor.class);
				if (executor != null)
				{
					if (!returnType.equals(consumptionType))
						return new Result("Must return " + consumptionType.getTypeName());
					
					return new Result();
				}
			}
			
			return new Result("Arguments must be (" + AdaptiveInterpretation.class.getName() + ", " + Executor.class.getName() + ")");
		})};
	}
	
	/**
	 * Steps up the inheritance until we find the type that directly implements the given type
	 * <p>
	 *     Returns null if the given type is never implemented
	 * </p>
	 * @param type
	 * @return
	 */
	private static Type stepUpToImplementation(Type type, Type inheritedType)
	{
		if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType().equals(inheritedType))
			return type;
		else if (type.equals(inheritedType))
			return type;
		else
		{
			Class<?> superClass = type.getClass().getSuperclass();
			if (superClass == null || superClass.getGenericSuperclass() == null)
				return null;
			else
				return stepUpToImplementation(superClass.getGenericSuperclass(), inheritedType);
		}
	}
	
	//these annotations are used to identify the required methods in a value types implementation
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ValidationValues
	{
	
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ByteConsumer
	{
	
	}
	
	/**
	 * <p>
	 *     Important to keep in mind that when part of a Data object, tabs will be inserted after each
	 *     new line in this Value's string representation.<br>
	 *     The Character Consumer needs to be able to account for these.
	 * </p>
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface CharacterConsumer
	{
	
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ArgumentList
	{
	
	}
	
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface BuildValue
	{
	
	}
	
	/**
	 * Ensures that a required method's return type is what it should be.
	 */
	private static interface MethodValidator
	{
		Result validate(Type returnType, Type valueType, Type consumptionType, Type[] arguments);
	}
	
	/**
	 * Represents a static method that value types must implement
	 */
	private static class RequiredMethod
	{
		final Class<? extends Annotation> annotation;
		Field field;
		final MethodValidator methodValidator;
		
		private RequiredMethod(Class<? extends Annotation> annotation, String fieldName,
							   MethodValidator methodValidator)
		{
			this.annotation = annotation;
			this.methodValidator = methodValidator;
			
			try
			{
				field = RegisteredType.class.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException | SecurityException e)
			{//these exceptions should in theory never occur but the IDE complains if we don't handle them
				System.err.println("Data TypeRegistry Could not initialize required method " + annotation.getSimpleName() + ": Could not find " + fieldName + " field.");
			}
		}
	}
}