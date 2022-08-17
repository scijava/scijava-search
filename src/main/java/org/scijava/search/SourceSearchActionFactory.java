/*
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2021 SciJava developers.
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
