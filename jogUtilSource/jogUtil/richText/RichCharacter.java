package jogUtil.richText;

public class RichCharacter
{
	Style style;
	char character;
	
	public RichCharacter(char character)
	{
		this.character = character;
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
		this.style = style;
	}
	
	//TODO everything needs implementation, I'm just slapping out the skeleton
	
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
		return false;
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
}