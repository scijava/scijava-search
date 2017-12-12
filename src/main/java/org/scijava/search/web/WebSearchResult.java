
package org.scijava.search.web;

import java.util.HashMap;
import java.util.Map;

import org.scijava.search.SearchResult;

/**
 * This class represents a typical web search result being represented by a
 * name/title of a website, an image (icon), the url of the website and some
 * text from the website as preview of its content.
 * 
 * @author Robert Haase (MPI-CBG)
 */
public class WebSearchResult implements SearchResult {

	private final String details;
	String name;
	String iconPath;
	String url;

	public WebSearchResult(final String name, final String iconPath,
		final String url, final String details)
	{
		this.name = name;
		this.iconPath = iconPath;
		this.url = url;
		this.details = details;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String iconPath() {
		return iconPath;
	}

	@Override
	public Map<String, String> properties() {
		final HashMap<String, String> properties = new HashMap<>();
		properties.put("name", name);
//		properties.put("iconpath", iconPath);
		properties.put("url", url);
		properties.put(null, "<body style='font-size: 11pt; font-family: Arial; background-color: #f4f4f7; padding: 10px 5px;'>.. " + details + " ..</body>");
		return properties;
	}
}
