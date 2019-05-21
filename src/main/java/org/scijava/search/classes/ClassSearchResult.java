/*-
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

package org.scijava.search.classes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.scijava.search.SearchResult;

/**
 * Search result for the {@link ClassesSearcher}.
 *
 * @author Curtis Rueden
 */
public class ClassSearchResult implements SearchResult {

	private final Class<?> c;
	private final String location;
	private final Map<String, String> props;

	public ClassSearchResult(final Class<?> c, final String baseDir) {
		this.c = c;
		location = ClassesSearcher.location(c, baseDir);

		props = new LinkedHashMap<>();
		props.put("Type", types());
		props.put("Modifiers", Modifier.toString(c.getModifiers()));
		props.put("Location", location == null ? "<unknown>" : location.toString());
	}

	public Class<?> clazz() {
		return c;
	}

	public String location() {
		return location;
	}

	@Override
	public String name() {
		return c.getName();
	}

	@Override
	public String identifier() {
		return c.getSimpleName();
	}

	@Override
	public String context() {
		final Package p = c.getPackage();
		return p == null ? "" : p.getName();
	}

	@Override
	public String iconPath() {
		// TODO
		return null;
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String types() {
		final List<String> types = new ArrayList<>();

		if (c.isAnonymousClass()) types.add("anonymous");
		if (c.isLocalClass()) types.add("local");
		if (c.isMemberClass()) types.add("member");
		if (c.isSynthetic()) types.add("synthetic");

		if (c.isArray()) types.add("array");
		if (c.isAnnotation()) types.add("annotation");
		if (c.isEnum()) types.add("enum");
		if (c.isInterface()) types.add("interface");
		if (c.isPrimitive()) types.add("primitive");
		if (!c.isArray() && !c.isEnum() && !c.isInterface() && !c.isPrimitive())
			types.add("class");

		return String.join(", ", types);
	}

}
