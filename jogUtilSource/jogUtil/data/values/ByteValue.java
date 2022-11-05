package jogUtil.data.values;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.util.*;

public class ByteValue extends Value<Byte, Byte>
{
	public ByteValue(Byte byt)
	{
		super(byt);
	}
	
	public ByteValue(Object[] initData)
	{
		super(initData);
	}
	
	public ByteValue()
	{
		super();
	}
	
	@Override
	public Byte emptyValue()
	{
		return 0;
	}
	
	@Override
	public String asString()
	{
		return "" + get();
	}
	
	@Override
	public byte[] asBytes()
	{
		return new byte[] {get()};
	}
	
	@Override
	protected Value<Byte, Byte> makeCopy()
	{
		return new ByteValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof ByteValue && Objects.equals(((ByteValue) value).get(), get());
	}
	
	@Override
	public String defaultName()
	{
		return "Byte";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public void initArgument(Object[] args)
	{
	
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, Byte>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			if (source.atEnd())
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new ByteValue(source.next()), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Byte>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeCharacters(source, numericCharacters);
			try
			{
				return new Consumer.ConsumptionResult<>(new ByteValue(Byte.parseByte(string)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid number format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Byte>[] validationValues()
	{
		return new ByteValue[] {
				new ByteValue((byte)-127),
				new ByteValue((byte)127),
				new ByteValue((byte)0)
		};
	}
	
	public static final char[] numericCharacters = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'};
	
	/**
	 * Toggles a bit within a byte
	 * @param value the byte to work with
	 * @param index the index of the bit to toggle (0-7)
	 * @return the byte with the bit toggled
	 */
	public static byte toggleBit(byte value, int index)
	{
		if (index >= 0 && index <= 7)
		{
			return setBit(value, index, !getBit(value, index));
		}
		else
		{
			throw new IllegalStateException("Index not in valid range: Expected 0-7, got " + index);
		}
	}
	
	/**
	 * Toggles a bit within a byte
	 * @param value the byte to work with
	 * @param index the index of the bit to set (0-7)
	 * @param state the new state of the bit
	 * @return the byte with the bit set
	 */
	public static byte setBit(byte value, int index, boolean state)
	{
		if (index >= 0 && index <= 7)
		{
			if (index == 0)
			{
				value = (byte)((value >>> 1) << 1);
				if (state)
				{
					value = (byte)(value | 1);
				}
			}
			else if (index == 7)
			{
				byte tempValue = (byte)(value >>> 7);
				value = (byte)(value - tempValue);
				if (state)
				{
					value = (byte)(value | (1 << 7));
				}
			}
			else
			{
				byte tempVal1 = (byte)((value >>> index + 1) << index + 1);
				byte tempVal2 = (byte)((value >>> index) << index);
				value = (byte)(tempVal1 | tempVal2);
				if (state)
				{
					value = (byte)(value | (1 << index - 1));
				}
			}
			return value;
		}
		else
		{
			throw new IllegalStateException("BIndex not in valid range: Expected 0-7, got " + index);
		}
	}
	
	/**
	 * Toggles a bit within a byte
	 * @param value the byte to work with
	 * @param index the index of the bit to get (0-7)
	 * @return the state of the bit
	 */
	public static boolean getBit(byte value, int index)
	{
		if (index >= 0 && index <= 7)
		{
			if (index == 0)
			{
				byte tempVal = (byte)(value >>> 1);
				value -= (tempVal << 1);
			}
			else if (index == 7)
			{
				value = (byte)(value >>> 7);
			}
			else
			{
				byte tempVal1 = (byte)((value >>> index + 1) << index + 1);
				byte tempVal2 = (byte)((value >>> index) << index);
				value = (byte) (value - tempVal1);
				value = (byte) (value - tempVal2);
				value = (byte)(value >>> index - 1);
			}
			return (value == 1);
		}
		else
		{
			throw new IllegalStateException("Index not in valid range: Expected 0-7, got " + index);
		}
	}
	
	public static byte buildByte(boolean bit0, boolean bit1, boolean bit2, boolean bit3, boolean bit4, boolean bit5, boolean bit6, boolean bit7)
	{
		byte byt = 0;
		byt = setBit(byt, 0, bit0);
		byt = setBit(byt, 1, bit1);
		byt = setBit(byt, 2, bit2);
		byt = setBit(byt, 3, bit3);
		byt = setBit(byt, 4, bit4);
		byt = setBit(byt, 5, bit5);
		byt = setBit(byt, 6, bit6);
		byt = setBit(byt, 7, bit7);
		return byt;
	}
}