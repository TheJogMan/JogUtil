package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.util.*;

public class BooleanValue extends Value<Boolean, Boolean>
{
	public BooleanValue()
	{
		super();
	}
	
	public BooleanValue(Boolean value)
	{
		super(value);
	}
	
	@Override
	public String defaultName()
	{
		return "Boolean";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		ArrayList<String> completions = new ArrayList<>();
		String token = StringValue.consumeString(source, ' ').toLowerCase();
		if ("true".startsWith(token))
			completions.add("True");
		else if ("false".startsWith(token))
			completions.add("False");
		return completions;
	}
	
	@Override
	public Boolean emptyValue()
	{
		return false;
	}
	
	@Override
	public String asString()
	{
		return get() ? "True" : "False";
	}
	
	@Override
	public byte[] asBytes()
	{
		return toByteData(get());
	}
	
	@Override
	protected Value<Boolean, Boolean> makeCopy()
	{
		return new BooleanValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof BooleanValue && ((BooleanValue)value).get() == get();
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Boolean>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			if (source.atEnd())
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new BooleanValue(source.next() == 1), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Boolean>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			if (StringValue.consumeSequence(source, "true", false))
				return new Consumer.ConsumptionResult<>(new BooleanValue(true), source);
			else if (StringValue.consumeSequence(source, "false", false))
				return new Consumer.ConsumptionResult<>(new BooleanValue(false), source);
			else
				return new Consumer.ConsumptionResult<>(source, "Invalid Boolean format.");
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Boolean>[] validationValues()
	{
		return new BooleanValue[] {
			new BooleanValue(true),
			new BooleanValue(false)
		};
	}
	
	public static byte[] toByteData(boolean value)
	{
		return new byte[] {value ? (byte)1 : 0};
	}
}