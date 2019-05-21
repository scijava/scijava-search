/*
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2019 SciJava developers.
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

import org.scijava.plugin.AbstractSingletonService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.prefs.PrefService;
import org.scijava.service.Service;

/**
 * Default implementation of {@link SearchService}.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultSearchService extends
	AbstractSingletonService<SearchActionFactory> implements SearchService
{

	@Parameter
	private PrefService prefService;

	@Override
	public boolean enabled(final Searcher s) {
		final String enabled = prefService.get(s.getClass(), "enabled");
		if (enabled != null) return Boolean.valueOf(enabled);
		// Get the default value from enabled property of PluginInfo.
		final PluginInfo<Searcher> info = //
			pluginService().getPlugin(s.getClass(), Searcher.class);
		return info == null ? false : info.isEnabled();
	}

	@Override
	public void setEnabled(final Searcher s, final boolean enabled) {
		prefService.put(s.getClass(), "enabled", enabled);
	}
}
