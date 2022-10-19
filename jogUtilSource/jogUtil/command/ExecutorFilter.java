package jogUtil.command;

import jogUtil.*;

import java.util.*;

public interface ExecutorFilter
{
	ArrayList<Filter> filters = new ArrayList<>();
	
	public default void addFilter(Filter filter)
	{
		filters.add(filter);
	}
	
	public default void removeFilter(Filter filter)
	{
		filters.remove(filter);
	}
	
	/**
	 * Checks if the executor passes all the set filters.
	 * <p>
	 * Returned Result will either be successful with no description, or
	 * be the filter result of the first filter that failed.
	 * </p>
	 *
	 * @param executor
	 * @return
	 * @see jogUtil.command.ExecutorFilter.Filter#canExecute(Executor);
	 */
	public default Result canExecute(Executor executor)
	{
		for (Filter filter : filters)
		{
			Result result = filter.canExecute(executor);
			if (!result.success())
				return result;
		}
		return new Result();
	}
	
	public interface Filter
	{
		public Result canExecute(Executor executor);
	}
}