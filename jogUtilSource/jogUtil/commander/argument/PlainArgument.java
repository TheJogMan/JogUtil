package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;

import java.util.*;

public abstract class PlainArgument<ValueType> implements Argument<ValueType>
{
	CompletionBehavior behavior = CompletionBehavior.FILTER;
	private final ArrayList<Filter> filters = new ArrayList<>();
	private final ArrayList<ExecutorFilter.Transformer> transformers = new ArrayList<>();
	
	@Override
	public void addFilter(Filter filter)
	{
		filters.add(filter);
	}
	
	@Override
	public void removeFilter(Filter filter)
	{
		filters.remove(filter);
	}
	
	@Override
	public void addTransformer(Transformer transformer)
	{
		transformers.add(transformer);
	}
	
	@Override
	public void removeTransformer(Transformer transformer)
	{
		transformers.add(transformer);
	}
	
	@Override
	public void transform(Executor executor)
	{
		for (ExecutorFilter.Transformer transformer : transformers)
			transformer.transform(executor);
	}
	
	@Override
	public Result canExecute(Executor executor, boolean applyTransformers)
	{
		if (applyTransformers)
			transform(executor);
		
		for (ExecutorFilter.Filter filter : filters)
		{
			Result result = filter.canExecute(executor);
			if (!result.success())
				return result;
		}
		return new Result();
	}
	
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