package jogUtil.commander;

import jogUtil.richText.*;

import java.io.*;
import java.util.*;

public abstract class Executor
{
	final HashMap<Class<? extends ExecutorModule>, ExecutorModule> modules = new HashMap<>();
	
	public abstract void respond(RichString message);
	
	public void respond(String string)
	{
		respond(new RichString(string));
	}
	
	public void addModule(ExecutorModule module)
	{
		modules.put(module.getClass(), module);
	}
	
	public void removeModule(Class<? extends ExecutorModule> moduleClass)
	{
		modules.remove(moduleClass);
	}
	
	public boolean hasModule(Class<? extends ExecutorModule> moduleClass)
	{
		return modules.containsKey(moduleClass);
	}
	
	public <T extends ExecutorModule> T getModule(Class<T> moduleClass)
	{
		return (T) modules.get(moduleClass);
	}
	
	public interface ExecutorModule
	{
	
	}
	
	public static class HeadlessExecutor extends Executor
	{
		final RichPrintStream stream;
		
		public HeadlessExecutor()
		{
			this(null);
		}
		
		public HeadlessExecutor(RichPrintStream stream)
		{
			this.stream = stream;
		}
		
		public HeadlessExecutor(PrintStream stream)
		{
			this(new RichPrintStream(stream, EncodingType.PLAIN));
		}
		
		@Override
		public void respond(RichString message)
		{
			if (stream != null)
				stream.println(message);
		}
	}
}
