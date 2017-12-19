
package org.scijava.search.web;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Deborah Schmidt
 */
public class ImageJForumSearchResult extends WebSearchResult {

	final HashMap<String, String> metaInfo;

	public ImageJForumSearchResult(final String name, final String iconPath,
		final String url, HashMap< String, String > metaInfo, final String details)
	{
		super(name, iconPath, url, details);
		this.metaInfo = metaInfo;
	}

	@Override
	public Map<String, String> properties() {
		final HashMap<String, String> properties = new HashMap<>();
//		properties.put("name", name);
//		properties.put("iconpath", iconPath);
		properties.put("url", url);
		properties.put("tags", metaInfo.get("tags"));
		properties.put("created / last posted", formatDate(metaInfo.get("created_at")) + " / " + formatDate(metaInfo.get("last_posted_at")));
		return properties;
	}
	
	public String formatDate(String datestr) {
		 Instant instant = Instant.parse(datestr);
		 LocalDateTime result = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
		return result.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}
}
