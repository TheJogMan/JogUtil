package jogUtil.data;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public class TypeRegistry
{
	private static final HashMap<String, RegisteredType<?, ?>> nameMap = new HashMap<>();
	private static final HashMap<Class<?>, RegisteredType<?, ?>> classMap = new HashMap<>();
	private static final Result[] defaultValueStatus = RegistrationQueue.start()
			.add("Byte", ByteValue.class)
			.add("Boolean", BooleanValue.class)
			.add("Short", ShortValue.class)
			.add("Character", CharacterValue.class)
			.add("Integer", IntegerValue.class)
			.add("Float", FloatValue.class)
			.add("Long", LongValue.class)
			.add("Double", DoubleValue.class)
			.add("UUID", UUIDValue.class)
			.add("String", StringValue.class)
			
			.addListValue("List", ListValue.class)
			.add("Data", DataValue.class)
			.process();
	
	/**
	 * Gets a type's registration
	 * <p>
	 *     If the given class isn't a registered type, null is returned.
	 * </p>
	 * @param typeClass
	 * @return
	 */
	public static <ValueType, ConsumptionResult> RegisteredType<ValueType, ConsumptionResult> get(Class<? extends Value<ValueType, ConsumptionResult>> typeClass)
	{
		return (RegisteredType<ValueType, ConsumptionResult>)classMap.get(typeClass);
	}
	
	/**
	 * Gets a registered type
	 * <p>
	 *     If no type is registered with the given name, null is returned.
	 * </p>
	 * @param name
	 * @return
	 */
	public static RegisteredType<?, ?> get(String name)
	{
		return nameMap.get(name);
	}
	
	/**
	 * Returns an array of the registration results from the default value types
	 * @return
	 */
	public static Result[] defaultValueStatus()
	{
		return defaultValueStatus;
	}
	
	/**
	 * <p>
	 *     Important to keep in mind that when part of a Data object, tabs will be inserted after each
	 *     new line in this Value's string representation.<br>
	 *     The Character Consumer needs to be able to account for these.
	 * </p>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface CharacterConsumer
	{
	
	}
	
	public static final class RegisteredPlainType<ValueType, ConsumptionResult> extends RegisteredType<ValueType, ConsumptionResult>
	{
		public final RequiredMethod characterConsumer = new RequiredMethod(CharacterConsumer.class,
		(returnType, arguments, valueType, consumptionResult) ->
		{
			if (arguments.length == 1)
			{
				if (!(arguments[0] instanceof GenericArrayType) || !((GenericArrayType)arguments[0]).getGenericComponentType().equals(Object.class))
					return new Result("Argument must be an array of Objects");
			}
			else if (arguments.length > 1)
				return new Result("Must either have no arguments, or accept an array of Objects");
			
			returnType = stepUpToImplementation(returnType, Consumer.class);
			if (returnType != null)
			{
				Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
				Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
				if (resultType != null)
				{
					Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
					if (resultParameters[0] instanceof WildcardType && resultParameters[1].equals(consumptionResult) && consumerParameters[1].equals(Character.class))
					{
						return new Result();
					}
				}
			}
			return new Result("Must return " + Consumer.class.getName() + "<" + Value.class.getName() + "<?, " + consumptionResult.getTypeName() + ">, " + Character.class.getName() + ">");
		});
		
		@Override
		Result captureMethods()
		{
			return characterConsumer.capture();
		}
		
		@Override
		public Consumer<Value<?, ConsumptionResult>, Character> characterConsumer(Object[] data)
		{
			if (characterConsumer.method.getParameterCount() == 1)
			{
				if (data.length == 0)
					data = new Object[] {new Object[0]};
			}
			return (Consumer<Value<?, ConsumptionResult>, Character>)characterConsumer.invoke(data);
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ArgumentList
	{
	
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface BuildValue
	{
	
	}
	
	public static final class RegisteredCompoundType<ValueType, ConsumptionResult> extends RegisteredType<ValueType, ConsumptionResult>
	{
		public final RequiredMethod argumentList = new RequiredMethod(ArgumentList.class,
		(returnType, arguments, valueType, consumptionResult) ->
		{
			if (!returnType.equals(AdaptiveArgumentList.class))
				return new Result("Must return " + AdaptiveArgumentList.class.getName());
			if (arguments.length != 1 || !arguments[0].equals(Object[].class))
				return new Result("Must accept an Object array as it's only argument.");
			
			return new Result();
		});
		
		public final RequiredMethod buildValue = new RequiredMethod(BuildValue.class,
		(returnType, arguments, valueType, consumptionResult) ->
		{
			if (arguments.length == 2 && arguments[0].equals(AdaptiveInterpretation.class))
			{
				Type executor = stepUpToImplementation(arguments[1], Executor.class);
				if (executor != null)
				{
					if (!returnType.equals(consumptionResult))
						return new Result("Must return " + consumptionResult.getTypeName());
					
					return new Result();
				}
			}
			
			return new Result("Arguments must be (" + AdaptiveInterpretation.class.getName() + ", " + Executor.class.getName() + ")");
		});
		
		@Override
		Result captureMethods()
		{
			Result result = argumentList.capture();
			if (!result.success())
				return result;
			return buildValue.capture();
		}
		
		@Override
		public Consumer<Value<?, ConsumptionResult>, Character> characterConsumer(Object[] data)
		{
			return CompoundArgumentValue.compoundCharacterConsumer(this, data);
		}
		
		public AdaptiveArgumentList argumentList(Object[] data)
		{
			return (AdaptiveArgumentList)argumentList.invoke(new Object[] {data});
		}
		
		public ConsumptionResult buildValue(AdaptiveInterpretation interpretation, Executor executor)
		{
			return (ConsumptionResult)buildValue.invoke(new Object[] {interpretation, executor});
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ValidationValues
	{
	
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface ByteConsumer
	{
	
	}
	
	public static abstract class RegisteredType<ValueType, ConsumptionResult>
	{
		Class<? extends Value<ValueType, ConsumptionResult>> typeClass;
		String name;
		Type valueType;
		Type consumptionResult;
		
		public final RequiredMethod byteConsumer = new RequiredMethod(ByteConsumer.class,
		(returnType, arguments, valueType, consumptionResult) ->
		{
			if (arguments.length == 1)
			{
				if (!(arguments[0] instanceof GenericArrayType) || !((GenericArrayType)arguments[0]).getGenericComponentType().equals(Object.class))
					return new Result("Argument must be an array of Objects");
			}
			else if (arguments.length > 1)
				return new Result("Must either have no arguments, or accept an array of Objects");
			
			returnType = stepUpToImplementation(returnType, Consumer.class);
			if (returnType != null)
			{
				Type[] consumerParameters = ((ParameterizedType)returnType).getActualTypeArguments();
				Type resultType = stepUpToImplementation(consumerParameters[0], Value.class);
				if (resultType != null)
				{
					Type[] resultParameters = ((ParameterizedType)resultType).getActualTypeArguments();
					if (resultParameters[0] instanceof WildcardType && resultParameters[1].equals(consumptionResult) && consumerParameters[1].equals(Byte.class))
						return new Result();
				}
			}
			return new Result("Must return " + Consumer.class.getName() + "<" + Value.class.getName() + "<?, " + consumptionResult.getTypeName() + ">, " + Byte.class.getName() + ">");
		});
		
		public final RequiredMethod validationValues = new RequiredMethod(ValidationValues.class,
		(returnType, arguments, valueType, consumptionResult) ->
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
					if (parameters[1].equals(consumptionResult))
						return new Result();
				}
			}
			return new Result("Must return " + Value.class.getName() + "<?, " + consumptionResult.getTypeName() + ">[]");
		});
		
		static <ValueType, ConsumptionResult> ReturnResult<RegisteredType<ValueType, ConsumptionResult>> create(String name, Class<? extends Value<ValueType, ConsumptionResult>> typeClass)
		{
			//make sure that this is at the very least a subclass of Value
			Type plainType = stepUpToImplementation(typeClass.getGenericSuperclass(), Value.class);
			Type compoundType = stepUpToImplementation(typeClass.getGenericSuperclass(), CompoundArgumentValue.class);
			if (plainType == null && compoundType == null)
				return new ReturnResult<>("Does not implement " + Value.class.getName() + " or " + CompoundArgumentValue.class.getName());
			
			Type[] parameters;
			RegisteredType<ValueType, ConsumptionResult> registration;
			if (compoundType == null)
			{
				parameters = ((ParameterizedType)plainType).getActualTypeArguments();
				registration = new RegisteredPlainType<>();
			}
			else
			{
				parameters = ((ParameterizedType)compoundType).getActualTypeArguments();
				registration = new RegisteredCompoundType<>();
			}
			
			registration.typeClass = typeClass;
			registration.name = name;
			registration.valueType = parameters[0];
			registration.consumptionResult = parameters[1];
			
			Result result = registration.byteConsumer.capture();
			if (!result.success())
				return new ReturnResult(result.description());
			result = registration.validationValues.capture();
			if (!result.success())
				return new ReturnResult(result.description());
			result = registration.captureMethods();
			if (!result.success())
				return new ReturnResult(result.description());
			return new ReturnResult<>(registration);
		}
		
		/**
		 * Capture additional methods
		 * @return either a successful result, or the result of the first method that failed to capture
		 */
		abstract Result captureMethods();
		
		public String name()
		{
			return name;
		}
		
		public Class<? extends Value<ValueType, ConsumptionResult>> typeClass()
		{
			return typeClass;
		}
		
		public final class RequiredMethod
		{
			private final MethodValidator validator;
			private final Class<? extends Annotation> annotation;
			private Result valid = new Result("Not yet captured");
			private Method method;
			
			RequiredMethod(Class<? extends Annotation> annotation, MethodValidator validator)
			{
				this.annotation = annotation;
				this.validator = validator;
			}
			
			private Result capture()
			{
				valid = new Result("No implementation found.");
				for (Method method : typeClass.getMethods())
				{
					Result result = checkMethod(method);
					
					//if the method didn't have our annotation, we skip over it
					if (result == null)
						continue;
					valid = result;
					
					//if the method had our annotation but was invalid for some reason, then we have
					// failed to capture it
					if (!result.success())
						break;
					
					//if the method had our annotation and was valid, then we keep it and continue
					// checking to ensure no duplicates
					if (result.success())
						this.method = method;
				}
				if (valid.success())
					return new Result();
				else
					return new Result(RichStringBuilder.start(annotation.getSimpleName() + " is invalid: ").append(valid.description()).build());
			}
			
			private Result checkMethod(Method method)
			{
				if (!method.isAnnotationPresent(annotation))
					return null;
				
				//check to make sure this isn't a duplicate
				if (this.method != null)
					return new ReturnResult<>("Only one " + annotation.getSimpleName() + " method can be provided.");
				
				//make sure the method has the correct modifiers
				int modifiers = method.getModifiers();
				if (!Modifier.isPublic(modifiers))
					return new ReturnResult<>("must be public.");
				if (Modifier.isAbstract(modifiers))
					return new ReturnResult<>("must not be abstract.");
				if (!Modifier.isStatic(modifiers))
					return new ReturnResult<>("method must be static.");
				
				//validate the method's return type and arguments
				return validator.validate(method.getGenericReturnType(), method.getGenericParameterTypes(), valueType, consumptionResult);
			}
			
			public Result valid()
			{
				return valid;
			}
			
			public Object invoke()
			{
				return invoke(new Object[0]);
			}
			
			public Object invoke(Object[] arguments)
			{
				if (!valid.success())
					throw new RuntimeException("Could not call " + annotation.getSimpleName() + " in " + typeClass.getName() + " because the method signature is invalid: " + valid.description());
				
				try
				{
					return method.invoke(null, arguments);
				}
				catch (IllegalAccessException | ClassCastException | InvocationTargetException e)
				{
					Exception exception;
					if (e instanceof InvocationTargetException invocationTargetException)
						exception = invocationTargetException;
					else
						exception = e;
					throw new RuntimeException("Exception occurred while calling " + annotation.getSimpleName() + " in " + typeClass.getName() + ": " + Result.describeThrowableFull(exception));
				}
				catch (IllegalArgumentException e)
				{
					throw new RuntimeException(annotation.getSimpleName() + " in " + typeClass.getName() + " called with illegal argument: " + e.getMessage());
				}
			}
			
			static interface MethodValidator
			{
				Result validate(Type returnType, Type[] arguments, Type valueType, Type consumptionType);
			}
		}
		
		public Value<?, ConsumptionResult>[] validationValues()
		{
			return (Value<?, ConsumptionResult>[])validationValues.invoke();
		}
		
		public Consumer<Value<?, ConsumptionResult>, Byte> byteConsumer()
		{
			return (Consumer<Value<?, ConsumptionResult>, Byte>)byteConsumer.invoke();
		}
		
		public Consumer<Value<?, ConsumptionResult>, Byte> byteConsumer(Object[] data)
		{
			if (byteConsumer.method.getParameterCount() == 1)
			{
				if (data.length == 0)
					data = new Object[] {new Object[0]};
			}
			return (Consumer<Value<?, ConsumptionResult>, Byte>)byteConsumer.invoke(data);
		}
		
		public Consumer<Value<?, ConsumptionResult>, Character> characterConsumer()
		{
			return characterConsumer(new Object[0]);
		}
		
		public abstract Consumer<Value<?, ConsumptionResult>, Character> characterConsumer(Object[] data);
	}
	
	public static final class RegistrationQueue
	{
		public static record Entry<ValueType, ConsumptionResult>(String name, Class<? extends Value<ValueType, ConsumptionResult>> typeClass)
		{
		
		}
		
		public static RegistrationQueue start()
		{
			return new RegistrationQueue();
		}
		
		final ArrayList<Entry<?, ?>> queue = new ArrayList<>();
		
		public <ValueType, ConsumptionResult> RegistrationQueue add(String name, Class<? extends Value<ValueType, ConsumptionResult>> typeClass)
		{
			queue.add(new Entry(name, typeClass));
			return this;
		}
		
		RegistrationQueue addListValue(String name, Class<ListValue> typeClass)
		{
			queue.add(new Entry(name, typeClass));
			return this;
		}
		
		public Result[] process()
		{
			Result[] registrationResults = new Result[queue.size()];
			for (int index = 0; index < queue.size(); index++)
			{
				Entry entry = queue.get(index);
				Result result = register(entry.name, entry.typeClass);
				if (!result.success())
					throw new RuntimeException("Could not register value: " + result.description());
				registrationResults[index] = result;
			}
			queue.clear();
			return registrationResults;
		}
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
	
	public static <ValueType, ConsumptionResult> Result register(String name, Class<? extends Value<ValueType, ConsumptionResult>> typeClass)
	{
		//make sure nothing has already been registered with this name or class
		if (classMap.containsKey(typeClass))
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Already registered.");
		if (nameMap.containsKey(name))
			return new Result("Could not register type " + typeClass.getName() + " as '" + name + "': Another type is already registered under that name.");
		
		//verify that the implementation is valid, and create the registration
		ReturnResult<RegisteredType<ValueType, ConsumptionResult>> creationResult = RegisteredType.create(name, typeClass);
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
	
	private static <ValueType, ConsumptionResult> Result validate(RegisteredType<ValueType, ConsumptionResult> type)
	{
		//retrieve the array of validation values and make sure it has at least one entry
		Value<ValueType, ConsumptionResult>[] values;
		try
		{
			values = (Value<ValueType, ConsumptionResult>[])type.validationValues.invoke(null);
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
			validationResult = checkValidationValue(values[index]);
			if (!validationResult.success())
				return new Result(RichStringBuilder.start("Validation value #" + index + " failed: ").append(validationResult.description()).build());
		}
		
		return new Result();
	}
	
	private static <ValueType, ConsumptionType> Result checkValidationValue(Value<ValueType, ConsumptionType> testValue)
	{
		//null values shouldn't be supported
		ValueType value = testValue.get();
		if (value == null)
			return new Result("Value is null.");
		
		//ensure that setting and getting values is consistent
		testValue.set(value);
		if (!value.equals(testValue.get()))
			return new Result("Getting value produced a result that didn't match the set value.");
		
		//ensure that copying a value doesn't throw an exception
		Value<ValueType, ConsumptionType> second;
		try
		{
			second = testValue.copy();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while making a copy of the value: " + Result.describeThrowableFull(e));
		}
		
		//ensure that instances with the same value will equal each other
		boolean equal;
		try
		{
			equal = testValue.equals(second);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while checking data equality: " + Result.describeThrowableFull(e));
		}
		if (!equal)
			return new Result("Two instances were not equal despite having the same value.");
		
		//ensure that converting to string works as expected
		String string;
		try
		{
			string = testValue.asString();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while converting to string: " + Result.describeThrowableFull(e));
		}
		if (string == null)
			return new Result("asString can not return null.");
		
		//ensure that interpreting character data produces the original value while consuming the correct
		//amount of data
		//we add an extra character to the end to make sure the interpreter doesn't consume too much data
		string += ' ';
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> characterResult;
		Indexer<Character> characterSource = StringValue.indexer(string);
		try
		{
			characterResult = second.setFromCharacters(characterSource);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while parsing character data: " + Result.describeThrowableFull(e));
		}
		if (characterResult == null)
			return new Result("Character consumer can not return a null result.");
		if (!characterResult.success())
			return new Result(RichStringBuilder.start("Character consumption failed on data that should have been valid: ").append(characterResult.description()).build());
		//make sure that the extra character we added earlier wasn't consumed, and is the only character
		//that wasn't consumed.
		if (characterSource.atEnd())
			return new Result("Character consumer has consumed too much data.");
		characterSource.next();
		if (!characterSource.atEnd())
			return new Result("Character consumer has not consumed enough data.");
		if (!testValue.equals(second))
			return new Result("String conversion did not produce an equal value.");
		
		//ensure that converting to byte data works as expected
		byte[] byteData;
		try
		{
			byteData = testValue.asBytes();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while converting to byte data: " + Result.describeThrowableFull(e));
		}
		if (byteData == null)
			return new Result("asBytes can not return null.");
		
		//ensure that interpreting byte data produces the original value while consuming the correct
		//amount of data
		//we add an extra byte to the end to make sure the interpreter doesn't consume too much data
		ByteArrayBuilder builder = new ByteArrayBuilder();
		builder.add(byteData);
		builder.add((byte)0);
		byteData = builder.toPrimitiveArray();
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte> byteResult;
		Indexer<Byte> byteSource = ByteArrayBuilder.indexer(byteData);
		try
		{
			byteResult = second.setFromBytes(byteSource);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while parsing byte data: " + Result.describeThrowableFull(e));
		}
		if (byteResult == null)
			return new Result("Byte consumer can not return a null result.");
		if (!byteResult.success())
			return new Result(RichStringBuilder.start("Byte consumption failed on data that should have been valid: ").append(byteResult.description()).build());
		//make sure that the extra byte we added earlier wasn't consumed, and is the only byte
		//that wasn't consumed.
		if (byteSource.atEnd())
			return new Result("Byte consumer has consumed too much data.");
		byteSource.next();
		if (!byteSource.atEnd())
			return new Result("Byte consumer has not consumed enough data.");
		if (!testValue.equals(second))
			return new Result("Byte conversion did not produce an equal value.");
		
		//all checks have passed
		return new Result();
	}
}