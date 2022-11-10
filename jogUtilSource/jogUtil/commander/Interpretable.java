package jogUtil.commander;

import jogUtil.*;
import jogUtil.indexable.*;

import java.util.*;

public interface Interpretable<InterpretationResult> extends ExecutorFilter
{
	/**
	 * Interprets a Character source.
	 * <p>
	 *     Interpretation should be denied to any executors that do not pass the set filters.
	 *     <br><br>
	 *     This method is also intended to apply transformers before doing any filter checks, but it is
	 *     not required.
	 * </p>
	 * @param source
	 * @param executor
	 * @return
	 * @see jogUtil.commander.ExecutorFilter#transform(Executor)
	 * @see jogUtil.commander.ExecutorFilter#canExecute(Executor)
	 */
	public ReturnResult<InterpretationResult> interpret(Indexer<Character> source, Executor executor);
	
	/**
	 * Provides Auto-Completions based on a Character source.
	 * <p>
	 *     Denying completions to executors that do not pass the set filters is optional.  The
	 *     primary purpose for providing the executor to this method is for contextualized
	 *     completions.
	 *     <br><br>
	 *     This method is also intended to apply transformers before doing any filter checks, but it is
	 *     not required.
	 * </p>
	 * @param source
	 * @param executor
	 * @return
	 * @see jogUtil.commander.ExecutorFilter#transform(Executor)
	 * @see jogUtil.commander.ExecutorFilter#canExecute(Executor)
	 */
	public List<String> getCompletions(Indexer<Character> source, Executor executor);
}