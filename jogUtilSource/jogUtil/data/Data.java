package jogUtil.data;

import jogUtil.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class Data implements Iterable<Value<?, ?>>
{
	KeyedList<String, Value<?, ?>> values = new KeyedList<>();
	
	public int size()
	{
		return values.size();
	}
	
	public boolean has(String name)
	{
		return values.containsKey(name);
	}
	
	/**
	 * Adds a new Value to this Data object.
	 * <p>
	 *     If the given value is already stored in another Data object, then a new unique copy will be
	 *     created to be stored in this one. In either case, whatever value object gets stored in this
	 *     object will be returned for external reference.
	 * </p>
	 * <p>
	 *     If a value has already been entered into this object with the same name, it will be overwritten
	 *     with the new value. Any external references to the old value will remain intact, and the old
	 *     value can still be used externally but will no longer consider this object to be it's parent.
	 * </p>
	 * @param name
	 * @param value
	 * @return
	 * @param <ValueType>
	 * @param <ConsumptionType>
	 */
	public <ValueType, ConsumptionType> Value<ValueType, ConsumptionType> put(
			String name, Value<ValueType, ConsumptionType> value)
	{
		if (value.parent != null)
			value = value.copy();
		
		if (values.containsKey(name))
		{
			Value<?, ?> oldValue = values.get(name);
			oldValue.parent = null;
			oldValue.name = null;
			values.remove(name);
		}
		
		values.put(name, value);
		value.parent = this;
		value.name = name;
		return value;
	}
	
	/**
	 * Retrieves a Value object from this Data object.
	 * <p>
	 *     If there is no value in this Data object with the given name, then the provided default value
	 *     will be returned instead. It will also be put into this data object.<br>
	 *     If the given default value was already part of another Data object, then a new unique copy will
	 *     be used.
	 * </p>
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public Value<?, ?> get(String name, Value<?, ?> defaultValue)
	{
		if (values.containsKey(name))
			return values.get(name);
		else
			return put(name, defaultValue);
	}
	
	/**
	 * Retrieves a specific type of value from this Data object.
	 * <p>
	 *     If there is no value with the given name, or if it is an incompatible type, then the provided
	 *     default value will be returned.<br>
	 *     If there is no value with the given name, then the provided default will be added to this
	 *     Data object.
	 * </p>
	 * @param name
	 * @param defaultValue
	 * @return
	 * @param <Type>
	 */
	public <Type> Type getValue(String name, Value<Type, ?> defaultValue)
	{
		if (values.containsKey(name))
		{
			Type value;
			try
			{
				value = (Type)values.get(name).get();
			}
			catch (ClassCastException e)
			{
				value = defaultValue.get();
			}
			return value;
		}
		else
			return put(name, defaultValue).get();
	}
	
	/**
	 * Retrieves a specific type of Value object from this Data object
	 * <p>
	 *     If there is no Value with the given name, or it is an incompatible type, then the provided
	 *     default Value will be returned.<br>
	 *     If there is no value with the given name, then the provided default Value will be added to this
	 *     Data object.
	 * </p>
	 * @param name
	 * @param defaultValue
	 * @return
	 * @param <Type>
	 */
	public <Type extends Value<?, ?>> Type getObject(String name, Type defaultValue)
	{
		Value<?, ?> value = get(name, defaultValue);
		try
		{
			return (Type)value;
		}
		catch (ClassCastException e)
		{
			return defaultValue;
		}
	}
	
	/**
	 * Removes a Value from this Data object.
	 * <p>
	 *     The removed value is returned and can still be used externally.
	 * </p>
	 * @param name
	 * @return
	 */
	public Value<?, ?> remove(String name)
	{
		if (values.containsKey(name))
		{
			Value<?, ?> value = values.get(name);
			values.remove(name);
			value.parent = null;
			value.name = null;
			return value;
		}
		else
			return null;
	}
	
	public Data copy()
	{
		Data data = new Data();
		for (Value<?, ?> value : this)
			data.put(value.name, value);
		return data;
	}
	
	/**
	 * Checks if two Data objects contain identical values
	 * @param otherData
	 * @return
	 */
	public boolean matches(Data otherData)
	{
		if (otherData == null || otherData.size() != size())
			return false;
		
		for (Value<?, ?> value : this)
		{
			Value<?, ?> otherValue = otherData.values.get(value.name);
			if (otherValue == null || !value.checkDataEquality(otherValue))
				return false;
		}
		return true;
	}
	
	@Override
	public Iterator<Value<?, ?>> iterator()
	{
		return values.iterator();
	}
	
	/**
	 * Provides an array of all the names of the Values in this Data object
	 * @return
	 */
	public String[] names()
	{
		String[] names = new String[values.size()];
		for (int index = 0; index < values.size(); index++)
		{
			names[index] = values.get(index).getKey();
			index++;
		}
		return names;
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("{\r\n");
		for (int valueIndex = 0; valueIndex < values.size(); valueIndex++)
		{
			KeyedList.KeyedEntry<String, Value<?, ?>> entry = values.get(valueIndex);
			TypeRegistry.RegisteredType type = TypeRegistry.get(entry.getValue().getClass());
			if (type == null)
				throw new RuntimeException("Can not convert data to string: "
										   + entry.getValue().getClass() + " isn't a registered type.");
			
			builder.append("\t").append(StringValue.pack(type.name())).append(":");
			builder.append(StringValue.pack(entry.getKey())).append(": ");
			
			String string = entry.getValue().toString();
			for (int index = 0; index < string.length(); index++)
			{
				char ch = string.charAt(index);
				builder.append(ch);
				if (ch == '\n')
					builder.append('\t');
			}
			if (valueIndex < values.size() - 1)
				builder.append(',');
			builder.append("\r\n");
		}
		builder.append('}');
		
		return builder.toString();
	}
	
	public byte[] toByteData()
	{
		ByteArrayBuilder builder = new ByteArrayBuilder();
		ArrayList<TypeRegistry.RegisteredType> typeIndex = new ArrayList<>();
		for (Value<?, ?> value : this)
		{
			TypeRegistry.RegisteredType type = TypeRegistry.get(value.getClass());
			if (type == null)
				throw new RuntimeException("Can not convert data to bytes: "
										   + value.getClass() + " isn't a registered type.");
			if (!typeIndex.contains(type))
				typeIndex.add(type);
		}
		
		builder.add(typeIndex.size());
		for (TypeRegistry.RegisteredType type : typeIndex)
			builder.add(type.name());
		
		builder.add(size());
		for (Value<?, ?> value : this)
		{
			TypeRegistry.RegisteredType type = TypeRegistry.get(value.getClass());
			int typeNumber = typeIndex.indexOf(type);
			
			builder.add(typeNumber);
			builder.add(value.name);
			builder.add(value.asBytes());
		}
		
		return builder.toPrimitiveArray();
	}
	
	public static Data fromBytes(Indexer<Byte> source)
	{
		return byteConsumer().consume(source).value();
	}
	
	public static Consumer<Data, Byte> byteConsumer()
	{
		return (source) ->
		{
			Consumer.ConsumptionResult<Value<?, Integer>, Byte> indexSizeResult =
					IntegerValue.getByteConsumer().consume(source);
			if (!indexSizeResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder
						.start("Could not parse type index length: ")
						.append(indexSizeResult.description()).build());
			int indexSize = (int)indexSizeResult.value().get();
			ArrayList<TypeRegistry.RegisteredType> typeIndex = new ArrayList<>(indexSize);
			
			for (int index = 0; index < indexSize; index++)
			{
				Consumer.ConsumptionResult<Value<?, String>, Byte> typeNameResult =
						StringValue.getByteConsumer().consume(source);
				if (!typeNameResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse type index entry #" + index + ": ")
							.append(typeNameResult.description()).build());
				String typeName = (String)typeNameResult.value().get();
				TypeRegistry.RegisteredType type = TypeRegistry.get(typeName);
				if (type == null)
					return new Consumer.ConsumptionResult<>(source, "Type index entry #"
																+ index + " \"" + typeName
																+ "\" is not a registered value type.");
				
				typeIndex.add(type);
			}
			
			Consumer.ConsumptionResult<Value<?, Integer>, Byte> valueCountResult =
					IntegerValue.getByteConsumer().consume(source);
			if (!indexSizeResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder
						.start("Could not parse value count: ")
						.append(indexSizeResult.description()).build());
			int valueCount = (int)valueCountResult.value().get();
			
			Data data = new Data();
			for (int index = 0; index < valueCount; index++)
			{
				Consumer.ConsumptionResult<Value<?, Integer>, Byte> typeNumberResult =
						IntegerValue.getByteConsumer().consume(source);
				if (!indexSizeResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse type number for value #" + index + ": ")
							.append(indexSizeResult.description()).build());
				TypeRegistry.RegisteredType type = typeIndex.get((int)typeNumberResult.value().get());
				
				Consumer.ConsumptionResult<Value<?, String>, Byte> valueNameResult =
						StringValue.getByteConsumer().consume(source);
				if (!valueNameResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse value name for value #" + index + ": ")
							.append(valueNameResult.description()).build());
				
				Consumer.ConsumptionResult<Value<?, ?>, Byte> valueResult =
						type.byteConsumer().consume(source);
				if (!valueResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse value #" + index + " as " + type.name()
								   + ": ").append(valueResult.description()).build());
				
				data.put((String)valueNameResult.value().get(), valueResult.value());
			}
			
			return new Consumer.ConsumptionResult<>(data, source);
		};
	}
	
	public static Data fromCharacters(Indexer<Character> source)
	{
		return characterConsumer().consume(source).value();
	}
	
	public static final Character[] formattingCharacters = {' ', '\r', '\n', '\t'};
	
	public static Consumer<Data, Character> characterConsumer()
	{
		return (source) ->
		{
			if (source.atEnd() || source.next() != '{')
				return new Consumer.ConsumptionResult<>(source, "Must begin with '{'");
			source.skip(formattingCharacters);
			
			boolean expectingNext = false;
			Data data = new Data();
			int index = 0;
			while (!source.atEnd() && source.get() != '}')
			{
				expectingNext = false;
				
				Consumer.ConsumptionResult<Value<?, String>, Character> typeNameResult =
						StringValue.getCharacterConsumer().consume(source);
				if (!typeNameResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse type name for value #" + index + ": ")
							.append(typeNameResult.description()).build());
				String typeName = (String)typeNameResult.value().get();
				TypeRegistry.RegisteredType type = TypeRegistry.get(typeName);
				if (type == null)
					return new Consumer.ConsumptionResult<>(source, "Could not parse value #"
																+ index + ": \"" + typeName
																+ "\" is not a registered value type.");
				
				if (source.next() != ':')
					return new Consumer.ConsumptionResult<>(source,
												"Expected ':' between type name and value name.");
				
				Consumer.ConsumptionResult<Value<?, String>, Character> valueNameResult =
						StringValue.getCharacterConsumer().consume(source);
				if (!valueNameResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse value name for value #" + index + ": ")
							.append(valueNameResult.description()).build());
				
				//this effectively checks for ": "
				if (source.next() != ':' || source.next() != ' ')
					return new Consumer.ConsumptionResult<>(source,
															"Expected \": \" after value name.");
				String name = (String)valueNameResult.value().get();
				
				Consumer.ConsumptionResult<Value<?, ?>, Character> valueResult =
						type.characterConsumer().consume(source);
				if (!valueResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder
							.start("Could not parse value #" + index + " \"" + name
								   + "\" as " + type.name() + ": ")
							.append(valueResult.description()).build());
				
				data.put(name, valueResult.value());
				
				if (source.get() == ',')
				{
					source.next();
					expectingNext = true;
				}
				index++;
				source.skip(formattingCharacters);
			}
			if (source.atEnd())
			{
				if (expectingNext)
					return new Consumer.ConsumptionResult<>(source,
															"Expecting another value after ','");
				else
					return new Consumer.ConsumptionResult<>(source, "Must end with '}'");
			}
			source.next();
			return new Consumer.ConsumptionResult<>(data, source);
		};
	}
}