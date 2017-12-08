package org.scijava.search;

import java.util.Map;

/**
 * Data container for one item of a search result.
 * <p>
 * Each {@link Searcher} plugin returns a list of these objects when
 * {@link Searcher#search(String, boolean)} is called.
 * </p>
 *
 * @author Curtis Rueden
 */
public interface SearchResult {

	String name();
	String iconPath();
	Map<String, String> properties();
}
