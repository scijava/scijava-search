
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
	private LogService logService;

	public ImageJForumSearcher() {
		super("ImageJ Forum");
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		try {
			final URL url = new URL("http://forum.imagej.net/search?q=" + URLEncoder
				.encode(text) + "&source=imagej");
			final Scanner s = new Scanner(url.openStream());
			s.useDelimiter("\"topics\":");

			String webSearchContent;
			if (!s.hasNext()) return getSearchResults();
			s.next();
			if (!s.hasNext()) return getSearchResults();
			webSearchContent = s.next();
			webSearchContent = webSearchContent.substring(webSearchContent.indexOf(
				"[{") + 2, webSearchContent.indexOf("}]"));

			final String[] results = webSearchContent.split("\\},\\{");
			for (final String result : results) {
				final HashMap<String, String> metaInfo = parseForumSearchResult(result);

				final String forumPostUrl = "http://forum.imagej.net/t/" + metaInfo.get(
					"slug") + "/" + metaInfo.get("id") + "/";

				final String details = "Url: " + forumPostUrl + "\n" + "Tags: " +
					metaInfo.get("tags") + "\n" + "Created: " + metaInfo.get(
						"created_at") + "\n" + "Last posted: " + metaInfo.get(
							"last_posted_at") + "\n";

				addResult(metaInfo.get("title"), "", forumPostUrl, details);
			}
		}
		catch (final IOException e) {
			logService.log().debug(e);
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
				if (currentChar.equals(",")) {
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
			currentValue = currentValue + currentChar;

		}
		return map;
	}

}
