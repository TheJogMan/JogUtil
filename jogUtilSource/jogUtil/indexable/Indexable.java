package jogUtil.indexable;

import java.util.*;

public abstract class Indexable<Type> implements Collection<Type>
{
	/**
	 * Gets the value at the given index
	 * @param index
	 * @return
	 */
	public abstract Type get(int index);
	
	/**
	 * Sets the value at the given index
	 * @param index
	 * @param value
	 */
	public abstract void set(int index, Type value);
	
	/**
	 * Checks if this Indexable is complete
	 * <p>
	 *     If the indexable is not complete, then more data might be added in the future
	 * </p>
	 * @return
	 * @see #waitForData()
	 */
	public abstract boolean complete();
	
	/**
	 * This class is used for synchronization when threads wait for data to be added.
	 */
	private static class WaitingPoint
	{
		boolean hasWaiting = false;
	}
	
	private final WaitingPoint waitingPoint = new WaitingPoint();
	
	/**
	 * Causes the current thread to yield until more data is available.
	 * <p>
	 *     New data should be available once the thread resumes, but this can't be guaranteed.  While not required, it is
	 *     recommended to verify that new data is available before trying to access it.<br>
	 *     <br>
	 *     If the indexable is complete the thread will not yield and this method will return immediately.
	 * </p>
	 */
	public final void waitForData()
	{
		synchronized(waitingPoint)
		{
			if (!complete())
			{
				waitingPoint.hasWaiting = true;
				//if the thread is awoken for any reason other than wakeWaiters() being called, we want to go back to waiting
				while (waitingPoint.hasWaiting)
				{
					try
					{
						waitingPoint.wait();
					}
					catch (InterruptedException ignored)
					{
					
					}
				}
			}
		}
	}
	
	/**
	 * Wakes all the threads that are currently waiting for new data to be added.
	 * <p>
	 *     If this is called without adding new data first, it may result in undesirable behavior in the waiting threads
	 *     if they are blindly assuming that new data will always be available after they have been awoken.  However this
	 *     can not be avoided if you no longer intend to add more data and need to wake any threads that are still trying
	 *     to wait for more.
	 * </p>
	 */
	protected final void wakeWaiters()
	{
		synchronized(waitingPoint)
		{
			waitingPoint.hasWaiting = false;
			waitingPoint.notifyAll();
		}
	}
	
	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	@Override
	public Indexer<Type> iterator()
	{
		return new Indexer<>(this);
	}
	
	@Override
	public Object[] toArray()
	{
		Object[] values = new Object[size()];
		for (int index = 0; index < size(); index++)
		{
			values[index] = get(index);
		}
		return values;
	}
	
	@Override
	public <ArrayType> ArrayType[] toArray(ArrayType[] values)
	{
		for (int index = 0; index < size(); index++)
		{
			values[index] = (ArrayType)get(index);
		}
		return values;
	}
	
	@Override
	public boolean containsAll(Collection<?> collection)
	{
		for (Object value : collection)
		{
			if (!contains(value))
				return false;
		}
		return true;
	}
	
	/**
	 * Returns true if all the given values are contained in the indexable
	 * @param values
	 * @return
	 */
	public boolean containsAll(Type[] values)
	{
		boolean contained = false;
		for (Type value : values)
		{
			if (!contains(value))
				return false;
		}
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends Type> collection)
	{
		boolean changed = false;
		for (Type type : collection)
		{
			if (add(type))
				changed = true;
		}
		return changed;
	}
	
	/**
	 * Adds all the given values
	 * @param values
	 * @return Whether this operation changed the indexable
	 */
	public boolean addAll(Type[] values)
	{
		boolean changed = false;
		for (Type value : values)
		{
			if (add(value))
				changed = true;
		}
		return changed;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection)
	{
		boolean changed = false;
		for (Object value : collection)
		{
			if (remove(value))
				changed = true;
		}
		return changed;
	}
	
	/**
	 * Removes all the given values
	 * @param values
	 * @return Whether this operation changed the indexable
	 */
	public boolean removeAll(Type[] values)
	{
		boolean changed = false;
		for (Type value : values)
		{
			if (remove(value))
				changed = true;
		}
		return changed;
	}
	
	@Override
	public boolean retainAll(Collection<?> collection)
	{
		ArrayList<Type> toBeRemoved = new ArrayList<>();
		for (Type value : this)
		{
			if (!collection.contains(value))
				toBeRemoved.add(value);
		}
		return removeAll(toBeRemoved);
	}
	
	/**
	 * Removes everything except for the given values
	 * @param values
	 * @return Whether this operation changed the indexable
	 */
	public boolean retainAll(Type[] values)
	{
		ArrayList<Type> toBeRemoved = new ArrayList<>();
		for (Type value : this)
		{
			boolean contained = false;
			for (Type check : values)
			{
				if (check.equals(value))
				{
					contained = true;
					break;
				}
			}
			if (!contained)
				toBeRemoved.add(value);
		}
		return removeAll(toBeRemoved);
	}
}