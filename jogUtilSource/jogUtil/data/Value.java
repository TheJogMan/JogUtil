package jogUtil.data;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.indexable.*;

import java.util.*;

public abstract class Value<ValueType, ConsumptionType> implements Argument<ValueType>
{
	/**
	 * Provides a default value intended for initializing new values.
	 * @return
	 */
	public abstract ValueType emptyValue();
	/**
	 * Converts this value into a String
	 * <p>
	 *     Important to keep in mind that when part of a Data object, indentation tabs will be inserted
	 *     after each new line in this value's string representation.<br>
	 *     The character parser needs to be able to account for these.
	 * </p>
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
	
	ValueType value = emptyValue();
	String name = null;
	Data parent = null;
	boolean persistent = false;
	final ArrayList<Value.ValueChangeListener<ValueType>> changeListeners = new ArrayList<>();
	
	public Value()
	{
		this(new Object[0]);
	}
	
	public Value(Object[] initData)
	{
		initArgument(initData);
	}
	
	public Value(ValueType initialValue)
	{
		this();
		set(initialValue);
	}
	
	public ValueType get()
	{
		return value;
	}
	
	/**
	 * Used for setting the value of a CompoundArgumentValue from its character consumer
	 * @param value
	 */
	Result internalSet(Object value)
	{
		try
		{
			set((ValueType)value);
			return new Result();
		}
		catch (ClassCastException e)
		{
			return new Result("Could not cast to value type");
		}
		catch (Exception e)
		{
			return new Result("Exception occurred: " + Result.describeThrowableFull(e));
		}
	}
	
	public void set(ValueType value)
	{
		if (value == null)
			throw new IllegalArgumentException("Null values are not supported.");
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
	
	public Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte> setFromBytes(Indexer<Byte> source)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte> result = byteConsumer().consume(source);
		if (result.success())
			set(result.value().get());
		return result;
	}
	
	public Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> setFromCharacters(Indexer<Character> source)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> result = characterConsumer().consume(source);
		if (result.success())
			set(result.value().get());
		return result;
	}
	
	public TypeRegistry.RegisteredType<?, ?> type()
	{
		return TypeRegistry.get(getClass());
	}
	
	public TypeRegistry.RegisteredPlainType<ValueType, ConsumptionType> plainType()
	{
		return (TypeRegistry.RegisteredPlainType)type();
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Byte> byteConsumer()
	{
		return byteConsumer(new Object[0]);
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Byte> byteConsumer(Object[] data)
	{
		Consumer<?, Byte> consumer = type().byteConsumer(data);
		return (Consumer<Value<ValueType, ConsumptionType>, Byte>)consumer;
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Character> characterConsumer()
	{
		return characterConsumer(new Object[0]);
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Character> characterConsumer(Object[] data)
	{
		Consumer<?, Character> consumer = type().characterConsumer(data);
		return (Consumer<Value<ValueType, ConsumptionType>, Character>)consumer;
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
	public ReturnResult<ValueType> interpretArgument(Indexer<Character> source, Executor executor, Object[] data)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> result;
		result = characterConsumer(data).consume(source);
		if (result.success())
			return new ReturnResult<>(result.value().get());
		else
			return new ReturnResult<>(result.description());
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
	
	private final ArrayList<ExecutorFilter.Filter> filters = new ArrayList<>();
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
}