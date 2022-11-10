package jogUtil.commander.argument.arguments;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class WordArgument extends PlainArgument<String>
{
	@Override
	public void initArgument(Object[] data)
	{
	
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
		return new ReturnResult<>(StringValue.consumeString(source, ' '));
	}
}