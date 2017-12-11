
package org.scijava.search.web;

import java.io.IOException;
import java.net.URL;

import org.scijava.log.LogService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;

/**
 * This factory creates actions for opening web search results in a browser.
 *
 * @author Robert Haase (MPI-CBG)
 */
@Plugin(type = SearchActionFactory.class)
public class OpenInBrowserActionFactory implements
	SearchActionFactory
{

	@Parameter
	private PlatformService platformService;

	@Parameter
	private LogService log;

	@Override
	public boolean supports(final SearchResult result) {
		return result instanceof WebSearchResult;
	}

	@Override
	public SearchAction create(final SearchResult result) {
		return new DefaultSearchAction("Open in Browser", //
			() -> openURL(result), true);
	}

	private void openURL(final SearchResult result) {
		try {
			final URL url = new URL(result.properties().get("url"));
			platformService.open(url);
		}
		catch (final IOException exc) {
			log.error(exc);
		}
	}
}
