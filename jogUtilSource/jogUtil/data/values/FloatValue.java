package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.util.*;

public class FloatValue extends Value<Float, Float>
{
	public FloatValue()
	{
		super();
	}
	
	public FloatValue(Float value)
	{
		super(value);
	}
	
	@Override
	public String defaultName()
	{
		return "Float";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public Float emptyValue()
	{
		return 0.0f;
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
	protected Value<Float, Float> makeCopy()
	{
		return new FloatValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof FloatValue && ((FloatValue)value).get().equals(get());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Float>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(4);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			return new Consumer.ConsumptionResult<>(new FloatValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Float>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, floatingPointCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new FloatValue(Float.parseFloat(string)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Float>[] validationValues()
	{
		return new FloatValue[] {
			new FloatValue(56.9981f),
			new FloatValue(Float.MIN_VALUE),
			new FloatValue(Float.MAX_VALUE),
			new FloatValue(12f)
		};
	}
	
	public static final char[] floatingPointCharacters = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
														  '9', '0', '.', '-', 'E'};
	
	public static byte[] toByteData(float value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putFloat(value);
		return buffer.array();
	}
	
	public static float fromByteData(byte[] byteData)
	{
		ByteBuffer buffer = ByteBuffer.wrap(byteData);
		return buffer.getFloat();
	}
	
	public static float fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	public static float fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(byteData.toArray(new Byte[0]));
	}
}