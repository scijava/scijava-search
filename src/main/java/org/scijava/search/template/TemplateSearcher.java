/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2021 SciJava developers.
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
package org.scijava.search.template;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.app.AppService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;
import org.scijava.util.FileUtils;

@Plugin(type = Searcher.class, priority = Priority.VERY_HIGH - 10)
public class TemplateSearcher implements Searcher {
	
	@Parameter
	private AppService appService;

	@Override
	public String title() {
		return "Script templates";
	}

	@Override
	public List<SearchResult> search(String text, boolean fuzzy) {
		// Get list of all templates in possible template paths
		// templateService.getTemplates() ??
		final String templatePath = "script_templates";
		File baseDir = appService.getApp().getBaseDirectory();

		Map<String, URL> templates = FileUtils.findResources(null, templatePath, baseDir);

		LinkedHashMap<String, URL> matches = new LinkedHashMap<>();

		// Filter those templates with name matching text
		templates.entrySet().stream() //
				.filter(entry -> entry.getKey().toLowerCase().contains(text.toLowerCase()))
				.forEach(entry -> matches.put(entry.getKey(), entry.getValue()));

		// Wrap each template into a TemplateSearchResult
		return matches.entrySet().stream() //
				.map(entry -> new TemplateSearchResult(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}
}
