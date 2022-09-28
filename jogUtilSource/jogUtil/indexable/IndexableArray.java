package jogUtil.indexable;

public class IndexableArray<Type> extends Indexable<Type>
{
	Type[] values;
	
	public IndexableArray(int size)
	{
		values = (Type[])(new Object[size]);
	}
	
	public IndexableArray(Type[] values)
	{
		this.values = values;
	}
	
	@Override
	public Type get(int index)
	{
		return values[index];
	}
	
	@Override
	public void set(int index, Type value)
	{
		values[index] = value;
	}
	
	@Override
	public boolean complete()
	{
		return true;
	}
	
	@Override
	public int size()
	{
		return values.length;
	}
	
	@Override
	public boolean contains(Object o)
	{
		for (Type value : values)
		{
			if (value.equals(o))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean add(Type type)
	{
		return false;
	}
	
	@Override
	public boolean remove(Object o)
	{
		return false;
	}
	
	@Override
	public void clear()
	{
	
	}
}