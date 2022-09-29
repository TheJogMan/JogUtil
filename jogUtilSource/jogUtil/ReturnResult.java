package jogUtil;

import jogUtil.richText.*;

public class ReturnResult<Type> extends Result
{
	Type value = null;
	
	public ReturnResult(RichString description, boolean success, Type value)
	{
		super(description, success);
		this.value = value;
	}
	
	public ReturnResult(String description, boolean success, Type value)
	{
		this(new RichString(description), success, value);
	}
	
	public ReturnResult(Type value)
	{
		super(true);
		this.value = value;
	}
	
	public ReturnResult()
	{
		super(false);
	}
	
	public ReturnResult(String description)
	{
		this(new RichString(description));
	}
	
	public ReturnResult(String description, Type value)
	{
		this(new RichString(description), value);
	}
	
	public ReturnResult(RichString description)
	{
		this(description, false, null);
	}
	
	public ReturnResult(RichString description, Type value)
	{
		this(description, true, value);
	}
	
	public ReturnResult(boolean success, Type value)
	{
		super(success);
		this.value = value;
	}
	
	public ReturnResult(Exception exception)
	{
		super(exception);
	}
	
	public Type value()
	{
		return value(false);
	}
	
	public Type value(boolean ignoreSuccess)
	{
		//TODO revisit once RichString is fully implemented
		//throw new RuntimeException("Attempt to get return value from an unsuccessful operation: " + description().encode(EncodingType.PLAIN);
		if (success() || ignoreSuccess)
			return value;
		else
			throw new RuntimeException("Attempt to get return value from an unsuccessful result!");
	}
}
