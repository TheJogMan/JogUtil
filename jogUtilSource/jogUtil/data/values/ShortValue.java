package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.util.*;

public class ShortValue extends Value<Short, Short>
{
	public ShortValue()
	{
		super();
	}
	
	public ShortValue(Short value)
	{
		super(value);
	}
	
	@Override
	public String defaultName()
	{
		return "Short";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public Short emptyValue()
	{
		return 0;
	}
	
	@Override
	public String asString()
	{
		return "" + get();
	}
	
	@Override
	public byte[] asBytes()
	{
		return toByteData(get());
	}
	
	@Override
	protected Value<Short, Short> makeCopy()
	{
		return new ShortValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof ShortValue && ((ShortValue)value).get().equals(get());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Short>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(2);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new ShortValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Short>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, ByteValue.numericCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new ShortValue(Short.parseShort(string)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Short>[] validationValues()
	{
		return new ShortValue[] {
				new ShortValue((short)1684),
				new ShortValue(Short.MIN_VALUE),
				new ShortValue(Short.MAX_VALUE),
				new ShortValue((short)12)
		};
	}
	
	public static byte[] toByteData(short value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(value);
		return buffer.array();
	}
	
	public static short fromByteData(byte[] data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getShort();
	}
	
	public static short fromByteData(Byte[] data)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(data));
	}
	
	public static short fromByteData(Collection<Byte> data)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(data));
	}
}