
package org.scijava.search;

/**
 * High-level entry point into searching.
 * <p>
 * Operation typically begins when the user first starts typing something and a
 * search pane pops up. Operation typically terminates when user completes the
 * search, either by executing a search action on a result, or by closing (i.e.
 * canceling) the search pane without running an action.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface SearchOperation {

	/**
	 * Asynchronously terminates the search.
	 * <p>
	 * Intended to be called from the dispatch thread in response to the search
	 * pane being closed.
	 * </p>
	 */
	void terminate();

	/**
	 * Asynchronously updates the search query. {@link Searcher} plugins will be
	 * reinvoked after some delay, with previous-but-still-running queries
	 * invalidated so the user is not bothered with stale results.
	 * <p>
	 * Intended to be called from the dispatch thread in response to the search
	 * pane's text field being updated by the user.
	 * </p>
	 * 
	 * @param text The query to be searched.
	 */
	void search(String text);

	/**
	 * Asynchronously toggles whether to perform
	 * <a href="https://en.wikipedia.org/wiki/Approximate_string_matching">"fuzzy"
	 * matching</a> of the query string.
	 * <p>
	 * Intended to be called from the dispatch thread in response to the search
	 * pane's fuzzy matching checkbox being toggled by the user.
	 * </p>
	 * 
	 * @param fuzzy Whether the search should perform "fuzzy" matching.
	 */
	void setFuzzy(boolean fuzzy);
}
