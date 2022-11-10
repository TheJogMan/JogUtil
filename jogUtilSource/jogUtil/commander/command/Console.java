package jogUtil.commander.command;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class Console extends Category
{
	public final char prefix;
	
	public Console(String name, char prefix, RichString description)
	{
		super(name, description);
		this.prefix = prefix;
	}
	
	public Console(String name, char prefix, String description)
	{
		this(name, prefix, new RichString(description));
	}
	
	public Console(String name, char prefix)
	{
		this(name, prefix, new RichString("No description."));
	}
	
	@Override
	public Console getConsole()
	{
		return this;
	}
	
	@Override
	public String fullName(boolean includePrefix)
	{
		return (includePrefix ? prefix : "") + name();
	}
	
	@Override
	public List<String> getCompletions(Indexer<Character> source, Executor executor)
	{
		if (!source.atEnd() && source.get() == prefix)
			source.next();
		return super.getCompletions(source, executor);
	}
	
	@Override
	public ReturnResult<Boolean> interpret(Indexer<Character> source, Executor executor)
	{
		if (!source.atEnd() && source.get() == prefix)
			source.next();
		return super.interpret(source, executor);
	}
}