/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
			iconPath == null || iconPath.isEmpty() ? "/icons/search/world_link.png"
				: iconPath, url, details));
	}

	public ArrayList<SearchResult> getSearchResults() {
		return searchResults;
	}

}
