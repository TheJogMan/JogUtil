package jogUtil.data;

import jogUtil.*;
import jogUtil.data.values.*;

import java.lang.reflect.*;
import java.util.*;

public class TypeRegistry
{
	private static final HashMap<String, RegisteredType> nameMap = new HashMap<>();
	private static final HashMap<Class<?>, RegisteredType> classMap = new HashMap<>();
	private static final ReturnResult<Result[]> defaultValueStatus = registerDefaults();
	
	private static ReturnResult<Result[]> registerDefaults()
	{
		Object[][] typeClasses = {
				{"String", StringValue.class}
		};
		
		boolean atLeastOneFailed = false;
		Result[] registrationResults = new Result[typeClasses.length];
		for (int index = 0; index < typeClasses.length; index++)
		{
			Class<? extends Value<?, ?>> typeClass = (Class<? extends Value<?, ?>>)typeClasses[index][1];
			String name = (String)typeClasses[index][0];
			Result result = register(name, typeClass);
			if (!result.success())
				atLeastOneFailed = true;
			registrationResults[index] = result;
		}
		
		return new ReturnResult<>(atLeastOneFailed, registrationResults);
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
			return new Result();
	}
	
	public static final class RegisteredType
	{
		Class<? extends Value<?, ?>> typeClass;
		String name;
		Type valueType;
		Type consumptionResult;
		
		
		Method emptyValue;
		Method byteConsumer;
		Method characterConsumer;
		Method validationValues;
		
		static ReturnResult<RegisteredType> create(String name, Class<? extends Value<?, ?>> typeClass)
		{
		
		}
	}
	
	private static Result validate(RegisteredType type)
	{
	
	}
	
	public static RegisteredType get(Class<?> typeClass)
	{
		return classMap.get(typeClass);
	}
	
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
		return defaultValueStatus.value();
	}
}
