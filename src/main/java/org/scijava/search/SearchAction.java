package org.scijava.search;

/**
 * An action which can be executed on a {@link SearchResult}.
 * <p>
 * {@link SearchActionFactory} plugins know how to generate these for specific
 * kinds of {@link SearchResult}.
 * </p>
 * 
 * @author Curtis Rueden
 * @see SearchService#actions(SearchResult)
 */
public interface SearchAction extends Runnable {
	// NB: No implementation needed.
}
