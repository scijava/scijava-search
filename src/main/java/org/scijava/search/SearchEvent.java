
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
	private final boolean exclusive;

	public SearchEvent(final Searcher searcher,
		final List<SearchResult> results, final boolean exclusive)
	{
		this.searcher = searcher;
		this.results = results;
		this.exclusive = exclusive;
	}

	public Searcher searcher() {
		return searcher;
	}

	public List<SearchResult> results() {
		return results;
	}

	public boolean exclusive() {
		return exclusive;
	}
}
