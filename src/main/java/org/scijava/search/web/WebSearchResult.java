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

package org.scijava.search.web;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.search.SearchResult;

/**
 * This class represents a typical web search result being represented by a
 * name/title of a website, an image (icon), the url of the website and some
 * text from the website as preview of its content.
 *
 * @author Robert Haase (MPI-CBG)
 * @author Curtis Rueden
 */
public class WebSearchResult implements SearchResult {

	private static final String DEFAULT_ICON = "/icons/search/world_link.png";

	private final String name;
	private final String iconPath;
	private final String url;
	private final Map<String, String> props;

	public WebSearchResult(final String name, final String url,
		final String details)
	{
		this(name, url, details, null, null);
	}

	public WebSearchResult(final String name, final String url,
		final String details, final String iconPath,
		final Map<String, String> extraProps)
	{
		this.name = name;
		this.url = url;
		this.iconPath = iconPath == null ? DEFAULT_ICON : iconPath;

		props = new LinkedHashMap<>();
		props.put(null, details);
		props.put("URL", url);
		if (extraProps != null) props.putAll(extraProps);
	}

	public String url() {
		return url;
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
		return props;
	}
}
