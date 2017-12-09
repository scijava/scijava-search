
package org.scijava.search.web;

import java.util.ArrayList;

import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

/**
 * The AbstractWebSearcher contains convenience functions to manage search
 * results of all {@link Searcher} plugins browsing the web.
 * 
 * @author Robert Haase (MPI-CBG)
 */
public abstract class AbstractWebSearcher implements Searcher {

	private final String title;
	private final ArrayList<SearchResult> searchResults = new ArrayList<>();

	/**
	 * @param title Name of the search engine
	 */
	public AbstractWebSearcher(final String title) {
		this.title = title;
	}

	@Override
	public String title() {
		return title;
	}

	/**
	 * @param name Resulting website title / name
	 * @param iconPath path to an image representing the results
	 * @param url URL of the found website
	 * @param details some text from the website representing its content
	 */
	protected void addResult(final String name, final String iconPath,
		final String url, final String details)
	{
		searchResults.add(new WebSearchResult(name, //
			iconPath == null || iconPath.isEmpty() ? "/icons/world_link.png"
				: iconPath, url, details));
	}

	public ArrayList<SearchResult> getSearchResults() {
		return searchResults;
	}

}
