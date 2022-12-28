package jogUtil.commander.argument.arguments;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class TokenArgument extends PlainArgument<TokenArgument.Token>
{
	String[] tokens = new String[0];
	boolean caseSensitive = false;
	
	@Override
	public void initArgument(Object[] data)
	{
		if (data instanceof String[])
		{
			tokens = (String[])data;
		}
		else if (data.length == 2 && data[0] instanceof String[] && data[1] instanceof Boolean)
		{
			tokens = (String[])data[0];
			caseSensitive = (Boolean)data[1];
		}
	}
	
	@Override
	public String defaultName()
	{
		return "Token";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor, Object[] data)
	{
		ArrayList<String> completions = new ArrayList<>(tokens.length);
		completions.addAll(Arrays.asList(tokens));
		return completions;
	}
	
	@Override
	public ReturnResult<Token> interpretArgument(Indexer<Character> source, Executor executor, Object[] data)
	{
		String token = StringValue.consumeString(source, ' ');
		if (caseSensitive)
			token = token.toLowerCase();
		for (int index = 0; index < tokens.length; index++)
			if (token.compareTo(caseSensitive ? tokens[index] : tokens[index].toLowerCase()) == 0)
				return new ReturnResult<>(new Token(index));
		return new ReturnResult<>("Invalid Argument");
	}
	
	public class Token
	{
		final int index;
		
		Token(int index)
		{
			this.index = index;
		}
		
		public String get()
		{
			return tokens[index];
		}
		
		public int index()
		{
			return index;
		}
	}
}