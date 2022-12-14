package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

public class AdaptiveInterpretation extends ReturnResult<Object[]>
{
	private final int listNumber;
	private final ResultContainer[] results;
	private final Indexer<Character> source;
	private final Executor executor;
	
	public AdaptiveInterpretation(RichString description, boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		super(description, success, values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
		this.executor = executor;
	}
	
	public AdaptiveInterpretation(String description, boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(new RichString(description), success, listNumber, values, source, results, executor);
	}
	
	public AdaptiveInterpretation(int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		super(results[0].result.description(), values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
		this.executor = executor;
	}
	
	public AdaptiveInterpretation(Indexer<Character> source, Executor executor)
	{
		super();
		listNumber = -1;
		results = new ResultContainer[0];
		this.source = source;
		this.executor = executor;
	}
	
	public AdaptiveInterpretation(Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(false, -1, new Object[0], source, results, executor);
	}
	
	public AdaptiveInterpretation(String description, int listNumber, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(new RichString(description), listNumber, source, results, executor);
	}
	
	public AdaptiveInterpretation(String description, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(new RichString(description), listNumber, values, source, results, executor);
	}
	
	public AdaptiveInterpretation(RichString description, int listNumber, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(description, false, listNumber, new Object[0], source, results, executor);
	}
	
	public AdaptiveInterpretation(RichString description, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		this(description, true, listNumber, values, source, results, executor);
	}
	
	public AdaptiveInterpretation(boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results, Executor executor)
	{
		super("Arguments didn't match any variant.", success, values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
		this.executor = executor;
	}
	
	public final Executor executor()
	{
		return executor;
	}
	
	public final int listNumber()
	{
		return listNumber;
	}
	
	public final ReturnResult<Object[]> listResult(int index)
	{
		return results[index].result;
	}
	
	public final Indexer<Character> indexer()
	{
		return source;
	}
	
	public final Indexer<Character> indexer(int index)
	{
		return results[index].source;
	}
	
	public final int resultListCount()
	{
		return results.length;
	}
	
	public record ResultContainer(ReturnResult<Object[]> result, Indexer<Character> source)
	{
	
	}
}