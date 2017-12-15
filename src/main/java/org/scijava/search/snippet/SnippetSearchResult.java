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
package org.scijava.search.snippet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.script.ScriptLanguage;
import org.scijava.search.SearchResult;
import org.scijava.search.module.ModuleSearcher;

/**
 * Search result for the {@link ModuleSearcher}.
 *
 * @author Curtis Rueden
 */
public class SnippetSearchResult implements SearchResult {

	private final ScriptLanguage language;
	private final String snippet;
	private final Map<String, String> props;

	public SnippetSearchResult(final ScriptLanguage language,
		final String snippet)
	{
		this.language = language;
		this.snippet = snippet;

		props = new LinkedHashMap<>();
		props.put("Language", language.getLanguageName());
		props.put("Nicknames", s(language.getNames()));
		props.put("Extensions", s(language.getExtensions()));
		props.put("Engine Name", language.getEngineName());
		props.put("Engine Version", language.getEngineVersion());
		props.put("MIME Types", s(language.getMimeTypes()));
	}

	public ScriptLanguage language() {
		return language;
	}

	public String snippet() {
		return snippet;
	}

	@Override
	public String name() {
		return language.getLanguageName() + ": " + snippet;
	}
	
	@Override
	public String identifier() {
		return name();
	}

	@Override
	public String iconPath() {
		return null;
//		return "/icons/" + language.getNames().get(0);
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String s(final List<String> names) {
		if (names == null || names.isEmpty()) return "<None>";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(names.get(i));
		}
		return sb.toString();
	}
}
