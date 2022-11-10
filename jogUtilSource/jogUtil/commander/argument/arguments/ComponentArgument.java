package jogUtil.commander.argument.arguments;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class ComponentArgument extends PlainArgument<CommandComponent>
{
	Category category;
	Class<? extends CommandComponent> filter;
	
	@Override
	public void initArgument(Object[] data)
	{
		category = (Category)data[0];
		filter = CommandComponent.class;
		if (data.length > 1)
		{
			Class<?> proposedFilter = (Class<?>)data[1];
			if (CommandComponent.class.isAssignableFrom(proposedFilter))
				filter = (Class<? extends CommandComponent>)proposedFilter;
		}
	}
	
	@Override
	public String defaultName()
	{
		return "Command Component";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		String token = StringValue.consumeString(source, ' ');
		ArrayList<String> completions = new ArrayList<>();
		for (CommandComponent component : category.getContext(executor))
		{
			if (component.name().toLowerCase().startsWith(token) && filter.isAssignableFrom(component.getClass()))
				completions.add(component.name());
		}
		return completions;
	}
	
	@Override
	public ReturnResult<CommandComponent> interpretArgument(Indexer<Character> source, Executor executor)
	{
		String token = StringValue.consumeString(source, ' ');
		CommandComponent component = category.getContext(executor).get(token);
		if (component != null && filter.isAssignableFrom(component.getClass()))
			return new ReturnResult<>(component);
		else
			return new ReturnResult<>("There is no component with that name.");
	}
}