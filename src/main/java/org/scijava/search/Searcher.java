package org.scijava.search;

import java.util.List;

import org.scijava.plugin.SciJavaPlugin;

/**
 * SciJava plugin type for discovering search results of a particular sort.
 * <p>
 * For example, {@code ModuleSearcher} finds SciJava modules matching the query,
 * whereas {@code WikiSearcher} finds web pages on
 * <a href="https://imagej.net/">imagej.net</a> matching the query.
 * </p>
 * 
 * @author Curtis Rueden
 */
public interface Searcher extends SciJavaPlugin {

	/**
	 * Short descriptive string identifying the sort of results found by this
	 * plugin. This value is typically used as a category title in search UIs.
	 */
	String title();

	/** Gets whether this plugin wants exclusive rights to the given text. */
	default boolean exclusive(@SuppressWarnings("unused") final String text) {
		return false;
	}

	/** Searches for the given text. */
	List<SearchResult> search(String text, boolean fuzzy);
}
