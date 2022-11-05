package jogUtil.richText;

import java.awt.*;
import java.awt.font.*;
import java.util.*;

public class RichString
{
	final RichCharacter[] characters;
	
	public RichString(String string)
	{
		this(string, new Style());
	}
	
	public RichString(String string, Style style)
	{
		if (string == null)
			string = "null";
		characters = new RichCharacter[string.length()];
		for (int index = 0; index < string.length(); index++)
			characters[index] = new RichCharacter(string.charAt(index), style);
	}
	
	public RichString(RichString string)
	{
		characters = new RichCharacter[string.length()];
		for (int index = 0; index < characters.length; index++)
			characters[index] = string.characters[index].clone();
	}
	
	public RichString()
	{
		characters = new RichCharacter[0];
	}
	
	public RichString(Collection<RichCharacter> characters)
	{
		if (characters == null)
			characters = new ArrayList<>();
		this.characters = new RichCharacter[characters.size()];
		int index = 0;
		for (RichCharacter character : characters)
		{
			this.characters[index] = character;
			index++;
		}
	}
	
	@Override
	public RichString clone()
	{
		return new RichString(this);
	}
	
	@Override
	public String toString()
	{
		return encode(EncodingType.PLAIN);
	}
	
	public String encode(EncodingType type)
	{
		return type.encode(this);
	}
	
	public int length()
	{
		return characters.length;
	}
	
	public RichCharacter charAt(int index)
	{
		return characters[index];
	}
	
	public int getLogicalWidth()
	{
		ArrayList<String> segments = new ArrayList<>();
		ArrayList<Font> fonts = new ArrayList<>();
		StringBuilder segment = new StringBuilder();
		RichCharacter lastCharacter = null;
		for (int index = 0; index < length(); index++)
		{
			RichCharacter character = charAt(index);
			if (lastCharacter == null
				|| lastCharacter.style.font.hashCode() == character.style.font.hashCode())
				segment.append(character.character);
			else
			{
				fonts.add(lastCharacter.style.font);
				segments.add(segment.toString());
				segment = new StringBuilder("" + character.character);
			}
			lastCharacter = character;
		}
		if (segment.length() > 0)
		{
			fonts.add(lastCharacter.style.font);
			segments.add(segment.toString());
		}
		
		int width = 0;
		
		for (int index = 0; index < segments.size(); index++)
		{
			segment = new StringBuilder(segments.get(index));
			Font font = fonts.get(index);
			width += font.getStringBounds(segment.toString(), new FontRenderContext(font.getTransform(),
										true, true)).getBounds().width;
		}
		return width;
	}
	
	public int getLogicalHeight()
	{
		int height = 0;
		for (RichCharacter character : characters)
		{
			int characterHeight = character.getLogicalHeight();
			if (characterHeight > height) height = characterHeight;
		}
		return height;
	}
	
	public int getVisualWidth()
	{
		ArrayList<String> segments = new ArrayList<>();
		ArrayList<Font> fonts = new ArrayList<>();
		StringBuilder segment = new StringBuilder();
		RichCharacter lastCharacter = null;
		for (int index = 0; index < length(); index++)
		{
			RichCharacter character = charAt(index);
			if (lastCharacter == null
				|| lastCharacter.style.font.hashCode() == character.style.font.hashCode())
				segment.append(character.character);
			else
			{
				fonts.add(lastCharacter.style.font);
				segments.add(segment.toString());
				segment = new StringBuilder("" + character.character);
			}
			lastCharacter = character;
		}
		if (segment.length() > 0)
		{
			fonts.add(lastCharacter.style.font);
			segments.add(segment.toString());
		}
		
		int width = 0;
		
		for (int index = 0; index < segments.size(); index++)
		{
			segment = new StringBuilder(segments.get(index));
			Font font = fonts.get(index);
			width += (new TextLayout(segment.toString(), font, new FontRenderContext(font.getTransform(),
					true, true)))
					.getPixelBounds(null, 0, 0).width;
		}
		return width;
	}
	
	public int getVisualHeight()
	{
		int height = 0;
		for (RichCharacter character : characters)
		{
			int characterHeight = character.getVisualHeight();
			if (characterHeight > height) height = characterHeight;
		}
		return height;
	}
}
