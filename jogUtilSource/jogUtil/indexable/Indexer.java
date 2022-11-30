package jogUtil.indexable;

import java.util.*;

public class Indexer<Type> implements Iterator<Type>
{
	final Indexable<Type> indexable;
	int index;
	final ArrayDeque<FilterState<Type>> filterStateStack = new ArrayDeque<>();
	
	public Indexer(Indexable<Type> indexable)
	{
		this(indexable, 0);
	}
	
	public Indexer(Indexable<Type> indexable, int index)
	{
		this.indexable = indexable;
		this.index = index;
		filterStateStack.push(new FilterState<>());
	}
	
	/**
	 * Checks if more values are currently available.
	 * <p>
	 *     Filters are enabled by default.
	 * </p>
	 * @return
	 * @see #hasNext(boolean)
	 */
	@Override
	public boolean hasNext()
	{
		return hasNext(true);
	}
	
	/**
	 * Checks if more values are currently available.
	 * <p>
	 *     If the underlying indexable is not complete, this will still only
	 *     return true if retrieving the next value won't cause this thread to
	 *     yield.<br>
	 *     <br>
	 *     If filters are enabled then any unwanted values will be skipped
	 *     over first.
	 * </p>
	 * @param applyFilter
	 * @return
	 */
	public boolean hasNext(boolean applyFilter)
	{
		if (applyFilter)
			skipUnwanted();
		return index < indexable.size();
	}
	
	/**
	 * Returns the indexer's current position in the underlying indexable.
	 * @return
	 */
	public int position()
	{
		return index;
	}
	
	/**
	 * Sets the indexer's current position in the underlying indexable.
	 * @param index
	 */
	public void setPosition(int index)
	{
		if (index < 0 || index > indexable.size())
			throw new IndexOutOfBoundsException();
		this.index = index;
	}
	
	/**
	 * Returns the current size of the underlying indexable.
	 * @return
	 */
	public int size()
	{
		return indexable.size();
	}
	
	/**
	 * Checks if the underlying indexable is complete.
	 * @return
	 * @see jogUtil.indexable.Indexable#complete()
	 */
	public boolean complete()
	{
		return indexable.complete();
	}
	
	/**
	 * Checks if the indexer has reached the end of the underlying indexable.
	 * <p>
	 *     If the indexable is incomplete, then more values might be available
	 *     in the future even if there are none right now.  The indexer has only
	 *     reached the end if there are no values available and the indexable is
	 *     also complete.
	 * </p>
	 * @return
	 * @see #complete()
	 */
	public boolean atEnd()
	{
		return complete() && !hasNext();
	}
	
	/**
	 * Returns the underlying indexable that this indexer is stepping through.
	 * @return
	 */
	public Indexable<Type> indexable()
	{
		return indexable;
	}
	
	/**
	 * Gets the next value from the underlying indexable.
	 * <p>
	 *     Filters will be applied by default.
	 * </p>
	 * @return next value, or null if we are at the end of the indexable.
	 * @see #next(boolean)
	 */
	@Override
	public Type next()
	{
		return next(true);
	}
	
	/**
	 * Gets multiple values from the underlying indexable.
	 * <p>
	 *     Filters will be applied by default.<br>
	 *     If the end of the indexable is reached before all the desired
	 *     values are retrieved, then the remaining values will be null.
	 * </p>
	 * @param count number of values to retrieve
	 * @return
	 * @see #next(int, boolean)
	 * @see #allNext(int)
	 */
	public ArrayList<Type> next(int count)
	{
		return next(count, true);
	}
	
	/**
	 * Gets multiple values from the underlying indexable.
	 * <p>
	 *     Filters will be applied by default.<br>
	 *     If the end of the indexable is reached before all the desired
	 *     values are retrieved, then null will be returned.
	 * </p>
	 * @param count number of values to retrieve
	 * @return
	 * @see #allNext(int, boolean)
	 * @see #next(int)
	 */
	public ArrayList<Type> allNext(int count)
	{
		return allNext(count, true);
	}
	
