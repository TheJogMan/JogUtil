package jogUtil.richText;

import java.util.*;

public enum EncodingType
{
	CODED((style, prefix) ->
	{
		if (prefix)
		{
			String code = "";
			if (style.bold())
				code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeBold;
			if (style.italic())
				code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeItalic;
			if (style.underlined())
				code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeUnderline;
			if (style.strikethrough())
				code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeStrikethrough;
			if (style.obfuscated())
				code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeObfuscate;
			code += style.color.toInlineCode();
			return code;
		}
		else
			return RichColor.characterCodePrefix + "" + RichColor.characterCodeReset;
	},
	(from, to) ->
	{
		String code = "";
		if (from.bold() != to.bold())
			code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeBold;
		if (from.italic() != to.italic())
			code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeItalic;
		if (from.underlined() != to.underlined())
			code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeUnderline;
		if (from.strikethrough() != to.strikethrough())
			code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeStrikethrough;
		if (from.obfuscated() != to.obfuscated())
			code = (code + RichColor.characterCodePrefix) + RichColor.characterCodeObfuscate;
		if (!from.color.equals(to.color))
			code += to.color.toInlineCode();
		return code;
	}, new PlainMorpher(), '\\', new char[] {RichColor.characterCodePrefix}),
	MARKUP(new MarkupEncoder(), '\\', new char[] {'*', '_', '~', '~'}),
	PLAIN((style, prefix) -> "",
		  (from, to) -> "",
		  new ObfuscationMorpher(), '\\', new char[0]);
	
	private interface Transition
	{
		String transition(Style from, Style to);
	}
	
	private interface Encode
	{
		String encode(Style style, boolean prefix);
	}
	
	private interface CharacterMorpher
	{
		char morph(Style style, char character);
	}
	
	private interface Encoder extends Encode, Transition, CharacterMorpher
	{
	
	}
	
	private final Transition transition;
	private final Encode encode;
	private final CharacterMorpher morph;
	private final char[] unsafeCharacters;
	private final char escapeCharacter;
	
	private EncodingType(Encoder encoder, char escapeCharacter, char[] unsafeCharacters)
	{
		this(encoder, encoder, encoder, escapeCharacter, unsafeCharacters);
	}
	
	private EncodingType(Encode encode, Transition transition, CharacterMorpher morph, char escapeCharacter, char[] unsafeCharacters)
	{
		this.encode = encode;
		this.transition = transition;
		this.morph = morph;
		this.escapeCharacter = escapeCharacter;
		this.unsafeCharacters = unsafeCharacters;
	}
	
	public char escapeCharacter()
	{
		return escapeCharacter;
	}
	
	public boolean isSafe(char ch)
	{
		for (char unsafeCharacter : unsafeCharacters)
			if (unsafeCharacter == ch)
				return false;
		return true;
	}
	
	public char morphCharacter(Style style, char character)
	{
		return morph.morph(style, character);
	}
	
	public String transition(Style from, Style to)
	{
		return transition.transition(from, to);
	}
	
	public String encodingPrefix(Style style)
	{
		return encode.encode(style, true);
	}
	
	public String encodingSuffix(Style style)
	{
		return encode.encode(style, false);
	}
	
	public String encode(RichCharacter character)
	{
		return encodingPrefix(character.style) + character.character + encodingSuffix(character.style);
	}
	
	public String encode(RichString string)
	{
		StringBuilder builder = new StringBuilder();
		Style lastPrefixedStyle = null;
		for (int index = 0; index < string.length(); index++)
		{
			RichCharacter ch = string.charAt(index);
			if (lastPrefixedStyle == null)
			{
				builder.append(encodingPrefix(ch.style));
				lastPrefixedStyle = ch.style;
			}
			else if (!lastPrefixedStyle.equals(ch.style))
			{
				builder.append(transition(lastPrefixedStyle, ch.style));
				lastPrefixedStyle = ch.style;
			}
			if (!isSafe(ch.character))
				builder.append(escapeCharacter());
			builder.append(ch.character);
		}
		if (lastPrefixedStyle != null)
			builder.append(encodingSuffix(lastPrefixedStyle));
		return builder.toString();
	}
	
	private static class ObfuscationMorpher implements CharacterMorpher
	{
		final Random random = new Random();
		
		@Override
		public char morph(Style style, char character)
		{
			if (style.obfuscated())
			{
				//generate a random number between 32 and 126 (inclusive)
				return (char) (random.nextInt(94) + 32);
			}
			else
				return character;
		}
	}
	
	private static class PlainMorpher implements CharacterMorpher
	{
		@Override
		public char morph(Style style, char character)
		{
			return character;
		}
	}
	
	private static class MarkupEncoder extends ObfuscationMorpher implements Encoder
	{
		private enum MarkupStyle
		{
			PLAIN					("", ""),
			ITALIC					("*", "*"),
			BOLD					("**", "**"),
			BOLD_ITALIC				("***", "***"),
			UNDERLINE				("__", "__"),
			UNDERLINE_ITALIC		("__*", "*__"),
			UNDERLINE_BOLD			("__**", "**__"),
			UNDERLINE_BOLD_ITALIC	("__***", "***__"),
			STRIKETHROUGH			("~~", "~~"),
			HIGHLIGHT				("`", "`");
			
			final String enter;
			final String exit;
			
			MarkupStyle(String enter, String exit)
			{
				this.enter = enter;
				this.exit = exit;
			}
			
			static MarkupStyle getStyle(Style ch)
			{
				if (ch.strikethrough())
					return MarkupStyle.STRIKETHROUGH;
				else if (ch.highlighted())
					return MarkupStyle.HIGHLIGHT;
				else if (ch.underlined() && ch.italic() && ch.bold())
					return MarkupStyle.UNDERLINE_BOLD_ITALIC;
				else if (!ch.underlined() && ch.italic() && ch.bold())
					return MarkupStyle.BOLD_ITALIC;
				else if (!ch.underlined() && !ch.italic() && ch.bold())
					return MarkupStyle.BOLD;
				else if (!ch.underlined() && ch.italic() && !ch.bold())
					return MarkupStyle.ITALIC;
				else if (ch.underlined() && !ch.italic() && ch.bold())
					return MarkupStyle.UNDERLINE_BOLD;
				else if (ch.underlined() && !ch.italic() && !ch.bold())
					return MarkupStyle.UNDERLINE;
				else if (ch.underlined() && ch.italic() && !ch.bold())
					return MarkupStyle.UNDERLINE_ITALIC;
				return MarkupStyle.PLAIN;
			}
		}
		
		@Override
		public String encode(Style style, boolean prefix)
		{
			MarkupStyle markup = MarkupStyle.getStyle(style);
			return prefix ? markup.enter : markup.exit;
		}
		
		@Override
		public String transition(Style from, Style to)
		{
			return encode(from, false) + encode(to, true);
		}
	}
}