/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2020 SciJava developers.
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
