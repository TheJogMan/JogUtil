package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.util.*;

public class IntegerValue extends Value<Integer, Integer>
{
	public IntegerValue(Integer integer)
	{
		super(integer);
	}
	
	public IntegerValue()
	{
		super();
	}
	
	@Override
	public Integer emptyValue()
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
	protected Value<Integer, Integer> makeCopy()
	{
		return new IntegerValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof IntegerValue && Objects.equals(((IntegerValue) value).get(), get());
	}
	
	@Override
	public String defaultName()
	{
		return "Integer";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Integer>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(4);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new IntegerValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Integer>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, ByteValue.numericCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new IntegerValue(Integer.parseInt(string)),
														source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Integer>[] validationValues()
	{
		return new IntegerValue[] {
			new IntegerValue(128),
			new IntegerValue(Integer.MIN_VALUE),
			new IntegerValue(Integer.MAX_VALUE)
		};
	}
	
	public static byte[] toByteData(int value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(value);
		return buffer.array();
	}
	
	public static int fromByteData(byte[] byteData)
	{
		ByteBuffer buffer = ByteBuffer.wrap(byteData);
		return buffer.getInt();
	}
	
	public static int fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	public static int fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(byteData.toArray(new Byte[0]));
	}
}