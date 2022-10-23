package jogUtil.data;

import jogUtil.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeRegistry
{
	private static final HashMap<String, RegisteredType> nameMap = new HashMap<>();
	private static final HashMap<Class<?>, RegisteredType> classMap = new HashMap<>();
	private static final RequiredMethod[] requiredMethods = requiredMethods();
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
				throw new RuntimeException("Could not register default value: "
										   + result.description().encode(EncodingType.PLAIN));
			registrationResults[index] = result;
		}
		
		return new ReturnResult<>(registrationResults);
	}
	
	public static Result register(String name, Class<? extends Value<?, ?>> typeClass)
	{
		if (classMap.containsKey(typeClass))
			return new Result("Could not register type " + typeClass.getName() + " as '" +
							  name + "': Already registered.");
		if (nameMap.containsKey(name))
			return new Result("Could not register type " + typeClass.getName() + " as '" +
							  name + "': Another type is already registered under that name.");
		
		ReturnResult<RegisteredType> creationResult = RegisteredType.create(name, typeClass);
		if (!creationResult.success())
			return new Result("Could not register type " + typeClass.getName() + " as '" +
							  name + "': Not a valid value type implementation: " +
							  creationResult.description());
		classMap.put(typeClass, creationResult.value());
		nameMap.put(name, creationResult.value());
		
		Result validationResult = validate(creationResult.value());
		if (!validationResult.success())
		{
			nameMap.remove(name);
			classMap.remove(typeClass);
			return new Result("Could not register type " + typeClass.getName() + " as '" +
							  name + "': Validation Failure: " + validationResult.description());
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
			return new Result("Exception occurred getting validation values, "
							  + Result.describeExceptionFull(e));
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
				return new Result(RichStringBuilder.start("Validation value #" + index
														  + " failed: ")
												   .append(validationResult.description()).build());
		}
		
		return new Result();
	}
	
	public static final class RegisteredType
	{
		Class<? extends Value<?, ?>> typeClass;
		String name;
		Type valueType;
		Type consumptionResult;
		
		Method byteConsumer;
		Method characterConsumer;
		Method validationValues;
		
		static ReturnResult<RegisteredType> create(String name, Class<? extends Value<?, ?>> typeClass)
		{
			Type type = stepUpToImplementation(typeClass.getGenericSuperclass(), Value.class);
			if (type == null)
				return new ReturnResult<>("Does not implement " + Value.class.getName());
			Type[] parameters = ((ParameterizedType)type).getActualTypeArguments();
			
			RegisteredType registration = new RegisteredType();
			registration.typeClass = typeClass;
			registration.name = name;
			registration.valueType = parameters[0];
			registration.consumptionResult = parameters[1];
			
			//once we know that this is indeed a Value class, and have the value type, we now need to
			//check for all of our expected methods and ensure that they are all defined properly
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
							//make sure the method has the correct modifiers and no arguments
							if (value.getParameterCount() != 0)
								return new ReturnResult<>(method.annotation.getSimpleName()
														  + " method must not require arguments.");
							int modifiers = value.getModifiers();
							if (!Modifier.isPublic(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName()
														  + " method must be public.");
							if (Modifier.isAbstract(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName()
														  + " method must not be abstract.");
							if (!Modifier.isStatic(modifiers))
								return new ReturnResult<>(method.annotation.getSimpleName()
														  + " method must be static.");
							
							//now we check to make sure there is only one method for each annotation
							if (method.field.get(registration) != null)
								return new ReturnResult<>("Only one "
														  + method.annotation.getSimpleName()
														  + " method can be provided.");
							
							//validate that this method has the correct return type, given the annotation
							//and value type
							Result result = method.returnValidator
									.validate(value.getGenericReturnType(),
											  registration.valueType,
											  registration.consumptionResult);
							if (!result.success())
								return new ReturnResult<>(RichStringBuilder.start(method.annotation
													.getSimpleName()
													+ " method does not have the correct return type: ")
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
						return new ReturnResult<>(method.annotation.getSimpleName()
												  + " method was not provided.");
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
		
		public Value<?, ?> newInstance()
		{
			try
			{
				return typeClass.getConstructor().newInstance();
			}
			catch (Exception e)
			{
				return null;
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
			try
			{
				return (Consumer<Value<?, ?>, Character>) characterConsumer.invoke(null);
			}
			catch (IllegalAccessException | InvocationTargetException | ClassCastException e)
			{
				return null;
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
	
	private static RequiredMethod[] requiredMethods()
	{
		return new RequiredMethod[] {
		new RequiredMethod(ValidationValues.class, "validationValues",
		(returnType, valueType, consumptionType) ->
		{
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
			return new Result("Must return " + Value.class.getName() + "<?, "
							  + consumptionType.getTypeName() + ">[]");
		}),
		
		new RequiredMethod(ByteConsumer.class, "byteConsumer",
		(returnType, valueType, consumptionType) ->
		{
			returnType = stepUpToImplementation(returnType, Consumer.class);
			if (returnType != null)
			{
				Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
				Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
				if (resultType != null)
				{
					Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
					if (resultParameters[0] instanceof WildcardType
						&& resultParameters[1].equals(consumptionType)
						&& consumerParameters[1].equals(Byte.class))
					{
						return new Result();
					}
				}
			}
			return new Result("Must return " + Consumer.class.getName() + "<"
							  + Value.class.getName() + "<?, " + consumptionType.getTypeName() + ">, "
							  + Byte.class.getName() + ">");
		}),
		
		new RequiredMethod(CharacterConsumer.class, "characterConsumer",
		(returnType, valueType, consumptionType) ->
		{
			returnType = stepUpToImplementation(returnType, Consumer.class);
			if (returnType != null)
			{
				Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
				Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
				if (resultType != null)
				{
					Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
					if (resultParameters[0] instanceof WildcardType
						&& resultParameters[1].equals(consumptionType)
						&& consumerParameters[1].equals(Character.class))
					{
						return new Result();
					}
				}
			}
			return new Result("Must return " + Consumer.class.getName() + "<"
							  + Value.class.getName() + "<?, " + consumptionType.getTypeName() + ">, "
							  + Character.class.getName() + ">");
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
		if (type instanceof ParameterizedType
			&& ((ParameterizedType) type).getRawType().equals(inheritedType))
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
	
	/**
	 * Ensures that a required method's return type is what it should be.
	 */
	private static interface ReturnValidator
	{
		Result validate(Type returnType, Type valueType, Type consumptionType);
	}
	
	/**
	 * Represents a static method that value types must implement
	 */
	private static class RequiredMethod
	{
		Class<? extends Annotation> annotation;
		Field field;
		ReturnValidator returnValidator;
		
		private RequiredMethod(Class<? extends Annotation> annotation, String fieldName,
							   ReturnValidator returnValidator)
		{
			this.annotation = annotation;
			this.returnValidator = returnValidator;
			
			try
			{
				field = RegisteredType.class.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException | SecurityException e)
			{//these exceptions should in theory never occur but the IDE complains if we don't handle them
				System.err.println("Data TypeRegistry Could not initialize required method "
								   + annotation.getSimpleName() + ": Could not find "
								   + fieldName + " field.");
			}
		}
	}
}
