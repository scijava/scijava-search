/*
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

package org.scijava.search.module;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.app.AppService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

/**
 * {@link Searcher} plugin for SciJava modules.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Searcher.class, priority = Priority.VERY_HIGH)
public class ModuleSearcher implements Searcher {

	@Parameter
	private Context context;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private AppService appService;

	@Override
	public String title() {
		// NB: A misnomer, but it's the term users are familiar with.
		return "Commands";
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		final String baseDir = //
			appService.getApp().getBaseDirectory().getAbsolutePath();

		final LinkedHashSet<ModuleInfo> matches = new LinkedHashSet<>();

		final List<ModuleInfo> modules = moduleService.getModules();

		final String textLower = text.toLowerCase();

		// First, add modules where title starts with the text.
		modules.stream() //
			.filter(info -> info.getTitle().startsWith(textLower)) //
			.forEach(matches::add);

		// Next, add modules where title has text inside somewhere.
		modules.stream() //
			.filter(info -> matches(info.getTitle(), textLower)) //
			.forEach(matches::add);

		// Finally, add modules where menu path has text inside somewhere.
		modules.stream() //
			.filter(info -> matches(info.getMenuPath().toString(), textLower)) //
			.forEach(matches::add);

		// Wrap each matching ModuleInfo in a ModuleSearchResult.
		return matches.stream() //
			.map(info -> new ModuleSearchResult(info, baseDir)) //
			.collect(Collectors.toList());
	}

	// -- Helper methods --

	private boolean matches(final String actual, final String desired) {
		// TODO: Implement fuzzy matching option, and maybe case sensitive option.
		// Probably put it in the SearchService itself, and make an API toggle.
		return actual.toLowerCase().matches(".*" + desired + ".*");
	}
}
