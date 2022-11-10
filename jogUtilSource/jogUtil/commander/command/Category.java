package jogUtil.commander.command;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.command.commands.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class Category extends CommandComponent
{
	final boolean followWithSpace;
	public final Command helpCommand;
	private final KeyedList<String, CommandComponent> components = new KeyedList<>();
	private final ArrayList<ContextFiller> contextFillers = new ArrayList<>();
	
	public Category(Category parent, String name, RichString description, boolean followWithSpace)
	{
		super(parent, name, description);
		this.followWithSpace = followWithSpace;
		helpCommand = addHelpCommand();
	}
	
	public Category(String name, RichString description, boolean followWithSpace)
	{
		this(null, name, description);
	}
	
	public Category(String name, RichString description)
	{
		this(name, description, true);
	}
	
	public Category(String name, String description)
	{
		this(name, new RichString(description));
	}
	
	public Category(Category parent, String name, String description)
	{
		this(parent, name, new RichString(description));
	}
	
	public Category(String name, String description, boolean followWithSpace)
	{
		this(name, new RichString(description), followWithSpace);
	}
	
	public Category(Category parent, String name, RichString description)
	{
		this(parent, name, description, true);
	}
	
	public Category(Category parent, String name, String description, boolean followWithSpace)
	{
		this(parent, name, new RichString(description), followWithSpace);
	}
	
	public Category(Category parent, String name, boolean followWithSpace)
	{
		this(parent, name, new RichString("No description."), followWithSpace);
	}
	
	public Category(String name, boolean followWithSpace)
	{
		this(name, new RichString("No description."), followWithSpace);
	}
	
	public Category(Category parent, String name)
	{
		this(parent, name, new RichString("No description."));
	}
	
	public Category(String name)
	{
		this(name, new RichString("No description."));
	}
	
	/**
	 * Adds this category's help command to itself
	 * <p>
	 *     This method can be left as is in most implementations, however it can be overridden if a
	 *     custom help command is desired.
	 *     <br><br>
	 *     If a custom help command is provided, it must have a variant with no arguments.
	 * </p>
	 */
	protected Command addHelpCommand()
	{
		return new HelpCommand(this);
	}
	
	protected Result addComponent(CommandComponent component)
	{
		if (component.parent != null)
			return new Result("Component already has a parent.");
		
		if (components.containsKey(component.name().toLowerCase()))
			return new Result("This category already has a component named '" + component.name() + "'");
		
		components.put(component.name().toLowerCase(), component);
		component.parent = this;
		return new Result();
	}
	
	@Override
	public ReturnResult<Boolean> interpret(Indexer<Character> source, Executor executor)
	{
		Result canExecute = canExecute(executor);
		if (!canExecute.success())
		{
			ReturnResult<Boolean> result = new ReturnResult<>(RichStringBuilder.start().append("You can not run this command: ", Style.create().color(RichColor.RED))
																			   .append(canExecute.description()).build());
			executor.respond(result.description());
			return result;
		}
		
		//if we are at the end of our source, then we want to run this category's help command
		if (source.atEnd())
		{
			helpCommand.interpret((new IndexableArray<Character>(0)).iterator(), executor);
			return new ReturnResult<>(true);
		}
		
		int start = source.position();
		for (CommandComponent component : getContext(executor, false))
		{
			//we can ignore any components that this executor can't execute
			if (!component.canExecute(executor).success())
				continue;
			
			source.setPosition(start);
			//if we match a valid component, then we want to interpret that component
			if (StringValue.consumeSequence(source, component.name(), false))
			{
				if (component instanceof Category)
				{
					if (((Category)component).followWithSpace == (source.get() == ' '))
					{
						if (((Category)component).followWithSpace)
							source.skip();
					}
					else
						continue;
				}
				else if (component instanceof Command && !source.atEnd() && source.next() != ' ')
					continue;
				
				return component.interpret(source, executor);
			}
		}
		
		//if we don't match any components, then the input was invalid
		executor.respond("That command does not exist.");
		return new ReturnResult<>("That command does not exist.");
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		if (!canExecute(executor).success())
			return null;
		
		ArrayList<String> completions = new ArrayList<>();
		int start = source.position();
		for (CommandComponent component : getContext(executor, false))
		{
			//we can ignore any components that this executor can't execute
			if (!component.canExecute(executor).success())
				continue;
			
			completions.add(component.name());
			
			source.setPosition(start);
			//if we already match a valid component, then we want to give that component's completions
			if (StringValue.consumeSequence(source, component.name(), false))
			{
				if (component instanceof Category)
				{
					if (((Category)component).followWithSpace == (source.get() == ' '))
					{
						if (((Category)component).followWithSpace)
							source.skip();
					}
					else
						continue;
				}
				else if (component instanceof Command && !source.atEnd() && source.next() != ' ')
					continue;
				
				return component.getCompletions(source, executor);
			}
		}
		return completions;
	}
	
	protected void addContextFiller(ContextFiller filler)
	{
		contextFillers.add(filler);
	}
	
	protected void removeContextFiller(ContextFiller filler)
	{
		contextFillers.remove(filler);
	}
	
	public Context getContext(Executor executor)
	{
		return getContext(executor, true);
	}
	
	public Context getContext(Executor executor, boolean applyTransformers)
	{
		if (applyTransformers)
			transform(executor);
		
		return new Context(executor, this);
	}
	
	public class Context implements Iterable<CommandComponent>
	{
		final ArrayList<CommandComponent> contextualizedComponents = new ArrayList<>();
		final Executor contextSource;
		
		private Context(Executor executor, Category category)
		{
			contextSource = executor;
			for (ContextFiller filler : contextFillers)
			{
				Collection<CommandComponent> contextualizedComponents = filler.getComponents(executor);
				for (CommandComponent component : contextualizedComponents)
				{
					component.parent = category;
					contextualizedComponents.add(component);
				}
			}
		}
		
		public Executor contextSource()
		{
			return contextSource;
		}
		
		public CommandComponent get(String name)
		{
			for (CommandComponent component : this)
			{
				if (component.name().equalsIgnoreCase(name))
					return component;
			}
			return null;
		}
		
		@Override
		public Iterator<CommandComponent> iterator()
		{
			return new ContextIterator();
		}
		
		public class ContextIterator implements Iterator<CommandComponent>
		{
			int index = 0;
			
			@Override
			public boolean hasNext()
			{
				return index < contextualizedComponents.size() + components.size();
			}
			
			@Override
			public CommandComponent next()
			{
				CommandComponent component;
				if (index < components.size())
					component = components.get(index).getValue();
				else if (index - components.size() < contextualizedComponents.size())
					component = contextualizedComponents.get(index - components.size());
				else
					component = null;
				index++;
				return component;
			}
		}
	}
	
	public interface ContextFiller
	{
		public Collection<CommandComponent> getComponents(Executor executor);
	}
}