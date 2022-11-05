package jogUtil.indexable;

import java.util.*;

public class IndexableList<Type> extends Indexable<Type>
{
	final List<Type> list;
	boolean finished = false;
	
	public IndexableList()
	{
		list = new ArrayList<>();
	}
	
	public IndexableList(List<Type> list)
	{
		this();
		this.list.addAll(list);
	}
	
	@Override
	public Type get(int index)
	{
		return null;
	}
	
	@Override
	public void set(int index, Type value)
	{
		list.set(index, value);
	}
	
	@Override
	public boolean complete()
	{
		return finished;
	}
	
	/**
	 * Marks this indexable as complete.
	 * <p>
	 *     It will no longer be possible to add and remove values.
	 * </p>
	 */
	public void finish()
	{
		finished = true;
		wakeWaiters();
	}
	
	@Override
	public int size()
	{
		return list.size();
	}
	
	@Override
	public boolean contains(Object o)
	{
		return list.contains(o);
	}
	
	@Override
	public boolean add(Type type)
	{
		if (!finished)
		{
			boolean changed = list.add(type);
			if (changed)
				wakeWaiters();
			return changed;
		}
		else
			return false;
	}
	
	@Override
	public boolean remove(Object o)
	{
		if (!finished)
			return list.remove(o);
		else
			return false;
	}
	
	@Override
	public void clear()
	{
		if (!finished)
			list.clear();
	}
}