package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public interface Argument<ValueType> extends Interpretable<ValueType>
{
	/**
	 * Used to pass in parameters
	 * <p>
	 *     Since arguments are added to ArgumentLists by class, arguments
	 *     can be provided along with the class to then be passed into
	 *     the argument with this method.<br>
	 *     <br>
	 *     Called immediately after a new instance of the argument is
	 *     created for an ArgumentList, and can be considered to be the
	 *     constructor.
	 * </p>
	 * @param data
	 */
	public void initArgument(Object[] data);
	
	/**
	 * Used if a new name isn't set by an ArgumentList
	 * @return
	 */
	public String defaultName();
	
	/**
	 * Used if a new description isn't set by an ArgumentList
	 * <p>
	 *     Intended to be overridden by subclasses that want to use it, by
	 *     default it returns an empty RichString
	 * </p>
	 * @return
	 */
	public default RichString defaultDescription()
	{
		return null;
	}
	
	/**
	 * Provides auto completions.
	 * <p>
	 *     The provided list of completions will be modified based on
	 *     the set CompletionBehavior.
	 *     <br><br>
	 *     This method is only called for executors that pass all set filters, any
	 *     that don't will receive no completions.
	 * </p>
	 * @param source
	 * @param executor
	 * @param data Additional data to configure completions
	 * @return
	 * @see jogUtil.commander.Interpretable#getCompletions(Indexer, Executor)
	 * @see #setCompletionBehavior(CompletionBehavior)
	 */
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor, Object[] data);
	
	public default List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return argumentCompletions(source, executor, new Object[0]);
	}
	
	/**
	 * Interprets this argument.
	 * <p>
	 *     This method is only called for executors that pass all set filters, any
	 *     that don't will have already been denied.
	 * </p>
	 * @param source
	 * @param executor
	 * @param data Additional data to configure interpreter
	 * @return
	 */
	public ReturnResult<ValueType> interpretArgument(Indexer<Character> source, Executor executor, Object[] data);
	
	public default ReturnResult<ValueType> interpretArgument(Indexer<Character> source, Executor executor)
	{
		return interpretArgument(source, executor, new Object[0]);
	}
	
	/**
	 * Determines how completions will be modified.
	 * <p>
	 *     Completions are modified based on an initial token. Which is the first string of non-space
	 *     characters from the provided Character Indexer.
	 *     <br><br>
	 *     Default: Filter
	 *     <br><br>
	 *     Filter: Any completions that don't begin with the initial token will be removed.
	 *     <br><br>
	 *     Append: The initial token will be appended to the beginning of all completions.
	 *     <br><br>
	 *     Basic: Completions will not be modified.
	 * </p>
	 * @param behavior
	 * @see #argumentCompletions(Indexer, Executor, Object[])
	 */
	public void setCompletionBehavior(CompletionBehavior behavior);
	
	/**
	 * Whether brackets should be added around this argument in a list of arguments.
	 * <p>
	 *     Default implementation always returns true, but an argument's implementation is free to
	 *     override it.
	 * </p>
	 * @return
	 */
	public default boolean addBrackets()
	{
		return true;
	}
	
	/**
	 * 
	 * @return
	 * @see #setCompletionBehavior(CompletionBehavior)
	 */
	public CompletionBehavior getCompletionBehavior();
	
	public enum CompletionBehavior
	{
		BASIC, APPEND, FILTER
	}
	
	@Override
	public default List<String> getCompletions(Indexer<Character> source, Executor executor, Object[] data)
	{
		ArrayList<String> completions = new ArrayList<>();
		if (canExecute(executor).success())
		{
			List<String> rawCompletions = argumentCompletions(source.copy(), executor, data);
			if (rawCompletions == null)
				rawCompletions = new ArrayList<>();
			String token = StringValue.consumeString(source, ' ');
			
			
			if (getCompletionBehavior() == CompletionBehavior.APPEND)
			{
				//append the token to the beginning of all completions
				for (String completion : rawCompletions)
					completions.add(token + completion);
			}
			else if (getCompletionBehavior() == CompletionBehavior.FILTER)
			{
				//exclude any completions that do not start with the token
				token = token.toLowerCase();
				for (String completion : rawCompletions)
				{
					if (completion.toLowerCase().startsWith(token))
						completions.add(completion);
				}
			}
		}
		return completions;
	}
	
	@Override
	public default ReturnResult<ValueType> interpret(Indexer<Character> source, Executor executor, Object[] data)
	{
		Result filterResult = canExecute(executor);
		if (!filterResult.success())
			return new ReturnResult<>(filterResult.description());
		
		return interpretArgument(source, executor, data);
	}
}