package jogUtil.commander.argument;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class ArgumentList implements Interpretable<Object[]>
{
	final ArrayList<ArgumentEntry> arguments = new ArrayList<>();
	
	@Override
	public ReturnResult<Object[]> interpret(Indexer<Character> source, Executor executor)
	{
		Result canExecute = canExecute(executor);
		if (!canExecute.success())
			return new ReturnResult<>(RichStringBuilder.start().append("Can not execute: ", Style.create().color(RichColor.RED)).append(canExecute.description()).build());
		
		Object[] values = new Object[arguments.size()];
		int index = 0;
		for (ArgumentEntry argument : arguments)
		{
			canExecute = argument.argument.canExecute(executor);
			if (!canExecute.success())
				return new ReturnResult<>(RichStringBuilder.start().append("Can not execute with argument #" + index + ": ", Style.create().color(RichColor.RED))
														   .append(canExecute.description()).build());
			
			ReturnResult<?> result = argument.interpret(source, executor);
			
			if (result.success())
			{
				values[index] = result.value();
				index++;
			}
			else
				return new ReturnResult<>(RichStringBuilder.start().append("Could not interpret argument #" + (index + 1) + ": ", Style.create().color(RichColor.RED))
														   .append(result.description()).build());
			
			if (index < arguments.size())
			{
				if (source.atEnd())
					return new ReturnResult<>("Could not interpret argument #" + (index + 1) + ": Not enough data.");
				else if (source.get() != ' ')
					return new ReturnResult<>("Space expected after argument #" + index);
				else
					source.next();
			}
		}
		return new ReturnResult<>(values);
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		if (!canExecute(executor).success())
			return new ArrayList<>();
		
		//step through the arguments until we reach the end of the source
		List<String> completions;
		for (ArgumentEntry argument : arguments)
		{
			completions = argument.getCompletions(source.copy(), executor);
			/*
			 * we want to check if this argument can be interpreted successfully
			 * 		if yes, then we need to verify that the source ends with a space character
			 * 			if yes, we move on to the next argument
			 * 			if not, then we give this argument's completions
			 * 		if not, then we want to give this argument's completions
			 */
			ReturnResult<?> result = argument.interpret(source, executor);
			if (result.success())
			{
				//can't end with a space if we have already reached the end
				if (source.atEnd() || source.next() != ' ')
					return completions;
			}
			else
				return completions;
		}
		return new ArrayList<>();
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, RichString description)
	{
		arguments.add(new ArgumentEntry(argument, name, data, description));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data, String description)
	{
		arguments.add(new ArgumentEntry(argument, name, data, new RichString(description)));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data, RichString description)
	{
		arguments.add(new ArgumentEntry(argument, null, data, description));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data, String description)
	{
		arguments.add(new ArgumentEntry(argument, null, data, new RichString(description)));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, String name, Object[] data)
	{
		arguments.add(new ArgumentEntry(argument, name, data, null));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, Object[] data)
	{
		arguments.add(new ArgumentEntry(argument, null, data, null));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument)
	{
		arguments.add(new ArgumentEntry(argument, null, new Object[0], null));
	}
	
	public void addArgument(Class<? extends Argument<?>> argument, String name)
	{
		arguments.add(new ArgumentEntry(argument, name, new Object[0], null));
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (Iterator<ArgumentEntry> iterator = arguments.iterator(); iterator.hasNext();)
		{
			ArgumentEntry entry = iterator.next();
			String name = entry.argument().defaultName();
			if (name == null)
				name = entry.name();
			builder.append("<").append(name).append(">");
			if (iterator.hasNext())
				builder.append(" ");
		}
		return builder.toString();
	}
	
	public static class ArgumentEntry
	{
		Argument<?> argument;
		String name = "Argument";
		RichString description = new RichString("No Description.");
		
		ArgumentEntry(Class<? extends Argument<?>> argumentClass, String name, Object[] data, RichString description)
		{
			try
			{
				argument = argumentClass.getConstructor().newInstance();
			}
			catch (Exception e)
			{
				RuntimeException ex = new RuntimeException("Could not create instance of Argument (" + argumentClass.getName() + ") " + e);
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			try
			{
				argument.initArgument(data);
			}
			catch (Exception e)
			{
				StringBuilder dataString = new StringBuilder("{\n");
				for (Object datum : data)
					dataString.append(datum.toString()).append("\n");
				dataString.append("}");
				RuntimeException ex = new RuntimeException("Could not initialize argument (" + argumentClass.getName() + ") " + e.getMessage() + "\nProvided data:" + dataString);
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			try
			{
				String defaultName = argument.defaultName();
				if (defaultName != null)
					this.name = defaultName;
			}
			catch (Exception e)
			{
				RuntimeException ex = new RuntimeException("Could not get default name for argument (" + argumentClass.getName() + ") " + e.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			try
			{
				RichString defaultDescription = argument.defaultDescription();
				if (defaultDescription != null)
					this.description = description;
			}
			catch (Exception e)
			{
				RuntimeException ex = new RuntimeException("Could not get default description for argument (" + argumentClass.getName() + ") " + e.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			if (name != null)
				this.name = name;
			if (description != null)
				this.description = description;
		}
		
		public List<String> getCompletions(Indexer<Character> source, Executor executor)
		{
			List<String> completions;
			try
			{
				completions = argument.getCompletions(source, executor);
			}
			catch (Exception e)
			{
				RuntimeException ex = new RuntimeException("Could not getCompletions for argument (" + argument.getClass().getName() + ") " + e.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			if (completions == null)
				completions = new ArrayList<>();
			return completions;
		}
		
		public ReturnResult<?> interpret(Indexer<Character> source, Executor executor)
		{
			try
			{
				return argument.interpret(source, executor);
			}
			catch (Exception e)
			{
				RuntimeException ex = new RuntimeException("Could not interpret argument (" + argument.getClass().getName() + ") " + e.getMessage());
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		}
		
		public String name()
		{
			return name;
		}
		
		public RichString description()
		{
			return description;
		}
		
		public Argument<?> argument()
		{
			return argument;
		}
	}
	
	public int argumentCount()
	{
		return arguments.size();
	}
	
	public ArgumentEntry getArgument(int index)
	{
		return arguments.get(index);
	}
}