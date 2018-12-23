/*
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2018 SciJava developers.
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

package org.scijava.search.classes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.app.AppService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;
import org.scijava.util.Types;

/**
 * {@link Searcher} plugin for Java classes.
 *
 * @author Curtis Rueden
 */
@Plugin(type = Searcher.class)
public class ClassesSearcher implements Searcher {

	@Parameter
	private AppService appService;

	@Parameter
	private LogService log;

	@Override
	public String title() {
		return "Classes";
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		if (text.isEmpty()) return Collections.emptyList();

		final String baseDir = //
			appService.getApp().getBaseDirectory().getAbsolutePath();

		final LinkedHashSet<Class<?>> matches = new LinkedHashSet<>();

		// Get the list of all classes for consideration.
		final List<Class<?>> classes = getClasses();

		final String textLower = text.toLowerCase();

		// First, add classes where name starts with the text.
		classes.stream() //
			.filter(c -> startsWith(c, textLower)) //
			.forEach(matches::add);

		// Next, add classes where name has text inside somewhere.
		classes.stream() //
			.filter(c -> hasSubstring(c, textLower)) //
			.forEach(matches::add);

		// Wrap each matching Class in a ClassSearchResult.
		return matches.stream() //
			.map(c -> new ClassSearchResult(c, baseDir)) //
			.collect(Collectors.toList());
	}

	// -- Utility methods --

	/** Gets an abbreviated location for the given class. */
	public static String location(final Class<?> c, final String baseDir) {
		String path = Types.location(c).toString();
		if (path == null) return null;
		if (path.startsWith("file:/")) path = path.replaceFirst("file:/+", "/");
		if (baseDir != null && path.startsWith(baseDir)) {
			if (path.length() == baseDir.length()) return "";
			path = path.substring(baseDir.length() + 1);
		}
		return path;
	}

	// -- Helper methods --

	private List<Class<?>> getClasses() {
		// HACK: Obtain the list of all classes loaded by the current class loader.
		// Maybe this is not ideal, because it won't find classes that have not
		// yet been loaded. Perhaps we should dig through all the JAR files...
		final ClassLoader classLoader = //
			Thread.currentThread().getContextClassLoader();
		Object value = null;
		try {
			final Field classesField = ClassLoader.class.getDeclaredField("classes");
			classesField.setAccessible(true);
			value = classesField.get(classLoader);
		}
		catch (final NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException exc)
		{
			log.debug(exc);
		}
		if (!(value instanceof List)) return Collections.emptyList();
		// NB: Unsafe, since the value might be a list containing non-Class objects.
		// But in practice, we know it will be OK. Iterating to check would be slow.
		@SuppressWarnings("unchecked")
		final List<Class<?>> classes = (List<Class<?>>) value;
		return new ArrayList<>(classes); // NB: Copy avoids concurrency issues.
	}

	private boolean startsWith(final Class<?> c, final String desiredLower) {
		return c.getName().toLowerCase().startsWith(desiredLower) || //
			c.getSimpleName().toLowerCase().startsWith(desiredLower);
	}

	private boolean hasSubstring(final Class<?> c, final String desiredLower) {
		return c.getName().toLowerCase().matches(".*" + desiredLower + ".*");
	}
}
