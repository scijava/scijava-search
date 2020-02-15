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

	/** Whether this plugin supports a search of this term. */
	default boolean supports(@SuppressWarnings("unused") final String term) {
		return true;
	}

	/** Gets whether this plugin wants exclusive rights to the given text. */
	default boolean exclusive(@SuppressWarnings("unused") final String text) {
		return false;
	}

	/** Searches for the given text. */
	List<SearchResult> search(String text, boolean fuzzy);
}
