package jogUtil;

import jogUtil.indexable.*;
import jogUtil.richText.*;

public interface Consumer<ResultType, InputType>
{
	public ConsumptionResult<ResultType, InputType> consume(Indexer<InputType> source);
	
	public static class ConsumptionResult<ResultType, InputType> extends ReturnResult<ResultType>
	{
		private final Indexer<InputType> indexer;
		
		public ConsumptionResult(ResultType result, Indexer<InputType> indexer, boolean success, RichString description)
		{
			super(description, success, result);
			this.indexer = indexer;
		}
		
		public ConsumptionResult(ResultType result, Indexer<InputType> indexer, boolean success, String description)
		{
			this(result, indexer, success, new RichString(description));
		}
		
		public ConsumptionResult(ResultType result, Indexer<InputType> indexer)
		{
			this(result, indexer, true, "No description.");
		}
		
		public ConsumptionResult(ResultType result, Indexer<InputType> indexer, String description)
		{
			this(result, indexer, new RichString(description));
		}
		
		public ConsumptionResult(ResultType result, Indexer<InputType> indexer, RichString description)
		{
			this(result, indexer, true, description);
		}
		
		public ConsumptionResult(Indexer<InputType> indexer)
		{
			this(null, indexer, false, "No description.");
		}
		
		public ConsumptionResult(Indexer<InputType> indexer, String description)
		{
			this(indexer, new RichString(description));
		}
		
		public ConsumptionResult(Indexer<InputType> indexer, RichString description)
		{
			this(null, indexer, false, description);
		}
		
		public Indexer<InputType> indexer()
		{
			return indexer;
		}
	}
}