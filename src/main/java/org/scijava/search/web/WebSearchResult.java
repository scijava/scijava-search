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

	protected final String details;
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
	public String identifier() {
		return name();
	}

	@Override
	public String iconPath() {
		return iconPath;
	}
	
	public String details() {
		return details;
	}

	@Override
	public Map<String, String> properties() {
		final HashMap<String, String> properties = new HashMap<>();
//		properties.put("name", name);
//		properties.put("iconpath", iconPath);
		properties.put("url", url);
		properties.put(null, details);
		return properties;
	}
}