	/**
	 * Gets the current value from the underlying indexable and advances the indexer.
	 * <p>
	 *     If filters are being applied, then advancing the indexer will skip over any
	 *     unwanted values.
	 * </p>
	 * @param applyFilter Whether filters should be applied.
	 * @return Next value or null.
	 * @see #get(boolean)
	 */
	public Type next(boolean applyFilter)
	{
		Type value = get(applyFilter);
		index++;
		if (applyFilter)
			skipUnwanted();
		return value;
	}
	
	/**
	 * Advances the indexer until the current value passes the filters
	 * <p>
	 *     If there are no further values available, but the indexable is incomplete
	 *     then this thread will yield until more values are added.
	 * </p>
	 */
	private void skipUnwanted()
	{
		boolean pass = false;
		while (!pass)
		{
			if (hasNext(false))
			{
				if (filterCheck(indexable.get(index)))
					pass = true;
				else
					index++;
			}
			else if (complete())
				return;
			else
				indexable.waitForData();
		}
	}
	
	/**
	 * Gets the current value from the underlying indexable.
	 * <p>
	 *     Filters are applied by default.
	 * </p>
	 * @return the current value, or null.
	 * @see #get(boolean)
	 */
	public Type get()
	{
		return get(true);
	}
	
	/**
	 * Gets the current value from the underlying indexable.
	 * <p>
	 *     If there are no values available, but the indexable is incomplete
	 *     then this thread will yield until more values are added.<br>
	 *     If there are no further values available and the indexable is complete, then
	 *     null will be returned.<br>
	 *     <br>
	 *     If filters are applied the indexer will first be advanced until the current
	 *     values passes the filters.
	 * </p>
	 * @param applyFilter
	 * @return the current value, or null.
	 * @see jogUtil.indexable.Indexable#waitForData()
	 */
	public Type get(boolean applyFilter)
	{
		if (applyFilter)
			skipUnwanted();
		if (index < indexable.size())
			indexable.waitForData();
		return indexable.get(index);
	}
	
	/**
	 * Gets multiple values from the underlying indexable.
	 * <p>
	 *     If the end of the indexable is reached before all the desired
	 *     values are retrieved, then the remaining values will be null.
	 * </p>
	 * @param count number of values to retrieve
	 * @param applyFilter
	 * @return Whether filters should be applied.
	 * @see #next(boolean)
	 * @see #allNext(int, boolean)
	 */
	public ArrayList<Type> next(int count, boolean applyFilter)
	{
		return unifiedNext(count, applyFilter, true);
	}
	
	/**
	 * Gets multiple values from the underlying indexable.
	 * <p>
	 *     If the end of the indexable is reached before all the desired
	 *     values are retrieved, then null will be returned.
	 * </p>
	 * @param count number of values to retrieve
	 * @param applyFilter
	 * @return Whether filters should be applied.
	 * @see #next(boolean)
	 * @see #next(int, boolean)
	 */
	public ArrayList<Type> allNext(int count, boolean applyFilter)
	{
		return unifiedNext(count, applyFilter, false);
	}
	
	private ArrayList<Type> unifiedNext(int count, boolean applyFilter, boolean fillNull)
	{
		ArrayList<Type> values = new ArrayList<>(count);
		for (int index = 0; index < count; index++)
		{
			if (atEnd())
			{
				if (fillNull)
					values.add(null);
				else
					return null;
			}
			else
				values.add(next(applyFilter));
		}
		return values;
	}
	
	/**
	 * Skips over the next value.
	 * <p>
	 *     Filters are applied by default.
	 * </p>
	 * @see #skip(boolean)
	 */
	public void skip()
	{
		skip(true);
	}
	
	public void skip(boolean applyFilters)
	{
		next(applyFilters);
	}
	
	/**
	 * Skips over multiple values.
	 * <p>
	 *     Filters will be applied by default.
	 * </p>
	 * @param count number of values to skip.
	 * @see #skip(int, boolean)    
	 */
	public void skip(int count)
	{
		skip(count, true);
	}
	
	/**
	 * Skips over multiple values.
	 * @param count number of values to skip.
	 * @param applyFilter whether filters should be applied.
	 * @see #skip(boolean)    
	 */
	public void skip(int count, boolean applyFilter)
	{
		for (int index = 0; index < count; index++)
			skip(applyFilter);
	}
	
