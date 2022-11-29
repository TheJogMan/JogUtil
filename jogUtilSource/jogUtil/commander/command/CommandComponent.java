package jogUtil.commander.command;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.richText.*;

import java.util.*;

public abstract class CommandComponent implements Interpretable<Boolean>
{
	private final ArrayList<ExecutorFilter.Filter> filters = new ArrayList<>();
	private final ArrayList<ExecutorFilter.Transformer> transformers = new ArrayList<>();
	private final String name;
	private final ArrayList<String> aliases = new ArrayList<>();
	private final RichString description;
	Category parent;
	
	CommandComponent(String name, RichString description)
	{
		this.name = name;
		this.description = description;
	}
	
	CommandComponent(Category parent, String name, RichString description)
	{
		this(name, description);
		if (parent != null)
			parent.addComponent(this);
	}
	
	public RichString helpCommandString()
	{
		RichStringBuilder builder = RichStringBuilder.start();
		if (parent != null)
		{
			builder.style(Style.create().color(RichColor.LIME));
			builder.append(parent.helpCommand.fullName(true) + " " + name());
			if (this instanceof Command)
			{
				int variantCount = ((Command)this).argumentListCount();
				if (variantCount >= 2)
					builder.append(" <").append(parent.helpCommand.getArgumentList(3).list().getArgument(1).name()).append(">");
			}
		}
		return builder.build();
	}
	
	public Category parent()
	{
		return parent;
	}
	
	public String name()
	{
		return name;
	}
	
	public boolean isName(String name)
	{
		return isName(name, false);
	}
	
	public boolean isName(String name, boolean caseSensitive)
	{
		if ((caseSensitive && name.equals(this.name)) || (!caseSensitive && name.equalsIgnoreCase(this.name)))
			return true;
		for (String alias : aliases)
		{
			if ((caseSensitive && name.equals(alias)) || (!caseSensitive && name.equalsIgnoreCase(alias)))
				return true;
		}
		return false;
	}
	
	public int aliasCount()
	{
		return aliases.size();
	}
	
	public String alias(int index)
	{
		return aliases.get(index);
	}
	
	public Iterator<String> aliasIterator()
	{
		return aliases.iterator();
	}
	
	public void addAlias(String alias)
	{
		aliases.add(alias);
	}
	
	public RichString description()
	{
		return description.clone();
	}
	
	public String fullName()
	{
		return fullName(false);
	}
	
	public String fullName(boolean includePrefix)
	{
		if (parent instanceof Console)
			return (includePrefix ? ((Console)parent).prefix : "") + name;
		else if (parent != null)
			return parent.fullName(includePrefix) + (parent.followWithSpace ? ' ' : "") + name;
		else
			return name;
	}
	
	public Console getConsole()
	{
		if (parent != null)
			return parent.getConsole();
		else
			return null;
	}
	
	@Override
	public void addFilter(Filter filter)
	{
		filters.add(filter);
	}
	
	@Override
	public void removeFilter(Filter filter)
	{
		filters.remove(filter);
	}
	
	@Override
	public void addTransformer(Transformer transformer)
	{
		transformers.add(transformer);
	}
	
	@Override
	public void removeTransformer(Transformer transformer)
	{
		transformers.add(transformer);
	}
	
	@Override
	public void transform(Executor executor)
	{
		for (ExecutorFilter.Transformer transformer : transformers)
			transformer.transform(executor);
	}
	
	@Override
	public Result canExecute(Executor executor, boolean applyTransformers)
	{
		if (applyTransformers)
			transform(executor);
		
		for (ExecutorFilter.Filter filter : filters)
		{
			Result result = filter.canExecute(executor);
			if (!result.success())
				return result;
		}
		return new Result();
	}
}