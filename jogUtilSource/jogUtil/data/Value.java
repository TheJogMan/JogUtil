package jogUtil.data;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.command.argument.*;
import jogUtil.indexable.*;

import java.lang.reflect.*;
import java.util.*;

public abstract class Value<ValueType, ConsumptionType> extends Argument<ConsumptionType>
{
	/**
	 * Converts this value into a String
	 * @return
	 */
	public abstract String asString();
	
	/**
	 * Converts this value into bytes
	 * @return
	 */
	public abstract byte[] asBytes();
	
	/**
	 * Creates a new value containing the same data
	 * <p>
	 *     value.checkDataEquality(otherValue) should return true when given the result
	 *     of this method.
	 * </p>
	 * @return
	 */
	protected abstract Value<ValueType, ConsumptionType> makeCopy();
	
	/**
	 * Checks if two values contain the same data
	 * <p>
	 *     If a copy of this value is created with value.copy() and then passed to this method,
	 *     it should return true.
	 * </p>
	 * @param value
	 * @return
	 */
	protected abstract boolean checkDataEquality(Value<?, ?> value);
	
	private ValueType value = emptyValue();
	String name = null;
	Data parent = null;
	boolean persistent = false;
	ArrayList<ValueChangeListener<ValueType>> changeListeners = new ArrayList<>();
	
	public Value()
	{
	
	}
	
	public Value(ValueType initialValue)
	{
		set(initialValue);
	}
	
	public ValueType get()
	{
		return value;
	}
	
	public void set(ValueType value)
	{
		ValueType old = this.value;
		this.value = value;
		persistent = true;
		for (ValueChangeListener<ValueType> listener : changeListeners)
		{
			try
			{
				listener.change(old, value);
			}
			catch (Exception e)
			{
				System.err.println("Exception occurred while running change listener for value.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates a new value containing the same data
	 * <p>
	 *     value.checkDataEquality(otherValue) should return true when given the result
	 *     of this method.
	 * </p>
	 * @return
	 */
	public Value<ValueType, ConsumptionType> copy()
	{
		Value<ValueType, ConsumptionType> value = makeCopy();
		value.persistent = persistent;
		return value;
	}
	
	public Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte>
	setFromBytes(Indexer<Byte> source)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte> result;
		result = byteConsumer().consume(source);
		if (result.success())
			set(result.value().get());
		return result;
	}
	
	public Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character>
	setFromCharacters(Indexer<Character> source)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> result;
		result = characterConsumer().consume(source);
		if (result.success())
			set(result.value().get());
		return result;
	}
	
	public TypeRegistry.RegisteredType type()
	{
		return TypeRegistry.get(getClass());
	}
	
	public ValueType emptyValue()
	{
		try
		{
			return (ValueType)type().emptyValue.invoke(this);
		}
		catch (Exception ignored)
		{
			return null;
		}
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Byte> byteConsumer()
	{
		try
		{
			Method method = TypeRegistry.get(getClass()).byteConsumer;
			return (Consumer<Value<ValueType, ConsumptionType>, Byte>)method.invoke(null);
		}
		catch (Exception ignored)
		{
			return null;
		}
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Character> characterConsumer()
	{
		try
		{
			Method method = TypeRegistry.get(getClass()).characterConsumer;
			return (Consumer<Value<ValueType, ConsumptionType>, Character>)method.invoke(null);
		}
		catch (Exception ignored)
		{
			return null;
		}
	}
	
	/**
	 * Adds a new change listener to this value
	 * <p>
	 *     Returns the listener back.
	 * </p>
	 * @param listener
	 * @return
	 */
	public ValueChangeListener<ValueType> addChangeListener(ValueChangeListener<ValueType> listener)
	{
		changeListeners.add(listener);
		return listener;
	}
	
	public void removeChangeListener(ValueChangeListener<ValueType> listener)
	{
		changeListeners.remove(listener);
	}
	
	/**
	 * Used as a callback whenever a value is changed
	 * @param <ValueType>
	 */
	public static interface ValueChangeListener<ValueType>
	{
		void change(ValueType oldValue, ValueType newValue);
	}
	
	@Override
	protected final void initArgument(Object[] data)
	{
		//this shouldn't be needed for value arguments, so by overriding it here
		//we are making sure that anyone implementing a Value is able to ignore it
	}
	
	@Override
	public final ReturnResult<ConsumptionType> interpretArgument(Indexer<Character> source,
																 Executor executor)
	{
		//TODO implement
		return new ReturnResult<>();
	}
	
	@Override
	public final String toString()
	{
		return asString();
	}
	
	@Override
	public final boolean equals(Object object)
	{
		if (object instanceof Value<?, ?>)
			return (checkDataEquality((Value<?, ?>)object));
		else
			return false;
	}
}