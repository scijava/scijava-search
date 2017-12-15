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

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

/**
 * A searcher for the <a href="http://forum.imagej.net/">ImageJ Forum</a>.
 *
 * @author Robert Haase (MPI-CBG)
 */
@Plugin(type = Searcher.class, name = "ImageJ Forum")
public class ImageJForumSearcher extends AbstractWebSearcher {
	
	@Parameter
	private LogService log;

	public ImageJForumSearcher() {
		super("ImageJ Forum");
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		try {
			final URL url = new URL("http://forum.imagej.net/search?q=" + //
				URLEncoder.encode(text, "utf-8") + "&source=imagej");
			String webSearchContent;
			try (final Scanner s = new Scanner(url.openStream())) {
				s.useDelimiter("\"topics\":");

				if (!s.hasNext()) return getSearchResults();
				s.next();
				if (!s.hasNext()) return getSearchResults();
				webSearchContent = s.next();
			}
			webSearchContent = webSearchContent.substring(webSearchContent.indexOf(
				"[{") + 2, webSearchContent.indexOf("}]"));

			final String[] results = webSearchContent.split("\\},\\{");
			for (final String result : results) {
				final HashMap<String, String> metaInfo = parseForumSearchResult(result);

				final String forumPostUrl = "http://forum.imagej.net/t/" + metaInfo.get(
					"slug") + "/" + metaInfo.get("id") + "/";

				final String details = "Tags: " +
					metaInfo.get("tags") + "<br />" + "Created: " + metaInfo.get(
						"created_at") + "<br />" + "Last posted: " + metaInfo.get(
							"last_posted_at");

				addResult(metaInfo.get("title"), "", forumPostUrl, details);
			}
		}
		catch (final IOException e) {
			log.debug(e);
		}
		return getSearchResults();
	}

	HashMap<String, String> parseForumSearchResult(String content) {
		content = content + ",";
		final HashMap<String, String> map = new HashMap<>();
		String currentKey = "";
		String currentValue = "";
		boolean readString = false;
		boolean readKey = true;
		boolean tagParentheses = false;
		String currentChar = " ";
		String previousChar = " ";
		for (int i = 0; i < content.length(); i++) {
			previousChar = currentChar;
			currentChar = content.substring(i, i + 1);

			if (currentChar.equals("\"")) {
				if (!previousChar.equals("\\")) {
					readString = !readString;
					continue;
				}
			}
			if (!readString) {
				if (currentChar.equals(":")) {
					readKey = false;
					continue;
				}
				if (currentChar.equals(",") && !tagParentheses) {
					readKey = true;
					map.put(currentKey, currentValue);
					currentKey = "";
					currentValue = "";
					continue;
				}
			}

			if (readString) {
				if (readKey) {
					currentKey = currentKey + currentChar;
					continue;
				}
			}
			if(currentChar.equals("[")){
				tagParentheses = true;
			}
			if(currentChar.equals("]") && tagParentheses){
				tagParentheses = false;
			}
			currentValue = currentValue + currentChar;

		}
		return map;
	}

}
