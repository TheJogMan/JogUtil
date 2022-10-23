package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.util.*;

public class DoubleValue extends Value<Double, Double>
{
	public DoubleValue()
	{
		super();
	}
	
	public DoubleValue(Double value)
	{
		super(value);
	}
	
	@Override
	public String defaultName()
	{
		return "Double";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public Double emptyValue()
	{
		return 0.0;
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
	protected Value<Double, Double> makeCopy()
	{
		return new DoubleValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof DoubleValue && ((DoubleValue)value).get().equals(get());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Double>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(8);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			return new Consumer.ConsumptionResult<>(new DoubleValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Double>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, FloatValue.floatingPointCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new DoubleValue(Double.parseDouble(string)),
														source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Double>[] validationValues()
	{
		return new DoubleValue[] {
				new DoubleValue(124.33421),
				new DoubleValue(Double.MIN_VALUE),
				new DoubleValue(Double.MAX_VALUE),
				new DoubleValue(12.0)
		};
	}
	
	public static byte[] toByteData(double value)
	{
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putDouble(value);
		return buffer.array();
	}
	
	public static double fromByteData(byte[] byteData)
	{
		ByteBuffer buffer = ByteBuffer.wrap(byteData);
		return buffer.getDouble();
	}
	
	public static double fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	public static double fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(byteData.toArray(new Byte[0]));
	}
}