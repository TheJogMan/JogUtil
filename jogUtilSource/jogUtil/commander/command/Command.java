package jogUtil.commander.command;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class Command extends CommandComponent
{
	private AdaptiveArgumentList argumentList = new AdaptiveArgumentList(true);
	private final Method[] executors;
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface CommandExecutor
	{
	
	}
	
	public Command(Category parent, String name, RichString description)
	{
		super(parent, name, description);
		
		Method[] methods = getClass().getMethods();
		ArrayList<Method> executors = new ArrayList<>();
		for (Method method : methods)
		{
			if (method.isAnnotationPresent(CommandExecutor.class))
				executors.add(method);
		}
		this.executors = executors.toArray(new Method[0]);
	}
	
	public Command(String name, RichString description)
	{
		this(null, name, description);
	}
	
	public Command(Category parent, String name, String description)
	{
		this(parent, name, new RichString(description));
	}
	
	public Command(String name, String description)
	{
		this(name, new RichString(description));
	}
	
	public Command(Category parent, String name)
	{
		this(parent, name, new RichString("No description."));
	}
	
	public Command(String name)
	{
		this(name, new RichString("No description."));
	}
	
	protected abstract void execute(AdaptiveInterpretation result, Executor executor);
	
	public String usage()
	{
		return usage(0);
	}
	
	public String usage(int variant)
	{
		return fullName(variant, true);
	}
	
	@Override
	public String fullName()
	{
		return fullName(-1);
	}
	
	@Override
	public String fullName(boolean includePrefix)
	{
		return fullName(-1, includePrefix);
	}
	
	/**
	 * Provides this commands full name, including the arguments of a given variant.
	 * @param variant argument variant to include, or -1 to not include arguments
	 * @return
	 * @see jogUtil.commander.command.CommandComponent#fullName()
	 */
	public String fullName(int variant)
	{
		return fullName(variant, false);
	}
	
	/**
	 * Provides this commands full name, including the arguments of a given variant.
	 * @param variant argument variant to include, or -1 to not include arguments
	 * @return
	 * @see jogUtil.commander.command.CommandComponent#fullName(boolean)
	 */
	public String fullName(int variant, boolean includePrefix)
	{
		return super.fullName(includePrefix) + (variant == -1 ? "" : " " + getArgumentList(variant).list());
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
		
		AdaptiveInterpretation result = argumentList.interpret(source, executor);
		if (result.success())
		{
			if (!callExecutors(result, executor))
				execute(result, executor);
			return new ReturnResult<>(true);
		}
		else
		{
			RichStringBuilder builder = RichStringBuilder.start();
			if (result.resultListCount() == 1 || result.listNumber() == -1)
				builder.append(result.description());
			else
			{
				builder.append("Could not interpret arguments: ", Style.create().color(RichColor.RED)).append(result.description());
				for (int index = 0; index < result.resultListCount(); index++)
				{
					builder.newLine();
					builder.append("Variant " + (index + 1) + ": ", Style.create().color(RichColor.AQUA));
					ReturnResult<Object[]> listResult = result.listResult(index);
					if (listResult != null)
						builder.append(listResult.description());
					else
						builder.append("Not checked.");
				}
			}
			if (parent() != null)
			{
				builder.newLine();
				builder.style(Style.create().color(RichColor.GREEN)).append("Use ").append(helpCommandString()).append(" to learn more.");
			}
			
			executor.respond(builder.build());
			return new ReturnResult<>(builder.build());
		}
	}
	
	public boolean callExecutors(AdaptiveInterpretation result, Executor executor)
	{
		Object[] values = result.value();
		for (Method method : executors)
		{
			Class<?>[] arguments = method.getParameterTypes();
			if (arguments.length - 1 != values.length)
				continue;
			
			boolean invalid = false;
			for (int index = 0; index < values.length; index++)
			{
				if (!arguments[index].isAssignableFrom(values[index].getClass()))
				{
					invalid = true;
					break;
				}
			}
			
			if (invalid || !arguments[arguments.length - 1].isAssignableFrom(Executor.class))
				continue;
			
			Object[] parameters = new Object[values.length + 1];
			System.arraycopy(values, 0, parameters, 0, values.length);
			parameters[parameters.length - 1] = executor;
			
			try
			{
				method.invoke(this, parameters);
				return true;
			}
			catch (IllegalAccessException | InvocationTargetException e)
			{
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		if (!canExecute(executor).success())
			return null;
		
		return argumentList.getCompletions(source, executor);
	}
	
	public final void setArgumentList(AdaptiveArgumentList list)
	{
		argumentList = list;
	}
	
	public final void addArgumentList()
	{
		argumentList.addList();
	}
	
	public final void addArgumentList(RichString description)
	{
		argumentList.addList(description);
	}
	
	public final void addArgumentList(String description)
	{
		argumentList.addList(description);
	}
	
	public final int argumentListCount()
	{
		return argumentList.listCount();
	}
	
	public final AdaptiveArgumentList.ArgumentListEntry getArgumentList(int index)
	{
		return argumentList.getList(index);
	}
	
	public final void setDefaultListDescription(RichString description)
	{
		argumentList.setDefaultListDescription(description);
	}
	
	public final void setDefaultListDescription(String description)
	{
		argumentList.setDefaultListDescription(description);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final int argumentCount()
	{
		return argumentList.argumentCount();
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final ArgumentList.ArgumentEntry getArgument(int index)
	{
		return argumentList.getArgument(index);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		argumentList.addArgument(0, argument, name, data, description);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		argumentList.addArgument(0, argument, name, data, description);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		argumentList.addArgument(0, argument, data, description);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		argumentList.addArgument(0, argument, data, description);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		argumentList.addArgument(0, argument, name, data);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, Object[] data)
	{
		argumentList.addArgument(0, argument, data);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument)
	{
		argumentList.addArgument(0, argument);
	}
	
	/***
	 * Defaults to list number 0.
	 * @return
	 */
	public final void addArgument(Class<? extends Argument<?>> argument, String name)
	{
		argumentList.addArgument(0, argument, name);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		argumentList.addArgument(listNumber, argument, name, data, description);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		argumentList.addArgument(listNumber, argument, name, data, description);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		argumentList.addArgument(listNumber, argument, data, description);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		argumentList.addArgument(listNumber, argument, data, description);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		argumentList.addArgument(listNumber, argument, name, data);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, Object[] data)
	{
		argumentList.addArgument(listNumber, argument, data);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument)
	{
		argumentList.addArgument(listNumber, argument);
	}
	
	public final void addArgument(int listNumber, Class<? extends Argument<?>> argument, String name)
	{
		argumentList.addArgument(listNumber, argument, name);
	}
	
	public static class SimpleCommand extends Command
	{
		public interface CommandExecutor
		{
			void execute(AdaptiveInterpretation result, Executor executor);
		}
		
		final CommandExecutor executor;
		
		@Override
		public void execute(AdaptiveInterpretation result, Executor executor)
		{
			this.executor.execute(result, executor);
		}
		
		public SimpleCommand(Category parent, String name, RichString description, CommandExecutor executor, AdaptiveArgumentList list)
		{
			super(parent, name, description);
			this.executor = executor;
			if (list != null)
				setArgumentList(list);
		}
		
		public SimpleCommand(Category parent, String name, String description, CommandExecutor executor, AdaptiveArgumentList list)
		{
			this(parent, name, new RichString(description), executor, list);
		}
		
		public SimpleCommand(Category parent, String name, RichString description, CommandExecutor executor)
		{
			this(parent, name, description, executor, null);
		}
		
		public SimpleCommand(Category parent, String name, String description, CommandExecutor executor)
		{
			this(parent, name, new RichString(description), executor, null);
		}
		
		public SimpleCommand(String name, RichString description, CommandExecutor executor, AdaptiveArgumentList list)
		{
			this(null, name, description, executor, list);
		}
		
		public SimpleCommand(String name, String description, CommandExecutor executor, AdaptiveArgumentList list)
		{
			this(null, name, new RichString(description), executor, list);
		}
		
		public SimpleCommand(String name, RichString description, CommandExecutor executor)
		{
			this(null, name, description, executor, null);
		}
		
		public SimpleCommand(String name, String description, CommandExecutor executor)
		{
			this(null, name, new RichString(description), executor, null);
		}
		
		public SimpleCommand(String name, CommandExecutor executor, AdaptiveArgumentList list)
		{
			this(null, name, new RichString("No description."), executor, list);
		}
		
		public SimpleCommand(String name, CommandExecutor executor)
		{
			this(null, name, new RichString("No description."), executor, null);
		}
		
		public SimpleCommand(Category parent, String name, CommandExecutor executor, AdaptiveArgumentList list)
		{
			this(parent, name, new RichString("No description."), executor, list);
		}
		
		public SimpleCommand(Category parent, String name, CommandExecutor executor)
		{
			this(parent, name, new RichString("No description."), executor, null);
		}
	}
}