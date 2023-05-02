/*
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2023 SciJava developers.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.plugin.SingletonService;
import org.scijava.service.SciJavaService;

/**
 * A service that performs asynchronous searches.
 *
 * @author Curtis Rueden
 */
public interface SearchService extends SingletonService<SearchActionFactory>,
	SciJavaService
{

	/**
	 * Begins an asynchronous and multi-threaded search operation.
	 *
	 * @param callbacks The objects to be notified as search results are found.
	 *          Callbacks will be made on the thread doing the search,
	 *          <em>not</em> the dispatch thread; it is the responsibility of
	 *          listeners to queue to the dispatch thread accordingly as needed
	 *          (e.g., to update UI elements).
	 * @return An object to use for managing the state of the search.
	 */
	default SearchOperation search(final SearchListener... callbacks) {
		return new DefaultSearchOperation(context(), callbacks);
	}

	/**
	 * Gets the suite of available actions for the given search result.
	 *
	 * @param result The search result for which available actions are desired.
	 * @return A list of actions which could possibly be executed for the result.
	 */
	default List<SearchAction> actions(final SearchResult result) {
		// Create a map used to track whether a name has been seen
		final Set<String> seenLabels = new HashSet<>();
		return getInstances().stream() //
			.filter(factory -> factory.supports(result)) //
			.map(factory -> factory.create(result)) //
			// NB The following line skip actions with duplicate labels
			.filter(t -> seenLabels.add(t.toString())) //
			.collect(Collectors.toList());
	}

	/** Gets whether the given searcher plugin is currently enabled. */
	boolean enabled(Searcher s);

	/** Enables or disables the given searcher plugin. */
	void setEnabled(Searcher s, boolean enabled);

	@Override
	default Class<SearchActionFactory> getPluginType() {
		return SearchActionFactory.class;
	}
}
