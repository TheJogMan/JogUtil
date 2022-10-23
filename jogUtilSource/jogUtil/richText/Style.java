package jogUtil.richText;

import jogUtil.data.values.*;

import java.awt.*;

public class Style
{
	public static final RichColor defaultMainColor = RichColor.WHITE;
	public static final RichColor defaultHighlightColor = RichColor.CLEAR;
	public static final byte defaultStyleFlags = buildFlagByte(false, false, false,
															 false);
	
	RichColor color;
	RichColor highlightColor;
	Font font;
	byte styleFlags;
	
	public Style(RichColor color, RichColor highlightColor, Font font, byte styleFlags)
	{
		this.color = color;
		this.highlightColor = highlightColor;
		this.font = font;
		this.styleFlags = styleFlags;
	}
	
	public Style(RichColor color, byte styleFlags)
	{
		this(color, defaultHighlightColor, Font.decode(null), styleFlags);
	}
	
	public Style(RichColor color, Font font, byte styleFlags)
	{
		this(color, defaultHighlightColor, font, styleFlags);
	}
	
	public Style(RichColor color, RichColor highlightColor, Font font)
	{
		this(color, highlightColor, font, defaultStyleFlags);
	}
	
	public Style(RichColor color, Font font)
	{
		this(color, defaultHighlightColor, font);
	}
	
	public Style(RichColor color)
	{
		this(color, Font.decode(null));
	}
	
	public Style(Font font)
	{
		this(defaultMainColor, font);
	}
	
	public Style()
	{
		this(defaultMainColor);
	}
	
	public static Style create()
	{
		return new Style();
	}
	
	public Font font()
	{
		return font;
	}
	
	public Style font(Font font)
	{
		this.font = font;
		return this;
	}
	
	public float pointSize()
	{
		return font.getSize2D();
	}
	
	public Style pointScale(int size)
	{
		font = new Font(font.getName(), font.getStyle(), size);
		return this;
	}
	
	public RichColor color()
	{
		return color;
	}
	
	public Style color(RichColor color)
	{
		this.color = color;
		return this;
	}
	
	public RichColor highlightColor()
	{
		return highlightColor;
	}
	
	public Style highlightColor(RichColor highlightColor)
	{
		this.highlightColor = highlightColor;
		return this;
	}
	
	public boolean strikethrough()
	{
		return ByteValue.getBit(styleFlags, 0);
	}
	
	public Style strikeThrough(boolean strikethrough)
	{
		styleFlags = ByteValue.setBit(styleFlags, 0, strikethrough);
		return this;
	}
	
	public boolean underlined()
	{
		return ByteValue.getBit(styleFlags, 1);
	}
	
	public Style underlined(boolean underlined)
	{
		styleFlags = ByteValue.setBit(styleFlags, 1, underlined);
		return this;
	}
	
	public boolean obfuscated()
	{
		return ByteValue.getBit(styleFlags, 2);
	}
	
	public Style obfuscated(boolean obfuscated)
	{
		styleFlags = ByteValue.setBit(styleFlags, 2, obfuscated);
		return this;
	}
	
	public boolean highlighted()
	{
		return ByteValue.getBit(styleFlags, 3);
	}
	
	public Style highlighted(boolean highlighted)
	{
		styleFlags = ByteValue.setBit(styleFlags, 3, highlighted);
		return this;
	}
	
	public boolean bold()
	{
		return font.isBold();
	}
	
	public Style bold(boolean bold)
	{
		font = font.deriveFont((bold ? Font.BOLD : 0) + (italic() ? Font.ITALIC : 0));
		return this;
	}
	
	public boolean italic()
	{
		return font.isItalic();
	}
	
	public Style italic(boolean italic)
	{
		font = font.deriveFont((bold() ? Font.BOLD : 0) + (italic ? Font.ITALIC : 0));
		return this;
	}
	
	public boolean equalTo(Style style)
	{
		return style != null && style.color.equals(color) && style.highlightColor.equals(highlightColor)
			   && style.font.equals(font) && style.styleFlags == styleFlags;
	}
	
	@Override
	public Style clone()
	{
		return new Style(color, highlightColor, font, styleFlags);
	}
	
	public static byte buildFlagByte(boolean strikeThrough, boolean underline, boolean obfuscate,
									 boolean highlight)
	{
		return ByteValue.buildByte(
				//flagMap legend
				strikeThrough,	//strike through
				underline,		//underline
				obfuscate,		//obfuscate
				highlight,		//highlight
				false,			//unused
				false,			//unused
				false,			//unused
				false			//unused
		);
	}
}