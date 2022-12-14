package jogUtil.richText;

import java.awt.*;
import java.awt.color.*;

public class RichColor extends Color
{
	public static final char characterCodePrefix = '§';
	public static final char characterCodeItalic = 'o';
	public static final char characterCodeUnderline = 'n';
	public static final char characterCodeStrikethrough = 'm';
	public static final char characterCodeBold = 'I';
	public static final char characterCodeObfuscate = 'k';
	public static final char characterCodeReset = 'r';
	
	public static final RichColor DARK_RED = new RichColor(11141120, '4');
	public static final RichColor RED = new RichColor(16733525, 'c');
	public static final RichColor ORANGE = new RichColor(16755200, '6');
	public static final RichColor YELLOW = new RichColor(16777045, 'e');
	public static final RichColor GREEN = new RichColor(43520, '2');
	public static final RichColor LIME = new RichColor(5635925, 'a');
	public static final RichColor AQUA = new RichColor(5636095, 'b');
	public static final RichColor CYAN = new RichColor(43690, '3');
	public static final RichColor BLUE = new RichColor(170, '1');
	public static final RichColor LIGHT_BLUE = new RichColor(5592575, '9');
	public static final RichColor PINK = new RichColor(16733695, 'd');
	public static final RichColor MAGENTA = new RichColor(11141290, '5');
	public static final RichColor WHITE = new RichColor(16777215, 'f');
	public static final RichColor GRAY = new RichColor(11184810, '7');
	public static final RichColor DARK_GRAY = new RichColor(5592405, '8');
	public static final RichColor BLACK = new RichColor(0, 0, 0, 255, '0');
	public static final RichColor CLEAR = new RichColor(0, characterCodeReset);
	//
	public static final RichColor JOG_YELLOW = new RichColor(255, 201, 14, 'e');
	//#3f48cc
	public static final RichColor JOG_BLUE = new RichColor(63, 72, 204, 'b');
	//#a349a4
	public static final RichColor JOG_PURPLE = new RichColor(163, 73, 164, '5');
	//
	public static final RichColor JOG_GRAY = new RichColor(127, 127, 127, '7');
	
	private char characterCode = characterCodeReset;
	
	public RichColor(int rgb, char characterCode)
	{
		super(rgb);
		this.characterCode = characterCode;
	}
	
	public RichColor(int r, int g, int b, char characterCode)
	{
		super(r, g, b);
		this.characterCode = characterCode;
	}
	
	public RichColor(int r, int g, int b, int a, char characterCode)
	{
		super(r, g, b, a);
		this.characterCode = characterCode;
	}
	
	public RichColor(Color color)
	{
		super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		if (color instanceof RichColor)
			this.characterCode = ((RichColor)color).characterCode;
	}
	
	public RichColor(ColorSpace cspace, float[] components, float alpha)
	{
		super(cspace, components, alpha);
	}
	
	public RichColor(float r, float g, float b)
	{
		super(r, g, b);
	}
	
	public RichColor(float r, float g, float b, float a)
	{
		super(r, g, b, a);
	}
	
	public RichColor(int rgb)
	{
		super(rgb);
	}
	
	public RichColor(int rgba, boolean hasAlpha)
	{
		super(rgba, hasAlpha);
	}
	
	public RichColor(int r, int g, int b)
	{
		super(r, g, b);
	}
	
	public RichColor(int r, int g, int b, int a)
	{
		super(r, g, b, a);
	}
	
	public String toInlineCode()
	{
		return characterCodePrefix + "" + characterCode;
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof RichColor otherColor))
			return false;
		return otherColor.getRGB() == getRGB() && characterCode == otherColor.characterCode;
	}
}