
package org.scijava.search;

import java.util.ArrayList;
import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.thread.ThreadService;

/**
 * Default implementation of {@link SearchOperation}.
 * 
 * @author Curtis Rueden
 */
public class DefaultSearchOperation implements SearchOperation {

	/** Delay in milliseconds before invoking the searchers. */
	private static final int DELAY = 200;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService log;

	private SearchListener[] listeners;

	private List<SearchAttempt> currentSearches = new ArrayList<>();

	private boolean active = true;

	private String query;
	private boolean fuzzy;
	private long lastSearchTime;
	private long lastModifyTime;

	public DefaultSearchOperation(final Context context, final SearchListener... callbacks) {
		listeners = callbacks;
		context.inject(this);
		threadService.run(() -> {
			while (active) {
				if (lastModifyTime - lastSearchTime > DELAY) {
					// Time to start a new search! Spawn one new thread per searcher.
					cancelCurrentSearches();
					for (final Searcher searcher : searchers()) {
						final SearchAttempt search = new SearchAttempt(searcher);
						currentSearches.add(search);
						threadService.run(search);
					}
					lastSearchTime = System.currentTimeMillis();
				}
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException exc) {
					log.error(exc);
				}
			}
			cancelCurrentSearches();
		});
	}

	@Override
	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
		lastModifyTime = System.currentTimeMillis();
	}

	@Override
	public void search(final String text) {
		query = text;
		lastModifyTime = System.currentTimeMillis();
	}

	@Override
	public void terminate() {
		active = false;
	}

	// -- Helper methods --

	private List<Searcher> searchers() {
		return pluginService.createInstancesOfType(Searcher.class);
	}

	private void cancelCurrentSearches() {
		currentSearches.forEach(search -> search.invalidate());
		currentSearches.clear();
	}

	// -- Helper classes  --

	private class SearchAttempt implements Runnable {
		private Searcher searcher;
		private boolean valid = true;

		private SearchAttempt(Searcher searcher) {
			this.searcher = searcher;
		}

		public void invalidate() {
			valid = false;
		}

		@Override
		public void run() {
			final List<SearchResult> results = searcher.search(query, fuzzy);
			if (!valid) return;
			for (final SearchListener l : listeners) {
				l.searchCompleted(searcher, results);
			}
		}
	}
}