	/**
	 * Copies this indexer.
	 * <p>
	 *     Creates a new indexer object with the same underlying indexable, at the same position,
	 *     and a copy if this indexer's filter state and state stack.
	 * </p>
	 * @return
	 */
	public Indexer<Type> copy()
	{
		Indexer<Type> indexer = new Indexer<>(indexable, index);
		indexer.filterStateStack.clear();
		for (Iterator<FilterState<Type>> iterator = filterStateStack.descendingIterator(); iterator.hasNext();)
		{
			indexer.filterStateStack.push(iterator.next());
		}
		return indexer;
	}
	
	public void skip(Type... filter)
	{
		skip(List.of(filter));
	}
	
	public void skip(Collection<Type> filter)
	{
		while (!atEnd() && filter.contains(get()))
			next();
	}
	
	/**
	 * Adds a new filter to the current filter state.
	 * @param filter
	 */
	public void addFilter(Filter<Type> filter)
	{
		filterStateStack.peek().filters.add(filter);
	}
	
	/**
	 * Removes a filter from the current filter state.
	 * @param filter
	 */
	public void removeFilter(Filter<Type> filter)
	{
		filterStateStack.peek().filters.remove(filter);
	}
	
	/**
	 * Push the current filter state to the stack.
	 * <p>
	 *     Creates a copy of the current state and places it at the top
	 *     of the filter stack.
	 * </p>
	 */
	public void pushFilterState()
	{
		filterStateStack.push(filterStateStack.peek().copy());
	}
	
	/**
	 * Pops the current filter state from the stack.
	 * <p>
	 *     The filter state at the top of the stack is removed. Leaving
	 *     the previous filter state as the new current state.<br>
	 *     If there is only one state on the stack, nothing happens.
	 * </p>
	 */
	public void popFilterState()
	{
		if (filterStateStack.size() > 1)
			filterStateStack.pop();
	}
	
	/**
	 * Checks if a value passes the current filters.
	 * @param value
	 */
	public boolean filterCheck(Type value)
	{
		for (int index = 0; index < filterStateStack.peek().filters.size(); index++)
		{
			Filter<Type> filter = filterStateStack.peek().filters.get(index);
			if (!filter.filter(value))
				return false;
		}
		return true;
	}
	
	/**
	 * Holds all the current filters in a confined state
	 * <p>
	 *     Allows a single indexer to have multiple states on a stack
	 * </p>
	 * @param <Type>
	 */
	static class FilterState<Type>
	{
		final ArrayList<Filter<Type>> filters = new ArrayList<>();
		
		protected FilterState<Type> copy()
		{
			FilterState<Type> state = new FilterState<>();
			state.filters.addAll(filters);
			return state;
		}
	}
	
	/**
	 * Used to automatically skip over unwanted values when indexing through an indexable.
	 * @param <FilterType>
	 */
	public interface Filter<FilterType>
	{
		/**
		 * Return false when the value should be skipped over.
		 */
		boolean filter(FilterType value);
	}
	
	/**
	 * Only allows values contained in the filter.
	 * @param <FilterType>
	 */
	public static class InclusionFilter<FilterType> implements Filter<FilterType>
	{
		private final Collection<FilterType> filter;
		
		public InclusionFilter(Collection<FilterType> filter)
		{
			this.filter = filter;
		}
		
		public InclusionFilter(FilterType... filter)
		{
			this(List.of(filter));
		}
		
		@Override
		public boolean filter(FilterType value)
		{
			return filter.contains(value);
		}
	}
	
	/**
	 * Only allows values not contained in the filter.
	 * @param <FilterType>
	 */
	public static class ExclusionFilter<FilterType> implements Filter<FilterType>
	{
		private final Collection<FilterType> filter;
		
		public ExclusionFilter(Collection<FilterType> filter)
		{
			this.filter = filter;
		}
		
		public ExclusionFilter(FilterType... filter)
		{
			this(List.of(filter));
		}
		
		@Override
		public boolean filter(FilterType value)
		{
			return !filter.contains(value);
		}
	}
}