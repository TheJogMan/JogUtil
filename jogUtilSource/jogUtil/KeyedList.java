package jogUtil;

import java.util.*;

public class KeyedList<KeyType, ValueType>
{
	final ArrayList<KeyType> keys = new ArrayList<>();
	final ArrayList<ValueType> values = new ArrayList<>();
	
	public void put(KeyType key, ValueType value)
	{
		int index = indexOf(key);
		if (index == -1)
		{
			keys.add(key);
			values.add(value);
		}
		else
		{
			keys.set(index, key);
			values.set(index, value);
		}
	}
	
	public void remove(KeyType key)
	{
		int index = indexOf(key);
		if (index != -1)
		{
			keys.remove(index);
			values.remove(index);
		}
	}
	
	public int indexOf(KeyType key)
	{
		return keys.indexOf(key);
	}
	
	public ValueType get(KeyType key)
	{
		int index = indexOf(key);
		if (index == -1)
			return null;
		else
			return values.get(index);
	}
	
	public boolean containsKey(KeyType key)
	{
		return keys.contains(key);
	}
	
	public int size()
	{
		return keys.size();
	}
	
	public KeyedEntry<KeyType, ValueType> get(int index)
	{
		if (index >= 0 && index < size())
			return new KeyedEntry<>(keys.get(index), values.get(index));
		else
			return null;
	}
	
	public static class KeyedEntry<Key, Value>
	{
		private KeyedEntry(Key key, Value value)
		{
			this.key = key;
			this.value = value;
		}
		
		final Key key;
		final Value value;
		
		public Key getKey()
		{
			return key;
		}
		
		public Value getValue()
		{
			return value;
		}
	}
	
	public KeyType[] keys()
	{
		return (KeyType[])keys.toArray();
	}
	
	public Iterator<ValueType> iterator()
	{
		return values.iterator();
	}
}