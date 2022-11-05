package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

public class AdaptiveInterpretation extends ReturnResult<Object[]>
{
	private final int listNumber;
	private final ResultContainer[] results;
	private final Indexer<Character> source;
	
	public AdaptiveInterpretation(RichString description, boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		super(description, success, values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
	}
	
	public AdaptiveInterpretation(String description, boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		this(new RichString(description), success, listNumber, values, source, results);
	}
	
	public AdaptiveInterpretation(int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		super(results[0].result.description(), values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
	}
	
	public AdaptiveInterpretation(Indexer<Character> source)
	{
		super();
		listNumber = -1;
		results = new ResultContainer[0];
		this.source = source;
	}
	
	public AdaptiveInterpretation(Indexer<Character> source, ResultContainer[] results)
	{
		this(false, -1, new Object[0], source, results);
	}
	
	public AdaptiveInterpretation(String description, int listNumber, Indexer<Character> source, ResultContainer[] results)
	{
		this(new RichString(description), listNumber, source, results);
	}
	
	public AdaptiveInterpretation(String description, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		this(new RichString(description), listNumber, values, source, results);
	}
	
	public AdaptiveInterpretation(RichString description, int listNumber, Indexer<Character> source, ResultContainer[] results)
	{
		this(description, false, listNumber, new Object[0], source, results);
	}
	
	public AdaptiveInterpretation(RichString description, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		this(description, true, listNumber, values, source, results);
	}
	
	public AdaptiveInterpretation(boolean success, int listNumber, Object[] values, Indexer<Character> source, ResultContainer[] results)
	{
		super("Arguments didn't match any variant.", success, values);
		this.listNumber = listNumber;
		this.results = results;
		this.source = source;
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