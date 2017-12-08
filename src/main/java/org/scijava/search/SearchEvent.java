
package org.scijava.search;

import java.util.List;

/**
 * An event housing the results of a search.
 *
 * @author Curtis Rueden
 */
public class SearchEvent {

	private final Searcher searcher;
	private final List<SearchResult> results;

	public SearchEvent(final Searcher searcher,
		final List<SearchResult> results)
	{
		this.searcher = searcher;
		this.results = results;
	}

	public Searcher searcher() {
		return searcher;
	}

	public List<SearchResult> results() {
		return results;
	}
}
