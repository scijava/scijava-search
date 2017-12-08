package org.scijava.search;

import java.util.List;

/**
 * An object to be notified when a {@link Searcher} completes a search and
 * produces results.
 *
 * @author Curtis Rueden
 */
public interface SearchListener {

	/** TODO */
	void searchCompleted(Searcher searcher, List<SearchResult> results);
}
