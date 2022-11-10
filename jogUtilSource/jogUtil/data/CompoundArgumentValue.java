package jogUtil.data;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.lang.reflect.*;
import java.util.*;

public abstract class CompoundArgumentValue<ValueType, ConsumptionType> extends Value<ValueType, ConsumptionType>
{
	Object[] initData;
	
	public CompoundArgumentValue()
	{
		super();
	}
	
	public CompoundArgumentValue(ValueType value)
	{
		super(value);
	}
	
	public CompoundArgumentValue(Object[] initData)
	{
		super(initData);
	}
	
	@Override
	public String defaultName()
	{
		return null;
	}
	
	@Override
	public final void initArgument(Object[] data)
	{
		initData = data;
	}
	
	@Override
	public final List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	public AdaptiveArgumentList argumentList()
	{
		return type().argumentList(initData);
	}
	
	public ConsumptionType buildValue(AdaptiveInterpretation result, Executor executor)
	{
		return (ConsumptionType)type().buildValue(result, executor);
	}
	
	public static Consumer<Value<?, ?>, Character> compoundCharacterConsumer(TypeRegistry.RegisteredType type)
	{
		if (!type.compoundArgument)
			return null;
		
		return (source) ->
		{
			Executor executor = new Executor.HeadlessExecutor();
			//get the argument list
			AdaptiveArgumentList list = type.argumentList(new Object[0]);
			
			//interpret the list, and make sure it was successful
			AdaptiveInterpretation result = list.interpret(source, executor);
			if (!result.success())
				return new Consumer.ConsumptionResult<>(source, result.description());
			
			//convert the interpretation results into the resulting value
			Object object = type.buildValue(result, executor);
			source.setPosition(result.indexer().position());
			
			//create the Value object and set the value, then make sure nothing went wrong
			try
			{
				Value<?, ?> value = type.typeClass.getConstructor().newInstance();
				Result setResult = value.internalSet(object);
				if (setResult.success())
					return new Consumer.ConsumptionResult<>(value, result.indexer());
				else
					return new Consumer.ConsumptionResult<>(result.indexer(), RichStringBuilder.start("Could not set value: ").append(setResult.description()).build());
			}
			catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
			{
				return new Consumer.ConsumptionResult<>(result.indexer(), "Exception occurred while creating a new instance of the value: " + Result.describeThrowableFull(e));
			}
		};
	}
}