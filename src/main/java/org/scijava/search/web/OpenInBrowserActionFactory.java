
package org.scijava.search.web;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

	@Override
	public boolean supports(final SearchResult result) {
		return result instanceof WebSearchResult;
	}

	@Override
	public SearchAction create(final SearchResult result) {
		return new DefaultSearchAction("Open in Browser", () -> {
			try {
				Desktop.getDesktop().browse(new URI(result.properties().get("url")));
			}
			catch (final IOException e1) {
				e1.printStackTrace();
			}
			catch (final URISyntaxException e1) {
				e1.printStackTrace();
			}
		});
	}
}