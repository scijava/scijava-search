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
package org.scijava.search.module;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.input.Accelerator;
import org.scijava.module.ModuleInfo;
import org.scijava.search.SearchResult;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;

/**
 * Search result for the {@link ModuleSearcher}.
 *
 * @author Curtis Rueden
 */
public class ModuleSearchResult implements SearchResult {

	private final ModuleInfo info;
	private final String baseDir;
	private final Map<String, String> props;

	public ModuleSearchResult(final ModuleInfo info, final String baseDir) {
		this.info = info;
		this.baseDir = baseDir;

		props = new LinkedHashMap<>();
		props.put("Title", info.getTitle());
		final MenuPath menuPath = info.getMenuPath();
		if (menuPath != null) {
			props.put("Menu path", menuPath.getMenuString(false));
			final MenuEntry menuLeaf = menuPath.getLeaf();
			if (menuLeaf != null) {
				final Accelerator accelerator = menuLeaf.getAccelerator();
				if (accelerator != null) {
					props.put("Shortcut", accelerator.toString());
				}
			}
		}
		props.put("Identifier", info.getIdentifier());
		props.put("Location", getLocation());
	}

	public ModuleInfo info() { return info; }

	@Override
	public String name() {
		return info.getTitle();
	}

	@Override
	public String iconPath() {
		final String iconPath = info.getIconPath();
		return iconPath != null ? iconPath : //
			info.getMenuPath().getLeaf().getIconPath();
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String getLocation() {
		final URL location = ClassUtils.getLocation(info.getDelegateClassName());
		final File file = FileUtils.urlToFile(location);
		if (file == null) return null;
		final String path = file.getAbsolutePath();
		if (path == null) return null;
		if (path.startsWith(baseDir)) {
			if (path.length() == baseDir.length()) return "";
			return path.substring(baseDir.length() + 1);
		}
		return path;
	}
}
