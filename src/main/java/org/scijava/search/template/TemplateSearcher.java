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
