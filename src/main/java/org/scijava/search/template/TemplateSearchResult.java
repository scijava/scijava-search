package org.scijava.search.template;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.search.SearchResult;

public class TemplateSearchResult implements SearchResult {

	private final String name;
	private final URL url;

	public TemplateSearchResult(String name, URL url) {
		this.name = name;
		this.url = url;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Map<String, String> properties() {
		// Currently no properties defined
		return new LinkedHashMap<>();
	}

	@Override
	public String iconPath() {
		// Currently no iconPath available
		return null;
	}

	public URL url() {
		return url;
	}

}
