package jogUtil.command.argument;

import jogUtil.*;
import jogUtil.command.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public abstract class Argument<ValueType> implements Interpretable<ValueType>
{
	/**
	 * Used if a new name isn't set by an ArgumentList
	 * @return
	 */
	public abstract String defaultName();
	/**
	 * Used if a new description isn't set by an ArgumentList
	 * <p>
	 *     Intended to be overridden by subclasses that want to use it, by
	 *     default it returns an empty RichString
	 * </p>
	 * @return
	 */
	public RichString defaultDescription()
	{
		return new RichString("");
	}
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
	protected abstract void initArgument(Object[] data);
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
	 * @return
	 * @see jogUtil.command.Interpretable#getCompletions(Indexer, Executor)
	 * @see #setCompletionBehavior(CompletionBehavior)
	 */
	protected abstract List<String> argumentCompletions(Indexer<Character> source, Executor executor);
	
	/**
	 * Interprets this argument.
	 * <p>
	 *     This method is only called for executors that pass all set filters, any
	 *     that don't will have already been denied.
	 * </p>
	 * @param source
	 * @param executor
	 * @return
	 */
	protected abstract ReturnResult<ValueType> interpretArgument(Indexer<Character> source,
																 Executor executor);
	
	private CompletionBehavior completionBehavior = CompletionBehavior.FILTER;
	
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
	 * @see #argumentCompletions(Indexer, Executor)
	 */
	protected void setCompletionBehavior(CompletionBehavior behavior)
	{
		completionBehavior = behavior;
	}
	
	public enum CompletionBehavior
	{
		BASIC, APPEND, FILTER
	}
	
	@Override
	public final List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		ArrayList<String> completions = new ArrayList<>();
		if (canExecute(executor).success())
		{
			List<String> rawCompletions = argumentCompletions(source.copy(), executor);
			if (rawCompletions == null)
				rawCompletions = new ArrayList<>();
			String token = StringValue.consumeString(source, ' ');
			
			
			if (completionBehavior == CompletionBehavior.APPEND)
			{
				//append the token to the beginning of all completions
				for (String completion : rawCompletions)
					completions.add(token + completion);
			}
			else if (completionBehavior == CompletionBehavior.FILTER)
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
	public final ReturnResult<ValueType> interpret(Indexer<Character> source, Executor executor)
	{
		Result filterResult = canExecute(executor);
		if (!filterResult.success())
			return new ReturnResult<>(filterResult.description());
		
		return interpretArgument(source, executor);
	}
}