package jogUtil.richText;

import java.awt.font.*;

public class RichCharacter
{
	Style style;
	Character character;
	
	public RichCharacter(char character)
	{
		this(character, new Style());
	}
	
	public RichCharacter(char character, Style style)
	{
		this.character = character;
		this.style = style.clone();
	}
	
	public RichCharacter()
	{
		this(' ');
	}
	
	public char character()
	{
		return character;
	}
	
	public void character(char character)
	{
		this.character = character;
	}
	
	public Style style()
	{
		return style;
	}
	
	public void style(Style style)
	{
		this.style = style.clone();
	}
	
	/**
	 * Checks if the underlying char values are equal in a case-sensitive check
	 * @param otherChar
	 * @return
	 * @see #equalTo(RichCharacter, boolean)
	 */
	public boolean equalTo(RichCharacter otherChar)
	{
		return equalTo(otherChar, true);
	}
	
	/**
	 * Checks if the underlying char values are equal
	 * @param otherChar
	 * @param caseSensitive
	 * @return
	 */
	public boolean equalTo(RichCharacter otherChar, boolean caseSensitive)
	{
		if (otherChar == null)
			return false;
		if (caseSensitive)
			return character == otherChar.character;
		else
			return Character.isUpperCase(character) && Character.isUpperCase(character)
				&& character == otherChar.character;
	}
	
	public boolean equalStyle(RichCharacter otherChar)
	{
		return style.equalTo(otherChar.style);
	}
	
	/**
	 * Checks if these two characters would appear the same
	 * <p>
	 *     Assumes that they are each being rendered on their own.
	 * </p>
	 * @param otherChar
	 * @return
	 */
	public boolean visuallyEqual(RichCharacter otherChar)
	{
		return equalTo(otherChar, true) && equalStyle(otherChar);
	}
	
	@Override
	public RichCharacter clone()
	{
		return new RichCharacter(character, style);
	}
	
	public int getLogicalHeight()
	{
		return style.font.getStringBounds("" + character, new FontRenderContext(style.font.getTransform(),
										true, true)).getBounds().height;
	}
	
	public int getLogicalWidth()
	{
		return style.font.getStringBounds("" + character, new FontRenderContext(style.font.getTransform(),
										true, true)).getBounds().width;
	}
	
	public int getVisualHeight()
	{
		return (new TextLayout("" + character, style.font,
				new FontRenderContext(style.font.getTransform(), true,true)))
				.getPixelBounds(null, 0, 0).height;
	}
	
	public int getVisualWidth()
	{
		return (new TextLayout("" + character, style.font,
				new FontRenderContext(style.font.getTransform(), true, true)))
				.getPixelBounds(null, 0, 0).width;
	}
	
	public String encode(EncodingType type)
	{
		return type.encode(this);
	}
	
	@Override
	public String toString()
	{
		return encode(EncodingType.PLAIN);
	}
}