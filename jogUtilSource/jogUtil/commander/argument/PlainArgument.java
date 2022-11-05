package jogUtil.commander.argument;

public abstract class PlainArgument<ValueType> implements Argument<ValueType>
{
	CompletionBehavior behavior = CompletionBehavior.FILTER;
	
	@Override
	public final void setCompletionBehavior(CompletionBehavior behavior)
	{
		this.behavior = behavior;
	}
	
	@Override
	public final CompletionBehavior getCompletionBehavior()
	{
		return behavior;
	}
}