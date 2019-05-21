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

package org.scijava.search.module;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.Priority;
import org.scijava.app.AppService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

/**
 * {@link Searcher} plugin for SciJava modules.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Searcher.class, priority = Priority.VERY_HIGH)
public class ModuleSearcher implements Searcher {

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private AppService appService;

	@Override
	public String title() {
		// NB: A misnomer, but it's the term users are familiar with.
		return "Commands";
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		if (text.isEmpty()) return Collections.emptyList();

		final String baseDir = //
			appService.getApp().getBaseDirectory().getAbsolutePath();

		final LinkedHashSet<ModuleInfo> matches = new LinkedHashSet<>();

		// Get the list of all modules for consideration.
		final List<ModuleInfo> modules = moduleService.getModules().stream() //
			.filter(this::isGoodModule) //
			.collect(Collectors.toList());

		final String textLower = text.toLowerCase();
		final List<String> textLowerParts = Arrays.asList(textLower.split("\\s+"));

		// Add modules where title starts with the text.
		modules.stream() //
			.filter(info -> startsWith(info, textLower) ) //
			.forEach(matches::add);

		// Add modules where title has text inside somewhere.
		modules.stream() //
			.filter(info -> hasSubstringInTitle(info, textLower)) //
			.forEach(matches::add);

		// Add modules where menu path has text inside somewhere.
		modules.stream() //
			.filter(info -> hasSubstringInMenu(info, textLower)) //
			.forEach(matches::add);

		// Add modules where title has all parts of the text inside somewhere.
		modules.stream() //
				.filter(info -> hasSubstringsInTitle(info, textLowerParts)) //
				.forEach(matches::add);

		// Add modules where menu path has all parts of the text inside somewhere.
		modules.stream() //
				.filter(info -> hasSubstringsInMenu(info, textLowerParts)) //
				.forEach(matches::add);

		// Wrap each matching ModuleInfo in a ModuleSearchResult.
		return matches.stream() //
			.map(info -> new ModuleSearchResult(info, baseDir)) //
			.collect(Collectors.toList());
	}

	// -- Utility methods --

	/**
	 * Gets a human-readable title for the module, or null if none.
	 * <p>
	 * We do not use {@link ModuleInfo#getTitle()} because that method tries very
	 * hard to return something in every case, whereas we only want to give really
	 * nice titles, or null if the module is inappropriate.
	 * </p>
	 */
	public static String title(final ModuleInfo info) {
		// use object label, if available
		final String label = info.getLabel();
		if (label != null && !label.isEmpty()) return label;

		// use name of leaf menu item, if available
		final MenuPath menuPath = info.getMenuPath();
		if (menuPath != null && menuPath.size() > 0) {
			final MenuEntry menuLeaf = menuPath.getLeaf();
			final String menuName = menuLeaf.getName();
			if (menuName != null && !menuName.isEmpty()) return menuName;
		}

		return null;
	}

	/** Gets the icon path associated with the given module. */
	public static String iconPath(final ModuleInfo info) {
		final String iconPath = info.getIconPath();
		if (iconPath != null) return iconPath;
		final MenuPath menuPath = info.getMenuPath();
		return menuPath == null || menuPath.getLeaf() == null ? //
			null : menuPath.getLeaf().getIconPath();
	}

	/** Gets an abbreviated location for the given module. */
	public static String location(final ModuleInfo info, final String baseDir) {
		String path = info.getLocation();
		if (path == null) return null;
		if (path.startsWith("file:/")) path = path.replaceFirst("file:/+", "/");
		if (baseDir != null && path.startsWith(baseDir)) {
			if (path.length() == baseDir.length()) return "";
			path = path.substring(baseDir.length() + 1);
		}
		return path;
	}

	// -- Helper methods --

	private boolean isGoodModule(final ModuleInfo info) {
		return info.isVisible() && info.isEnabled() && title(info) != null;
	}

	private boolean startsWith(final ModuleInfo info, final String desiredLower) {
		final String title = title(info);
		return title != null && title.toLowerCase().startsWith(desiredLower);
	}

	private boolean hasSubstringInTitle(final ModuleInfo info,
	                                    final String desiredLower)
	{
		final String title = title(info);
		return title != null && //
			title.toLowerCase().matches(".*" + desiredLower + ".*");
	}

	private boolean hasSubstringsInTitle(final ModuleInfo info,
	                                    final List<String> desiredLower)
	{
		final String title = title(info);
		if(title == null) return false;
		return desiredLower.stream().allMatch(part -> title.toLowerCase().contains(part));
	}

	private boolean hasSubstringInMenu(final ModuleInfo info,
	                                    final String desiredLower)
	{
		MenuPath menuPath = info.getMenuPath();
		if(menuPath == null) return false;
		return menuPath.stream().anyMatch(entry -> entry.getName().toLowerCase().contains(desiredLower));
	}

	private boolean hasSubstringsInMenu(final ModuleInfo info,
	                                    final List<String> desiredLower)
	{
		MenuPath menuPath = info.getMenuPath();
		if(menuPath == null) return false;
		return desiredLower.stream().allMatch(part -> menuPath.stream().anyMatch(entry -> entry.getName().toLowerCase().contains(part)));
	}
}
