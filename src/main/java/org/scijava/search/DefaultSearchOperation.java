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

package org.scijava.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

	private final SearchListener[] listeners;
	private final List<SearchAttempt> currentSearches = new ArrayList<>();

	@Parameter
	private SearchService searchService;

	@Parameter
	private PluginService pluginService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService log;

	private boolean active = true;

	private String query;
	private boolean fuzzy;
	private long lastModifyTime;

	public DefaultSearchOperation(final Context context, final SearchListener... callbacks) {
		listeners = callbacks;
		context.inject(this);
		threadService.run(() -> {
			while (active) {
				try {
					Thread.sleep(50);
				}
				catch (final InterruptedException exc) {
					log.error(exc);
				}
				if (lastModifyTime == 0) continue; // nothing modified yet!
				if (System.currentTimeMillis() - lastModifyTime < DELAY) {
					// Not enough time elapsed since last modification; wait longer.
					continue;
				}
				lastModifyTime = 0;

				// Time to start a new search! Spawn one new thread per searcher.
				cancelCurrentSearches();
				for (final Searcher searcher : searchers()) {
					final SearchAttempt search = new SearchAttempt(searcher);
					currentSearches.add(search);
					threadService.run(search);
				}
			}
			cancelCurrentSearches();
		});
	}

	@Override
	public void setFuzzy(boolean fuzzy) {
		this.fuzzy = fuzzy;
		refreshModifyTime();
	}

	@Override
	public void search(final String text) {
		query = text;
		refreshModifyTime();
	}

	@Override
	public void terminate() {
		active = false;
	}

	// -- Helper methods --

	private List<Searcher> searchers() {
		final List<Searcher> searchers = //
			pluginService.createInstancesOfType(Searcher.class);

		// Check for a searcher that wants exclusive rights.
		final Optional<Searcher> exclusive = searchers.stream().filter(
			searcher -> searcher.exclusive(query)).findFirst();

		return exclusive.isPresent() ? //
			Collections.singletonList(exclusive.get()) : searchers;
	}

	private void cancelCurrentSearches() {
		currentSearches.forEach(search -> search.invalidate());
		currentSearches.clear();
	}

	private void refreshModifyTime() {
		lastModifyTime = System.currentTimeMillis();
	}

	// -- Helper classes  --

	private class SearchAttempt implements Runnable {
		private Searcher searcher;
		private boolean valid = true;

		private SearchAttempt(final Searcher searcher) {
			this.searcher = searcher;
		}

		public void invalidate() {
			valid = false;
		}

		@Override
		public void run() {
			final boolean exclusive = searcher.exclusive(query);
			final boolean supported = searcher.supports(query);
			final boolean enabled = searchService.enabled(searcher);
			if (!valid) return;
			final List<SearchResult> results = supported ? (enabled ? //
				searcher.search(query, fuzzy) : Collections.emptyList()) : null;
			if (!valid) return;
			for (final SearchListener l : listeners) {
				l.searchCompleted(new SearchEvent(searcher, results, exclusive));
			}
		}
	}
}
