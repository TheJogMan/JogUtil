package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class AdaptiveArgumentList extends ArgumentList
{
	final ArrayList<ArgumentListEntry> lists = new ArrayList<>();
	final boolean mustReachEnd;
	
	public AdaptiveArgumentList()
	{
		this(false);
	}
	
	public AdaptiveArgumentList(boolean mustReachEnd)
	{
		this.mustReachEnd = mustReachEnd;
		addList();
	}
	
	public static AdaptiveArgumentList create()
	{
		return create(false);
	}
	
	public static AdaptiveArgumentList create(boolean mustReachEnd)
	{
		return new AdaptiveArgumentList(mustReachEnd);
	}
	
	public AdaptiveArgumentList addList()
	{
		addList(new RichString());
		return this;
	}
	
	public AdaptiveArgumentList addList(RichString description)
	{
		lists.add(new ArgumentListEntry(description));
		return this;
	}
	
	public AdaptiveArgumentList addList(String description)
	{
		addList(new RichString(description));
		return this;
	}
	
	public int listCount()
	{
		return lists.size();
	}
	
	public ArgumentListEntry getList(int index)
	{
		return lists.get(index);
	}
	
	public AdaptiveArgumentList setDefaultListDescription(RichString description)
	{
		lists.get(0).description = description;
		return this;
	}
	
	public AdaptiveArgumentList setDefaultListDescription(String description)
	{
		setDefaultListDescription(new RichString(description));
		return this;
	}
	
	@Override
	public AdaptiveInterpretation interpret(Indexer<Character> source, Executor executor)
	{
		AdaptiveInterpretation.ResultContainer[] results = new AdaptiveInterpretation.ResultContainer[lists.size()];
		
		Result canExecute = canExecute(executor);
		if (!canExecute.success())
			return new AdaptiveInterpretation(RichStringBuilder.start().append("Can not execute: ").append(canExecute.description()).build(), -1, source, results);
		
		for (int index = 0; index < lists.size(); index++)
		{
			Indexer<Character> sourceCopy = source.copy();
			ReturnResult<Object[]> result = lists.get(index).list.interpret(sourceCopy, executor);
			results[index] = new AdaptiveInterpretation.ResultContainer(result, sourceCopy);
			if (result.success() && (!mustReachEnd || sourceCopy.atEnd()))
				return new AdaptiveInterpretation(result.description(), result.success(), index, result.value(), sourceCopy, results);
		}
		return new AdaptiveInterpretation(source, results);
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		if (!canExecute(executor).success())
			return new ArrayList<>();
		
		ArrayList<String> completions = new ArrayList<>();
		for (ArgumentListEntry list : lists)
			completions.addAll(list.list.getCompletions(source.copy(), executor));
		return completions;
	}
	
	public static class ArgumentListEntry
	{
		final ArgumentList list;
		RichString description;
		
		public ArgumentListEntry(RichString description)
		{
			list = new ArgumentList();
			this.description = description;
		}
		
		public ArgumentList list()
		{
			return list;
		}
		
		public RichString description()
		{
			return description;
		}
	}
	
	public AdaptiveArgumentList addFilterToAdaptiveList(Filter filter)
	{
		addFilter(filter);
		return this;
	}
	
	public AdaptiveArgumentList addFilterToList(int listNumber, Filter filter)
	{
		lists.get(listNumber).list.addFilter(filter);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		lists.get(listNumber).list.addArgument(argument, name, data, description);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		lists.get(listNumber).list.addArgument(argument, name, data, description);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		lists.get(listNumber).list.addArgument(argument, data, description);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		lists.get(listNumber).list.addArgument(argument, data, description);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		lists.get(listNumber).list.addArgument(argument, name, data);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data)
	{
		lists.get(listNumber).list.addArgument(argument, data);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument)
	{
		lists.get(listNumber).list.addArgument(argument);
		return this;
	}
	
	public AdaptiveArgumentList addArgument(int listNumber, Class<? extends Argument<?>> argument, String name)
	{
		lists.get(listNumber).list.addArgument(argument, name);
		return this;
	}
	
	@Override
	public void addFilter(Filter filter)
	{
		addFilterToList(0, filter);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		addArgument(0, argument, name, data, description);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		addArgument(0, argument, name, data, description);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		addArgument(0, argument, data, description);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		addArgument(0, argument, data, description);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		addArgument(0, argument, name, data);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data)
	{
		addArgument(0, argument, data);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument)
	{
		addArgument(0, argument);
	}
	
	@Override
	public void addArgument(Class<? extends Argument<?>> argument, String name)
	{
		addArgument(0, argument, name);
	}
}