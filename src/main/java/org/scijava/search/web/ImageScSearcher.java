/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2024 SciJava developers.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonStreamParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.ocpsoft.prettytime.PrettyTime;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

@Plugin(type = Searcher.class, enabled = false)
public class ImageScSearcher implements Searcher {

	private static String URL_PREFIX = "https://forum.image.sc/search.json?q=";
	private static String POST_URL_PREFIX = "https://forum.image.sc/t";
	private static String FORUM_AVATAR_PREFIX = "https://forum.image.sc";
	private static String TERM_SUFFIX = " tags:imagej";

	@Parameter
	private LogService logService;

	@Override
	public String title() {
		return "Image.sc Forum";
	}

	@Override
	public List<SearchResult> search(String text, boolean fuzzy) {

		final List<SearchResult> searchResults = new ArrayList<>();

		try {
			final URL url = new URL(URL_PREFIX + URLEncoder.encode(text + TERM_SUFFIX, "utf-8"));

			// Pass the API key as a header parameter.
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Api-Username", "imagesc-bot");
			con.setRequestProperty("Api-Key", "b1a28dbb29c385e06026482661c8de55dd01972ff993bea6547783e52e8a017d");

			// Connect and read the result.
			InputStream is = con.getInputStream();
			try (InputStreamReader sr = new InputStreamReader(is, "UTF-8");
					BufferedReader reader = new BufferedReader(sr))
			{
				Map<String, String> topicTitleMap = new HashMap<>();
				Map<String, String> topicTagMap = new HashMap<>();
				JsonObject info = new JsonStreamParser(reader).next().getAsJsonObject();
				JsonArray topics = info.get("topics").getAsJsonArray();
				topics.forEach(t -> appendTopicMaps(t.getAsJsonObject(), topicTitleMap, topicTagMap));
				// TODO get posts and topics
				JsonArray posts = info.get("posts").getAsJsonArray();
				posts.forEach(p -> searchResults.add(createResult(p.getAsJsonObject(), topicTitleMap, topicTagMap)));
			}
		}
		catch (MalformedURLException exc) {
			logService.warn(exc);
		}
		catch (UnsupportedEncodingException exc) {
			logService.warn(exc);
		}
		catch (IOException exc) {
			logService.warn(exc);
		}

		return searchResults;
	}

	private void appendTopicMaps(JsonObject t,
		Map<String, String> topicTitleMap, Map<String, String> topicTagMap)
	{
		topicTitleMap.put(get(t, "id"), get(t, "title"));
		topicTagMap.put(get(t, "id"), String.join(", ", StreamSupport.stream(t.get("tags").getAsJsonArray().spliterator(), false).map(j -> j.getAsString()).collect(Collectors.toList())));
	}

	private SearchResult createResult(JsonObject post, Map<String, String> topics, Map<String, String> tags) {
		String title = topics.get(get(post, "topic_id"));
		String displayName = get(post, "name");
		displayName += displayName.isEmpty() ? get(post, "username") : " (" + get(post, "username") + ")";
		String iconPath = null;
		Map<String, String> extraProps = new LinkedHashMap<>();
		extraProps.put("Created", formatDate(get(post, "created_at")) + " by " + displayName);
		extraProps.put("Tags", tags.get(get(post, "topic_id")));
		extraProps.put("Likes", "\u2665 " + get(post, "like_count"));
		return new WebSearchResult(title, String.join("/", POST_URL_PREFIX, get(post, "topic_id"), get(post, "post_number")), get(post, "blurb"), iconPath, extraProps);
	}

	private String get(JsonObject post, String key) {
		return post.get(key).getAsString();
	}

	private String formatDate(final String datestr) {
		final Instant instant = Instant.parse(datestr);
		return new PrettyTime().format(Date.from(instant));
	}

	// Credit: https://www.baeldung.com/java-http-request
	public static String getParamsString(final Map<String, String> params)
		throws UnsupportedEncodingException
	{
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}

		String resultString = result.toString();
		return resultString.length() > 0 ? //
			resultString.substring(0, resultString.length() - 1) : resultString;
	}
}
