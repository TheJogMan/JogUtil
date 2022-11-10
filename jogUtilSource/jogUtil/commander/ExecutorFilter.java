package jogUtil.commander;

import jogUtil.*;

public interface ExecutorFilter
{
	public void addFilter(Filter filter);
	public void removeFilter(Filter filter);
	/**
	 * Checks if the executor passes all the set filters.
	 * <p>
	 *     Returned Result will either be successful with no description, or
	 *     be the filter result of the first filter that failed.
	 * </p>
	 *
	 * @param executor
	 * @param applyTransformers Whether transformers should be applied before checking filters.
	 * @return
	 * @see jogUtil.commander.ExecutorFilter.Filter#canExecute(Executor)
	 * @see #transform(Executor)
	 */
	public Result canExecute(Executor executor, boolean applyTransformers);
	public void addTransformer(Transformer transformer);
	public void removeTransformer(Transformer transformer);
	
	/**
	 * Applies all the transformers to an executor.
	 * <p>
	 *     This is intended to be used before checking if an executor passes the filters, but is not
	 *     required.
	 * </p>
	 * @param executor
	 * @see jogUtil.commander.ExecutorFilter.Transformer#transform(Executor)
	 */
	public void transform(Executor executor);
	
	public default Result canExecute(Executor executor)
	{
		return canExecute(executor, true);
	}
	
	
	public interface Filter
	{
		public Result canExecute(Executor executor);
	}
	
	public interface Transformer
	{
		public void transform(Executor executor);
	}
}