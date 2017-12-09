
package org.scijava.search.snippet;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptREPL;
import org.scijava.script.ScriptService;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;

/**
 * Search action for executing a code snippet.
 *
 * @author Curtis Rueden
 */
@Plugin(type = SearchActionFactory.class)
public class RunSnippetActionFactory implements SearchActionFactory {

	/** The singleton REPL shared by all snippet executions. */
	private ScriptREPL repl;

	@Parameter
	private ScriptService scriptService;

	@Override
	public boolean supports(final SearchResult result) {
		return result instanceof SnippetSearchResult;
	}

	@Override
	public SearchAction create(final SearchResult result) {
		final SnippetSearchResult snippetResult = (SnippetSearchResult) result;

		if (repl == null) {
			repl = new ScriptREPL(scriptService.context());
			repl.initialize(); // TODO: initialize(false) once it exists.
		}

		return new DefaultSearchAction("Evaluate", () -> {
			repl.lang(snippetResult.language().getLanguageName());
			repl.evaluate(snippetResult.snippet());
		});
	}
}
