
package org.scijava.search;

import org.scijava.log.LogService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;
import org.scijava.search.SourceFinder;
import org.scijava.search.SourceNotFoundException;
import org.scijava.ui.UIService;

import java.io.IOException;
import java.net.URL;

public abstract class SourceSearchActionFactory implements SearchActionFactory {

	@Parameter
	private LogService log;

	@Parameter
	private UIService uiService;

	@Parameter
	private PlatformService platformService;

	public abstract Class<?> classFromSearchResult(final SearchResult result);

	@Override
	public SearchAction create(final SearchResult result) {
		return new DefaultSearchAction("Source", //
			() -> source(classFromSearchResult(result)));
	}

	private void source(final Class<?> c) {
		URL sourceLocation = null;
		try {
			sourceLocation = SourceFinder.sourceLocation(c, log);
		}
		catch (final SourceNotFoundException exc) {
			log.error(exc);
		}
		if (sourceLocation == null) {
			uiService.showDialog("Source location unknown for " + c.getName());
			return;
		}
		try {
			platformService.open(sourceLocation);
		}
		catch (final IOException exc) {
			log.error(exc);
			uiService.showDialog("Platform error opening source URL: " +
				sourceLocation);
		}
	}

}
