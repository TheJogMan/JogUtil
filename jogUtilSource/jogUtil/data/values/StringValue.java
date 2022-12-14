package jogUtil.data.values;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class StringValue extends Value<String, String>
{
	public StringValue(String value)
	{
		super(value);
	}
	
	public StringValue(Object[] initData)
	{
		super(initData);
	}
	
	public StringValue()
	{
		super();
	}
	
	@Override
	public String defaultName()
	{
		return "String";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		ArrayList<String> completion = new ArrayList<>();
		completion.add("\"");
		if (source.atEnd())
			return completion;
		else
		{
			if (source.next() == '"')
			{
				boolean escaped = false;
				while (!source.atEnd())
				{
					char ch = source.next();
					if (escaped)
						escaped = false;
					else if (ch == '\\')
						escaped = true;
					else if (ch == '"')
						return null;
				}
				return completion;
			}
			else
				return null;
		}
	}
	
	@Override
	public String emptyValue()
	{
		return "";
	}
	
	@Override
	public String asString()
	{
		return pack(get());
	}
	
	/**
	 * Encodes a string using UTF-8
	 * @return
	 * @see #toByteData(String)
	 */
	@Override
	public byte[] asBytes()
	{
		return toByteData(get());
	}
	
	@Override
	protected Value<String, String> makeCopy()
	{
		return new StringValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof StringValue && ((StringValue)value).get().compareTo(get()) == 0;
	}
	
	@Override
	public void initArgument(Object[] args)
	{
	
	}
	
	public static Consumer.ConsumptionResult<String, Byte> primitiveByteConsume(Indexer<Byte> source)
	{
		Consumer.ConsumptionResult<Value<?, String>, Byte> result = getByteConsumer().consume(source);
		if (result.success())
			return new Consumer.ConsumptionResult<>((String)result.value().get(), source,  result.description());
		else
			return new Consumer.ConsumptionResult<>(source, result.description());
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, String>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ByteArrayBuilder builder = new ByteArrayBuilder();
			boolean stringEnded = false;
			while (!source.atEnd() && !stringEnded)
			{
				byte byt = source.next();
				if (byt == 0)
					stringEnded = true;
				else
					builder.add(byt);
			}
			return new Consumer.ConsumptionResult<>(new StringValue(
					fromByteData(builder.toPrimitiveArray())), source);
		};
	}
	
	public static Consumer.ConsumptionResult<String, Character> primitiveCharacterConsume(Indexer<Character> source)
	{
		Consumer.ConsumptionResult<Value<?, String>, Character> result = getCharacterConsumer().consume(source);
		if (result.success())
			return new Consumer.ConsumptionResult<>((String)result.value().get(), source,  result.description());
		else
			return new Consumer.ConsumptionResult<>(source, result.description());
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, String>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			if (!source.atEnd() && source.get() == '"')
			{
				StringBuilder builder = new StringBuilder();
				builder.append(source.next());
				boolean unsanitary = false;
				boolean closed = false;
				while(!source.atEnd())
				{
					char ch = source.next();
					if (unsanitary)
					{
						builder.append(ch);
						unsanitary = false;
					}
					else
					{
						if (ch == '\\')
						{
							builder.append(ch);
							unsanitary = true;
						}
						else if (ch == '"')
						{
							builder.append(ch);
							closed = true;
							break;
						}
						else
							builder.append(ch);
					}
				}
				if (!closed)
					return new Consumer.ConsumptionResult<>(source, "Must end with \"");
				else
				{
					return new Consumer.ConsumptionResult<>(new StringValue(unpack(builder.toString())), source);
				}
			}
			else
				return new Consumer.ConsumptionResult<>(source, "Must begin with '\"', got '" + sanitize("" + source.get()) + "'");
		};
	}
	
	/**
	 * Encodes a string using UTF-8
	 * @param string
	 * @return
	 */
	public static byte[] toByteData(String string)
	{
		if (string == null || string.length() == 0)
			return new byte[1];
		else
		{
			byte[] data = StandardCharsets.UTF_8.encode(string).array();
			//apparently we sometimes seem to get two 0 bytes at the end of the array
			//so to ensure consistency we check for this case and remove the excess bytes
			if (data.length > 1 && data[data.length - 2] == 0)
			{
				ByteArrayBuilder builder = new ByteArrayBuilder(data);
				while (builder.size() > 1 && builder.get(builder.size() - 2) == 0)
					builder.remove(builder.size() - 2);
				data = builder.toPrimitiveArray();
			}
			//it's apparently possible for a 0 byte to not be added at the end
			//so let's ensure we have one
			if (data.length == 0 || data[data.length - 1] != 0)
			{
				ByteArrayBuilder builder = new ByteArrayBuilder(data);
				builder.add((byte)0);
				data = builder.toPrimitiveArray();
			}
			return data;
		}
	}
	
	/**
	 * Decodes a string using UTF-8
	 * @param byteData
	 * @return
	 */
	public static String fromByteData(byte[] byteData)
	{
		if (byteData == null || byteData.length == 0 || (byteData.length == 1 && byteData[0] == 0))
			return "";
		else
			return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(byteData)).toString();
	}
	
	/**
	 * Decodes a string using UTF-8
	 * @param byteData
	 * @return
	 * @see #fromByteData(byte[])
	 */
	public static String fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	/**
	 * Decodes a string using UTF-8
	 * @param byteData
	 * @return
	 * @see #fromByteData(byte[])
	 */
	public static String fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(byteData.toArray(new Byte[0]));
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, String>[] validationValues()
	{
		return new StringValue[] {
				new StringValue("Hello World!"),
				new StringValue("Line 1\nLine 2"),
				new StringValue("Look at that slide \\, oh my.")
		};
	}
	
	private static final char[][] unsanitaryCharacters = {
		{'"', '"'},
		{'\\', '\\'},
		{'\n', 'n'},
		{'\r', 'r'},
		{'\t', 't'}
	};
	
	/**
	 * Adds escape characters to any unsafe characters
	 * <p>
	 *     unsafe characters are: ", \, \n, \r, \t
	 * </p>
	 * @param string
	 * @return
	 */
	public static String sanitize(String string)
	{
		StringBuilder newString = new StringBuilder();
		for (int index = 0; index < string.length(); index++)
		{
			char ch = string.charAt(index);
			boolean wasSanitary = true;
			for (char[] unsanitaryCharacter : unsanitaryCharacters)
			{
				if (ch == unsanitaryCharacter[0])
				{
					newString.append('\\').append(unsanitaryCharacter[1]);
					wasSanitary = false;
					break;
				}
			}
			if (wasSanitary)
				newString.append(ch);
		}
		return newString.toString();
	}
	
	/**
	 * Removes escape characters.
	 * @param string
	 * @return
	 */
	public static String desanitize(String string)
	{
		StringBuilder newString = new StringBuilder();
		boolean unsanitary = false;
		for (int index = 0; index < string.length(); index++)
		{
			char ch = string.charAt(index);
			if (unsanitary)
			{
				for (char[] unsanitaryCharacter : unsanitaryCharacters)
				{
					if (ch == unsanitaryCharacter[1])
					{
						newString.append(unsanitaryCharacter[0]);
						break;
					}
				}
				unsanitary = false;
			}
			else
			if (ch == '\\')
				unsanitary = true;
			else
				newString.append(ch);
		}
		return newString.toString();
	}
	
	/**
	 * Appends double quotes '"' to the beginning and end of the string.
	 * <p>
	 *     This will also sanitize the string.
	 * </p>
	 * @param string
	 * @return
	 * @see #sanitize(String) 
	 */
	public static String pack(String string)
	{
		return '"' + sanitize(string) + '"';
	}
	
	/**
	 * Removes double quotes '"' from the beginning and end of a string.
	 * <p>
	 *     This will also desanitize the string.
	 * </p>
	 * @param string
	 * @return
	 * @see #desanitize(String)
	 */
	public static String unpack(String string)
	{
		if (string.length() > 1 && string.charAt(0) == '"' && string.charAt(string.length() - 1) == '"')
		{
			if (string.length() == 2)
				return "";
			else
				return desanitize(string.substring(1, string.length() - 1));
		}
		else
			return null;
	}
	
	public static Indexer<Character> indexer(String string)
	{
		IndexableArray<Character> array = new IndexableArray<>(string.length());
		for (int index = 0; index < string.length(); index++)
		{
			array.set(index, string.charAt(index));
		}
		return array.iterator();
	}
	
	/**
	 * Consumes characters into a string until it encounters the endMarker
	 * <p>
	 *     The endMarker character will not be included in the resulting string.
	 * </p>
	 * @param source
	 * @param endMarker
	 * @return
	 */
	public static String consumeString(Indexer<Character> source, char endMarker)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd() && source.get() != endMarker)
			builder.append(source.next());
		return builder.toString();
	}
	
	/**
	 * Consumes characters into a string until it encounters a character that is not in the filter
	 * @param source
	 * @param filter
	 * @return
	 */
	public static String consumeCharacters(Indexer<Character> source, char[] filter)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd() && CharacterValue.containsChar(source.get(), filter))
			builder.append(source.next());
		return builder.toString();
	}
	
	/**
	 * Consumes all the remaining characters into a string.
	 * @param source
	 * @return
	 */
	public static String consumeString(Indexer<Character> source)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd())
			builder.append(source.next());
		return builder.toString();
	}
	
	public static String consumeAlphabeticalString(Indexer<Character> source)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd() && Character.isLetter(source.get()))
			builder.append(source.next());
		return builder.toString();
	}
	
	public static String consumeNumericString(Indexer<Character> source)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd() && Character.isDigit(source.get()))
			builder.append(source.next());
		return builder.toString();
	}
	
	/**
	 * Consumes an amount of characters into a string.
	 * <p>
	 *     If all characters are required but there aren't enough to consume all of them, null is returned
	 * </p>
	 * @param source
	 * @param amount
	 * @param requireAll
	 * @return
	 */
	public static String consumeString(Indexer<Character> source, int amount, boolean requireAll)
	{
		StringBuilder builder = new StringBuilder();
		while (!source.atEnd() && builder.length() < amount)
			builder.append(source.next());
		if (requireAll && builder.length() < amount)
			return null;
		return builder.toString();
	}
	
	/**
	 * Consumes an amount of characters into a string
	 * <p>
	 *     If not all characters are available, then the string will only contain those that were.
	 * </p>
	 * @param source
	 * @param amount
	 * @return
	 * @see #consumeString(Indexer, int, boolean)
	 */
	public static String consumeString(Indexer<Character> source, int amount)
	{
		return consumeString(source, amount, false);
	}
	
	/**
	 * Checks if the given string is what appears next in the source
	 * <p>
	 *     Will consume the sequence from the source if it matches.
	 * </p>
	 * @param sequence
	 * @param caseSensitive
	 * @return
	 */
	public static boolean consumeSequence(Indexer<Character> source, String sequence, boolean caseSensitive)
	{
		int position = source.position();
		int index = 0;
		if (!caseSensitive)
			sequence = sequence.toLowerCase();
		while (!source.atEnd())
		{
			char ch = source.next();
			if (!caseSensitive)
				ch = Character.toLowerCase(ch);
			if (sequence.charAt(index) == ch)
				index++;
			else
				break;
			if (index == sequence.length())
				return true;
		}
		source.setPosition(position);
		return false;
	}
	
	/**
	 * Checks if the given string is what appears next in the source
	 * <p>
	 *     Will consume the sequence from the source if it matches.
	 * </p>
	 * @param sequence
	 * @return
	 * @see #consumeSequence(Indexer, String, boolean)
	 */
	public static boolean consumeSequence(Indexer<Character> source, String sequence)
	{
		return consumeSequence(source, sequence, true);
	}
}