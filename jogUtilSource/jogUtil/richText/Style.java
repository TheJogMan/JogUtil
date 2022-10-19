package jogUtil.richText;

import java.awt.*;

public class Style
{
	RichColor backgroundColor = RichColor.CLEAR;
	RichColor textColor = RichColor.WHITE;
	//TODO figure out default values to initialize these to
	//nvm, all style data will be in a separate style class
	Font font;
	float size;
	byte styleFlags;
	
	public Font font()
	{
		return font;
	}
	
	public void font(Font font)
	{
		this.font = font;
	}
	
	public float size()
	{
		return size;
	}
	
	public void size(float size)
	{
		this.size = size;
	}
	
	public RichColor glyphColor()
	{
		return textColor;
	}
	
	public void glyphColor(RichColor glyphColor)
	{
		this.textColor = glyphColor;
	}
	
	public RichColor backgroundColor()
	{
		return backgroundColor;
	}
	
	public void backgroundColor(RichColor backgroundColor)
	{
		this.backgroundColor = backgroundColor;
	}
	
	//TODO everything needs implementation, I'm just slapping out the skeleton
	
	public boolean italic()
	{
		return false;
	}
	
	public boolean bold()
	{
		return false;
	}
	
	public boolean underlined()
	{
		return false;
	}
	
	public boolean strikethrough()
	{
		return false;
	}
	
	public boolean obfuscated()
	{
		return false;
	}
	
	public boolean equalTo(Style style)
	{
		return false;
	}
}