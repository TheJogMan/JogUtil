package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.util.*;

public class LongValue extends Value<Long, Long>
{
	public LongValue()
	{
		super();
	}
	
	public LongValue(Long value)
	{
		super(value);
	}
	
	@Override
	public String defaultName()
	{
		return "Long";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public Long emptyValue()
	{
		return 0L;
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
	protected Value<Long, Long> makeCopy()
	{
		return new LongValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof LongValue && ((LongValue)value).get().equals(get());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Long>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(8);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new LongValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Long>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, ByteValue.numericCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new LongValue(Long.parseLong(string)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Long>[] validationValues()
	{
		return new LongValue[] {
				new LongValue(168465135843L),
				new LongValue(Long.MIN_VALUE),
				new LongValue(Long.MAX_VALUE),
				new LongValue(12L)
		};
	}
	
	public static byte[] toByteData(long value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(value);
		return buffer.array();
	}
	
	public static long fromByteData(byte[] data)
	{
		ByteBuffer buffer = ByteBuffer.wrap(data);
		return buffer.getLong();
	}
	
	public static long fromByteData(Byte[] data)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(data));
	}
	
	public static long fromByteData(Collection<Byte> data)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(data));
	}
}