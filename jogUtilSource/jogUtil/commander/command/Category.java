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
		return getContext(executor).interpret(source);
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		return getContext(executor).getCompletions(source);
	}
	
	public void addContextFiller(ContextFiller filler)
	{
		contextFillers.add(filler);
	}
	
	public void removeContextFiller(ContextFiller filler)
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
	
	public static class Context implements Iterable<CommandComponent>
	{
		final ArrayList<CommandComponent> contextualizedComponents = new ArrayList<>();
		final Executor contextSource;
		final Category category;
		
		private Context(Executor executor, Category category)
		{
			contextSource = executor;
			this.category = category;
			for (ContextFiller filler : category.contextFillers)
			{
				Collection<CommandComponent> contextualizedComponents = filler.getComponents(executor);
				if (contextualizedComponents == null)
					continue;
				for (CommandComponent component : contextualizedComponents)
				{
					component.parent = category;
					this.contextualizedComponents.add(component);
				}
			}
		}
		
		public ReturnResult<Boolean> interpret(Indexer<Character> source)
		{
			Result canExecute = category.canExecute(contextSource, false);
			if (!canExecute.success())
			{
				ReturnResult<Boolean> result = new ReturnResult<>(RichStringBuilder.start().append("You can not run this command: ", Style.create().color(RichColor.RED))
																				   .append(canExecute.description()).build());
				contextSource.respond(result.description());
				return result;
			}
			
			//if we are at the end of our source, then we want to run this category's help command
			if (source.atEnd())
			{
				category.helpCommand.interpret((new IndexableArray<Character>(0)).iterator(), contextSource);
				return new ReturnResult<>(true);
			}
			
			int start = source.position();
			for (CommandComponent component : this)
			{
				//we can ignore any components that this executor can't execute
				if (!component.canExecute(contextSource).success())
					continue;
				
				source.setPosition(start);
				//if we match a valid component, then we want to interpret that component
				if (StringValue.consumeSequence(source, component.name(), false))
				{
					if (!ensureValidEnd(component, source))
						continue;
					
					return component.interpret(source, contextSource);
				}
			}
			
			//if we don't match any components by proper name, then we check aliases
			for (CommandComponent component : this)
			{
				//we can ignore any components that this executor can't execute
				if (!component.canExecute(contextSource).success())
					continue;
				
				source.setPosition(start);
				//if we match a valid component, then we want to interpret that component
				for (Iterator<String> aliasIterator = component.aliasIterator(); aliasIterator.hasNext();)
				{
					String alias = aliasIterator.next();
					if (StringValue.consumeSequence(source, alias, false))
					{
						if (!ensureValidEnd(component, source))
							continue;
						
						return component.interpret(source, contextSource);
					}
				}
			}
			
			//if we don't match any components, then the input was invalid
			contextSource.respond("That command does not exist.");
			return new ReturnResult<>("That command does not exist.");
		}
		
		private static boolean ensureValidEnd(CommandComponent component, Indexer<Character> source)
		{
			if (component instanceof Category subCategory)
			{
				if (subCategory.followWithSpace)
				{
					if (!source.atEnd() && source.get() == ' ')
						source.skip();
				}
				else
					return source.atEnd() || source.get() != ' ';
			}
			else if (component instanceof Command && !source.atEnd())
			{
				if (source.get() == ' ')
					source.skip();
				else
					return false;
			}
			
			return true;
		}
		
		public List<String> getCompletions(Indexer<Character> source)
		{
			if (!category.canExecute(contextSource, false).success())
				return null;
			
			ArrayList<String> completions = new ArrayList<>();
			int start = source.position();
			for (CommandComponent component : this)
			{
				//we can ignore any components that this executor can't execute
				if (!component.canExecute(contextSource).success())
					continue;
				
				completions.add(component.name());
				for (Iterator<String> aliasIterator = component.aliasIterator(); aliasIterator.hasNext();)
					completions.add(aliasIterator.next());
				
				source.setPosition(start);
				//if we already match a valid component, then we want to give that component's completions
				if (StringValue.consumeSequence(source, component.name(), false))
				{
					if (!ensureValidEnd(component, source))
						continue;
					
					return component.getCompletions(source, contextSource);
				}
			}
			return completions;
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
			for (CommandComponent component : this)
			{
				for (Iterator<String> aliasIterator = component.aliasIterator(); aliasIterator.hasNext();)
				{
					String alias = aliasIterator.next();
					if (alias.equalsIgnoreCase(name))
						return component;
				}
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
				return index < contextualizedComponents.size() + category.components.size();
			}
			
			@Override
			public CommandComponent next()
			{
				CommandComponent component;
				if (index < category.components.size())
					component = category.components.get(index).getValue();
				else if (index - category.components.size() < contextualizedComponents.size())
					component = contextualizedComponents.get(index - category.components.size());
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