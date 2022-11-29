package jogUtil.commander.argument.arguments;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class WordArgument extends PlainArgument<String>
{
	boolean addBrackets = true;
	
	@Override
	public void initArgument(Object[] data)
	{
		if (data.length == 1 && data[0] instanceof Boolean value)
			addBrackets = value;
	}
	
	@Override
	public boolean addBrackets()
	{
		return addBrackets;
	}
	
	@Override
	public String defaultName()
	{
		return "Word";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public ReturnResult<String> interpretArgument(Indexer<Character> source, Executor executor)
	{
		return new ReturnResult<>(true, StringValue.consumeString(source, ' '));
	}
}