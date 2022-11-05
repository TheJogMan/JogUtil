package jogUtil.data.values;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class CharacterValue extends Value<Character, Character>
{
	public CharacterValue()
	{
		super();
	}
	
	public CharacterValue(Object[] initData)
	{
		super(initData);
	}
	
	public CharacterValue(Character character)
	{
		super(character);
	}
	
	@Override
	public Character emptyValue()
	{
		return ' ';
	}
	
	@Override
	public String asString()
	{
		return "" + get();
	}
	
	/**
	 * Encodes a character into bytes using UTF-32
	 * @return
	 * @see #toByteData(Character)
	 */
	@Override
	public byte[] asBytes()
	{
		return toByteData(get());
	}
	
	@Override
	protected Value<Character, Character> makeCopy()
	{
		return new CharacterValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof CharacterValue && ((CharacterValue)value).get().equals(get());
	}
	
	@Override
	public String defaultName()
	{
		return "Character";
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
	public static Consumer<Value<?, Character>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			List<Byte> bytes = source.allNext(4);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new CharacterValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, Character>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			if (source.atEnd())
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			else
				return new Consumer.ConsumptionResult<>(new CharacterValue(source.next()), source);
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, Character>[] validationValues()
	{
		return new CharacterValue[] {
			new CharacterValue('A'),
			new CharacterValue('\n'),
			new CharacterValue('\\'),
			new CharacterValue('"')
		};
	}
	
	/**
	 * Encodes a character into bytes using UTF-32
	 * @param value
	 * @return
	 */
	public static byte[] toByteData(Character value)
	{
		return Charset.forName("UTF-32").encode("" + value).array();
	}
	
	/**
	 * Decodes a character using UTF-32
	 * @param byteData
	 * @return
	 */
	public static Character fromByteData(byte[] byteData)
	{
		return Charset.forName("UTF-32").decode(ByteBuffer.wrap(byteData)).charAt(0);
	}
	
	/**
	 * Decodes a character using UTF-32
	 * @param byteData
	 * @return
	 * @see #fromByteData(byte[])
	 */
	public static Character fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(byteData.toArray(new Byte[0]));
	}
	
	/**
	 * Decodes a character using UTF-32
	 * @param byteData
	 * @return
	 * @see #fromByteData(byte[])
	 */
	public static Character fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	public static Character[] toObjectArray(char[] characters)
	{
		Character[] objects = new Character[characters.length];
		for (int index = 0; index < characters.length; index++)
			objects[index] = characters[index];
		return objects;
	}
	
	public static char[] toPrimitiveArray(Character[] objects)
	{
		char[] characters = new char[objects.length];
		for (int index = 0; index < characters.length; index++)
			characters[index] = objects[index];
		return characters;
	}
	
	public static boolean containsChar(char ch, char[] filter)
	{
		for (char c : filter)
		{
			if (c == ch)
				return true;
		}
		return false;
	}
	
	public static boolean containsChar(char ch, Character[] filter)
	{
		for (Character character : filter)
		{
			if (character == ch)
				return true;
		}
		return false;
	}
}