/*-
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

package org.scijava.search.module;

import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.UIDetails;
import org.scijava.input.Accelerator;
import org.scijava.module.ModuleInfo;
import org.scijava.search.SearchResult;

/**
 * Search result for the {@link ModuleSearcher}.
 *
 * @author Curtis Rueden
 */
public class ModuleSearchResult implements SearchResult {

	private final ModuleInfo info;
	private final Map<String, String> props;

	public ModuleSearchResult(final ModuleInfo info, final String baseDir) {
		this.info = info;

		props = new LinkedHashMap<>();
		final MenuPath menuPath = info.getMenuPath();
		if (menuPath != null && !menuPath.isEmpty()) {
			props.put("Menu path", getMenuPath(true));
			final MenuEntry menuLeaf = menuPath.getLeaf();
			if (menuLeaf != null) {
				final Accelerator accelerator = menuLeaf.getAccelerator();
				if (accelerator != null) {
					props.put("Shortcut", accelerator.toString());
				}
			}
		}
		props.put("Identifier", info.getIdentifier());
		props.put("Location", ModuleSearcher.location(info, baseDir));
	}

	public ModuleInfo info() {
		return info;
	}

	@Override
	public String name() {
		return ModuleSearcher.title(info);
	}

	@Override
	public String identifier() {
		return name();
	}

	@Override
	public String context() {
		return "/" + getMenuPath(name() != "" + info.getMenuPath().getLeaf(), "/");
	}

	@Override
	public String iconPath() {
		return ModuleSearcher.iconPath(info);
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String getMenuPath(boolean includeLeaf) {
		return getMenuPath(includeLeaf, " \u203a ");
	}

	private String getMenuPath(boolean includeLeaf, String separator) {
		final String menuRoot = info.getMenuRoot();
		final boolean isContextMenu = menuRoot != null && //
			!menuRoot.equals(UIDetails.APPLICATION_MENU_ROOT);
		final String prefix = isContextMenu ? "[" + menuRoot + "]" : "";
		final MenuPath menuPath = info.getMenuPath();
		if (menuPath == null) return prefix;
		final String menuString = menuPath.getMenuString(includeLeaf);
		return prefix + " " + menuString.replace(" > ", separator);
	}
}
