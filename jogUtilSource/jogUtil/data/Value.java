package jogUtil.data;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

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
	
	public TypeRegistry.RegisteredType type()
	{
		return TypeRegistry.get(getClass());
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Byte> byteConsumer()
	{
		Consumer<?, Byte> consumer = type().byteConsumer();
		return (Consumer<Value<ValueType, ConsumptionType>, Byte>)consumer;
	}
	
	public Consumer<Value<ValueType, ConsumptionType>, Character> characterConsumer()
	{
		Consumer<?, Character> consumer = type().characterConsumer();
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
	public ReturnResult<ValueType> interpretArgument(Indexer<Character> source, Executor executor)
	{
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> result;
		result = characterConsumer().consume(source);
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
	
	//this is an internal method used during validation
	Result validate()
	{
		//null values shouldn't be supported
		ValueType value = get();
		if (value == null)
			return new Result("Value is null.");
		
		//ensure that setting and getting values is consistent
		set(value);
		if (!value.equals(get()))
			return new Result("Getting value produced a result that didn't match the set value.");
		
		//ensure that copying a value doesn't throw an exception
		Value<ValueType, ConsumptionType> second;
		try
		{
			second = copy();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while making a copy of the value: " + Result.describeThrowableFull(e));
		}
		
		//ensure that instances with the same value will equal each other
		boolean equal;
		try
		{
			equal = equals(second);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while checking data equality: " + Result.describeThrowableFull(e));
		}
		if (!equal)
			return new Result("Two instances were not equal despite having the same value.");
		
		//ensure that converting to string works as expected
		String string;
		try
		{
			string = asString();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while converting to string: " + Result.describeThrowableFull(e));
		}
		if (string == null)
			return new Result("asString can not return null.");
		
		//ensure that interpreting character data produces the original value while consuming the correct
		//amount of data
		//we add an extra character to the end to make sure the interpreter doesn't consume too much data
		string += ' ';
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Character> characterResult;
		Indexer<Character> characterSource = StringValue.indexer(string);
		try
		{
			characterResult = second.setFromCharacters(characterSource);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while parsing character data: " + Result.describeThrowableFull(e));
		}
		if (characterResult == null)
			return new Result("Character consumer can not return a null result.");
		if (!characterResult.success())
			return new Result(RichStringBuilder.start("Character consumption failed on data that should have been valid: ").append(characterResult.description()).build());
		//make sure that the extra character we added earlier wasn't consumed, and is the only character
		//that wasn't consumed.
		if (characterSource.atEnd())
			return new Result("Character consumer has consumed too much data.");
		characterSource.next();
		if (!characterSource.atEnd())
			return new Result("Character consumer has not consumed enough data.");
		if (!equals(second))
			return new Result("String conversion did not produce an equal value.");
		
		//ensure that converting to byte data works as expected
		byte[] byteData;
		try
		{
			byteData = asBytes();
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while converting to byte data: " + Result.describeThrowableFull(e));
		}
		if (byteData == null)
			return new Result("asBytes can not return null.");
		
		//ensure that interpreting byte data produces the original value while consuming the correct
		//amount of data
		//we add an extra byte to the end to make sure the interpreter doesn't consume too much data
		ByteArrayBuilder builder = new ByteArrayBuilder();
		builder.add(byteData);
		builder.add((byte)0);
		byteData = builder.toPrimitiveArray();
		Consumer.ConsumptionResult<Value<ValueType, ConsumptionType>, Byte> byteResult;
		Indexer<Byte> byteSource = ByteArrayBuilder.indexer(byteData);
		try
		{
			byteResult = second.setFromBytes(byteSource);
		}
		catch (Exception e)
		{
			return new Result("Exception occurred while parsing byte data: " + Result.describeThrowableFull(e));
		}
		if (byteResult == null)
			return new Result("Byte consumer can not return a null result.");
		if (!byteResult.success())
			return new Result(RichStringBuilder.start("Byte consumption failed on data that should have been valid: ").append(byteResult.description()).build());
		//make sure that the extra byte we added earlier wasn't consumed, and is the only byte
		//that wasn't consumed.
		if (byteSource.atEnd())
			return new Result("Byte consumer has consumed too much data.");
		byteSource.next();
		if (!byteSource.atEnd())
			return new Result("Byte consumer has not consumed enough data.");
		if (!equals(second))
			return new Result("Byte conversion did not produce an equal value.");
		
		//all checks have passed
		return new Result();
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
}