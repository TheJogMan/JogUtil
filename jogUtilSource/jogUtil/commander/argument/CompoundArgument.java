package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public abstract class CompoundArgument<ValueType> implements Argument<ValueType>
{
	final AdaptiveArgumentList arguments = new AdaptiveArgumentList();
	
	protected abstract ReturnResult<ValueType> compoundInterpretation(AdaptiveInterpretation result, Executor executor);
	
	@Override
	public String defaultName()
	{
		return arguments.toString();
	}
	
	@Override
	public final List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return arguments.getCompletions(source, executor);
	}
	
	@Override
	public ReturnResult<ValueType> interpretArgument(Indexer<Character> source, Executor executor)
	{
		ReturnResult<AdaptiveInterpretation> result = arguments.compoundInterpret(source, executor);
		if (!result.success())
			return new ReturnResult<>(result.description());
		else
			return compoundInterpretation(result.value(), executor);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		arguments.addArgument(listNumber, argument, name, data, description);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		arguments.addArgument(listNumber, argument, name, data, description);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		arguments.addArgument(listNumber, argument, data, description);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		arguments.addArgument(listNumber, argument, data, description);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		arguments.addArgument(listNumber, argument, name, data);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data)
	{
		arguments.addArgument(listNumber, argument, data);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument)
	{
		arguments.addArgument(listNumber, argument);
	}
	
	protected final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name)
	{
		arguments.addArgument(listNumber, argument, name);
	}
	
	protected final void addFilterToList(int listNumber, Filter filter)
	{
		arguments.addFilterToList(listNumber, filter);
	}
	
	CompletionBehavior behavior = CompletionBehavior.FILTER;
	
	@Override
	public final void setCompletionBehavior(CompletionBehavior behavior)
	{
		this.behavior = behavior;
	}
	
	@Override
	public final CompletionBehavior getCompletionBehavior()
	{
		return behavior;
	}
}