package jogUtil;

import jogUtil.richText.*;

import java.io.*;

public class Result
{
	private final boolean success;
	private final RichString description;
	private Exception exception = null;
	
	/**
	 * Creates an exception result.
	 * <p>
	 *     The exception's message will be used as the result's description
	 *     and the success status will be false.<br>
	 * </p>
	 * @param exception
	 * @see #Result(RichString, boolean)
	 */
	public Result(Exception exception)
	{
		this(exception.getMessage(), false);
		this.exception = exception;
	}
	
	/**
	 * Creates a result with a given description and success status.
	 * @param description
	 * @param success
	 */
	public Result(RichString description, boolean success)
	{
		this.description = description;
		this.success = success;
	}
	
	/**
	 * Converts String to RichString
	 * @param description
	 * @param success
	 * @see #Result(RichString, boolean)
	 */
	public Result(String description, boolean success)
	{
		this(new RichString(description), success);
	}
	
	/**
	 * Description defaults to "No description."
	 * @param result
	 * @see #Result(String, boolean) 
	 */
	public Result(boolean result)
	{
		this("No description.", result);
	}
	
	/**
	 * Success defaults to false.
	 * @param description
	 * @see #Result(RichString, boolean) 
	 */
	public Result(RichString description)
	{
		this(description, false);
	}
	
	/**
	 * Converts String to RichString.
	 * @param description
	 * @see #Result(RichString) 
	 */
	public Result(String description)
	{
		this(new RichString(description));
	}
	
	/**
	 * Success defaults to true.
	 * @see #Result(boolean) 
	 */
	public Result()
	{
		this(true);
	}
	
	public boolean success()
	{
		return success;
	}
	
	public RichString description()
	{
		return description;
	}
	
	/**
	 * Checks if this is an exception result.
	 * @return
	 */
	public boolean hasException()
	{
		return exception != null;
	}
	
	/**
	 * If this is an exception result, this will return the exception.
	 * @return Exception or null.
	 */
	public Exception exception()
	{
		return exception;
	}
	
	/**
	 * Describes where the exception was thrown from.
	 * @param e
	 * @return
	 */
	public static String describeThrowable(Throwable e)
	{
		return e + " at " + e.getStackTrace()[0].getClassName() + "." + e.getStackTrace()[0].getMethodName()
			   + "(" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")";
	}
	
	/**
	 * Gives the full stack trace of the exception.
	 * @param e
	 * @return
	 */
	public static String describeThrowableFull(Throwable e)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		e.printStackTrace(new PrintStream(stream));
		return stream.toString();
	}
}