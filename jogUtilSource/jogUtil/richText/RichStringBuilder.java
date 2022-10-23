package jogUtil.richText;

import java.util.*;

public class RichStringBuilder
{
	LinkedList<RichCharacter> characters = new LinkedList<>();
	Style style = new Style();
	
	public static RichStringBuilder start()
	{
		return new RichStringBuilder();
	}
	
	public static RichStringBuilder start(RichString string)
	{
		return new RichStringBuilder(string);
	}
	
	public static RichStringBuilder start(String string)
	{
		return start(string, new Style());
	}
	
	public static RichStringBuilder start(String string, Style style)
	{
		return new RichStringBuilder(string, style);
	}
	
	public RichStringBuilder()
	{
	
	}
	
	public RichStringBuilder(RichString base)
	{
		append(base);
	}
	
	public RichStringBuilder(String base)
	{
		append(base);
	}
	
	public RichStringBuilder(String base, Style style)
	{
		append(base, style);
	}
	
	public RichStringBuilder newLine()
	{
		append('\n');
		return this;
	}
	
	public RichStringBuilder append(RichCharacter character)
	{
		characters.add(character);
		return this;
	}
	
	public RichStringBuilder append(Character character)
	{
		return append(new RichCharacter(character, style));
	}
	
	public RichStringBuilder append(Character character, Style style)
	{
		return append(new RichCharacter(character, style));
	}
	
	public RichStringBuilder append(RichString string)
	{
		for (RichCharacter character : string.characters)
			append(character);
		return this;
	}
	
	public RichStringBuilder append(String string)
	{
		for (char character : string.toCharArray())
			append(character);
		return this;
	}
	
	public RichStringBuilder append(String string, Style style)
	{
		for (char character : string.toCharArray())
			append(character, style);
		return this;
	}
	
	public RichStringBuilder style(Style style)
	{
		this.style = style.clone();
		return this;
	}
	
	public Style style()
	{
		return style;
	}
	
	public int length()
	{
		return characters.size();
	}
	
	public RichStringBuilder clear()
	{
		characters.clear();
		return this;
	}
	
	public RichString build()
	{
		return new RichString(characters);
	}
}