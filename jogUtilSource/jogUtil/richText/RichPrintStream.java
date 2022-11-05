package jogUtil.richText;

import jogUtil.data.*;

import java.io.*;
import java.util.*;

public class RichPrintStream extends PrintStream
{
	//the last style prefix that was applied to the underlying output stream
	private Style lastPrefixed = null;
	private Style style = new Style();
	private final EncodingType encodingType;
	
	public RichPrintStream(OutputStream out, EncodingType type)
	{
		super(out, false);
		this.encodingType = type;
	}
	
	public RichPrintStream(OutputStream out, String encoding, EncodingType type) throws UnsupportedEncodingException
	{
		super(out, false, encoding);
		this.encodingType = type;
	}
	
	public void setStyle(Style style)
	{
		this.style = style.clone();
	}
	
	public Style style()
	{
		return style;
	}
	
	public void print(Value<?, ?> value)
	{
		print(value.toString());
	}
	
	public void println(Value<?, ?> value)
	{
		print(value);
		print('\n');
	}
	
	//Final output methods \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/ \/
	
	//this is used to prevent setPrefix from being called while we are trying to set the prefix, as this
	//would result in a stack overflow
	private boolean settingPrefix = false;
	
	private void setPrefix(Style style)
	{
		if (!settingPrefix)
		{
			settingPrefix = true;
			if (lastPrefixed == null)
				super.print(encodingType.encodingPrefix(style));
			else if (!lastPrefixed.equals(style))
				super.print(encodingType.transition(lastPrefixed, style));
			lastPrefixed = style;
			settingPrefix = false;
		}
	}
	
	@Override
	public RichPrintStream append(char c)
	{
		setPrefix(style);
		if (c == '\n')
			flush();
		if (!encodingType.isSafe(c))
			super.print(encodingType.escapeCharacter());
		super.print(encodingType.morphCharacter(style, c));
		return this;
	}
	
	@Override
	public void write(int b)
	{
		setPrefix(style);
		super.write(b);
	}
	
	public RichPrintStream append(RichCharacter c)
	{
		setPrefix(c.style);
		if (!encodingType.isSafe(c.character))
			super.print(encodingType.escapeCharacter());
		super.print(encodingType.morphCharacter(style, c.character));
		return this;
	}
	
	@Override
	public void flush()
	{
		if (lastPrefixed != null)
			super.print(encodingType.encodingSuffix(lastPrefixed));
		lastPrefixed = null;
		super.flush();
	}
	
	public void print(RichString string)
	{
		for (int index = 0; index < string.length(); index++)
			print(string.charAt(index));
	}
	
	public void println(RichString string)
	{
		print(string);
		print('\n');
	}
	
	public void print(RichCharacter c)
	{
		append(c);
		flush();
	}
	
	public void println(RichCharacter c)
	{
		print(c);
		print('\n');
	}
	
	//Final output methods /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\ /\
	
	@Override
	public RichPrintStream append(CharSequence csq)
	{
		return append(csq, 0, csq.length());
	}
	
	@Override
	public RichPrintStream append(CharSequence csq, int start, int end)
	{
		for (int index = start; index < end; index++)
		{
			append(csq.charAt(index));
		}
		return this;
	}
	
	@Override
	public void print(boolean b)
	{
		print(Boolean.toString(b));
	}
	
	@Override
	public void println(boolean b)
	{
		println(Boolean.toString(b));
	}
	
	@Override
	public void print(char c)
	{
		append(c);
	}
	
	@Override
	public void println(char c)
	{
		print(c);
		print('\n');
	}
	
	@Override
	public void print(char[] c)
	{
		for (char value : c) print(value);
	}
	
	@Override
	public void println(char[] c)
	{
		for (char value : c) print(value);
		print('\n');
	}
	
	@Override
	public void print(double d)
	{
		print(Double.toString(d));
	}
	
	@Override
	public void println(double d)
	{
		println(Double.toString(d));
	}
	
	@Override
	public void print(float f)
	{
		print(Float.toString(f));
	}
	
	@Override
	public void println(float f)
	{
		println(Float.toString(f));
	}
	
	@Override
	public void print(int i)
	{
		print(Integer.toString(i));
	}
	
	@Override
	public void println(int i)
	{
		println(Integer.toString(i));
	}
	
	@Override
	public void print(long l)
	{
		print(Long.toString(l));
	}
	
	@Override
	public void println(long l)
	{
		println(Long.toString(l));
	}
	
	@Override
	public void print(Object obj)
	{
		print(String.valueOf(obj));
	}
	
	@Override
	public void println(Object obj)
	{
		println(String.valueOf(obj));
	}
	
	@Override
	public void print(String string)
	{
		if (string == null)
			string = "null";
		for (int index = 0; index < string.length(); index++)
			print(string.charAt(index));
		flush();
	}
	
	@Override
	public void println(String string)
	{
		print(string + '\n');
	}
	
	@Override
	public void println()
	{
		print('\n');
	}
	
	@Override
	public void write(byte[] buf, int off, int len)
	{
		for (int index = off; index < off + len; index++)
			write(buf[index]);
	}
	
	@Override
	public RichPrintStream format(Locale l, String format, Object... args)
	{
		Formatter formatter = new Formatter();
		formatter.format(l, format, args);
		print(formatter.toString());
		formatter.close();
		return this;
	}
	
	@Override
	public RichPrintStream format(String format, Object... args)
	{
		Formatter formatter = new Formatter();
		formatter.format(format, args);
		print(formatter.toString());
		formatter.close();
		return this;
	}
	
	@Override
	public RichPrintStream printf(Locale l, String format, Object... args)
	{
		Formatter formatter = new Formatter();
		formatter.format(l, format, args);
		print(formatter.toString());
		formatter.close();
		return this;
	}
	
	@Override
	public RichPrintStream printf(String format, Object... args)
	{
		Formatter formatter = new Formatter();
		formatter.format(format, args);
		print(formatter.toString());
		formatter.close();
		return this;
	}
}