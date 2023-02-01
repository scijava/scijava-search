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

package org.scijava.search.snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;
import org.scijava.search.SearchResult;
import org.scijava.search.SearchService;
import org.scijava.search.Searcher;

/**
 * {@link Searcher} plugin for code snippets.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Searcher.class, priority = Priority.EXTREMELY_HIGH)
public class SnippetSearcher implements Searcher {

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private SearchService searchService;

	@Override
	public String title() {
		// NB: A misnomer, but it's the term users are familiar with.
		return "Code snippets";
	}

	@Override
	public boolean supports(final String text) {
		return text.startsWith("#!") || text.startsWith("!");
	}

	@Override
	public boolean exclusive(final String text) {
		return text.startsWith("#!") || text.startsWith("!");
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		if (!searchService.enabled(this)) return new ArrayList<>();
		if (text.startsWith("#!")) {
			final String[] tokens = text.split("\\s", 2);
			if (tokens.length < 2) return Collections.emptyList();
			final String langHint = tokens[0];
			final String snippet = tokens[1];

			return results(scriptService.getLanguages().stream().filter(
				language -> matches(langHint, language.getLanguageName()) || matches(
					langHint, language.getNames()) || matches(langHint, language
						.getExtensions())).collect(Collectors.toList()), snippet);
		}
		if (text.startsWith("!")) {
			return results(scriptService.getLanguages(), text.substring(1));
		}
		return Collections.emptyList();
	}

	// -- Helper methods --

	private boolean matches(final String actual, final List<String> desiredList) {
		return desiredList.stream().filter( //
			desired -> matches(actual, desired)).findAny().isPresent();
	}

	private boolean matches(final String actual, final String desired) {
		return actual.toLowerCase().matches(".*" + desired + ".*");
	}

	private List<SearchResult> results(final List<ScriptLanguage> languages,
		final String snippet)
	{
		final List<SearchResult> results = new ArrayList<>();
		for (final ScriptLanguage language : languages) {
			results.add(new SnippetSearchResult(language, snippet));
		}
		return results;
	}
}
