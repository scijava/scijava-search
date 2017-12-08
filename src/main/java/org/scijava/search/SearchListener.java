package org.scijava.search;

/**
 * An object to be notified when a {@link Searcher} completes a search and
 * produces results.
 *
 * @author Curtis Rueden
 */
public interface SearchListener {

	/** TODO */
	void searchCompleted(SearchEvent event);
}
