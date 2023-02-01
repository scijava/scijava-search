/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2023 SciJava developers.
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

package org.scijava.search.module;

import org.scijava.module.ModuleInfo;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;
import org.scijava.search.SourceSearchActionFactory;

/**
 * Search action for viewing the source code of a Java class described by a
 * {@link ModuleInfo}.
 *
 * @author Gabriel Selzer
 */
@Plugin(type = SearchActionFactory.class)
public class ModuleSourceSearchActionFactory extends SourceSearchActionFactory {

	@Override
	public boolean supports(final SearchResult result) {
		if (!(result instanceof ModuleSearchResult)) return false;
		ModuleInfo info = ((ModuleSearchResult) result).info();
		try {
			info.loadDelegateClass();
			return true;
		}
		catch (ClassNotFoundException exc) {
			return false;
		}
	}

	@Override
	protected Class<?> classFromSearchResult(SearchResult result) {
		try {
			ModuleInfo info = ((ModuleSearchResult) result).info();
			return info.loadDelegateClass();
		}
		catch (ClassNotFoundException exc) {
			throw new IllegalArgumentException("Cannot load class for SearchResult " +
				result, exc);
		}
	}
}
