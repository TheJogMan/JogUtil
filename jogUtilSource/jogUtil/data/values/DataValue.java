package jogUtil.data.values;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.util.*;

public class DataValue extends Value<Data, Data>
{
	public DataValue()
	{
		super();
	}
	
	public DataValue(Data data)
	{
		super(data);
	}
	
	@Override
	public String defaultName()
	{
		return "Data";
	}
	
	@Override
	protected List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public Data emptyValue()
	{
		return new Data();
	}
	
	@Override
	public String asString()
	{
		return get().toString();
	}
	
	@Override
	public byte[] asBytes()
	{
		return get().toByteData();
	}
	
	@Override
	protected Value<Data, Data> makeCopy()
	{
		return new DataValue(get().copy());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof DataValue && ((DataValue)value).get().matches(get());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Data>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			Consumer.ConsumptionResult<Data, Byte> result = Data.byteConsumer().consume(source);
			if (result.success())
				return new Consumer.ConsumptionResult<>(new DataValue(result.value()), source,
														result.description());
			else
				return new Consumer.ConsumptionResult<>(source, result.description());
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Data>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			Consumer.ConsumptionResult<Data, Character> result = Data.characterConsumer().consume(source);
			if (result.success())
				return new Consumer.ConsumptionResult<>(new DataValue(result.value()), source,
														result.description());
			else
				return new Consumer.ConsumptionResult<>(source, result.description());
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Data>[] validationValues()
	{
		Data data = new Data();
		
		data.put("test1", IntegerValue.validationValues()[0]);
		data.put("test2", StringValue.validationValues()[0]);
		data.put("test3", ListValue.validationValues()[0]);
		data.put("test4", BooleanValue.validationValues()[0]);
		data.put("test5", UUIDValue.validationValues()[0]);
		
		return new DataValue[] {new DataValue(data)};
	}
}