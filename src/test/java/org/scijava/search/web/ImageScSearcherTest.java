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
package org.scijava.search.web;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.search.SearchResult;

/**
 * Tests {@link ImageScSearcher}.
 *
 * @author Curtis Rueden
 */
public class ImageScSearcherTest {

	@Test
	public void testSimpleSearch() {
		try (final Context ctx = new Context(LogService.class)) {
			final String query = "before:2015-12-31 in:title bonej";
			final ImageScSearcher searcher = new ImageScSearcher();
			ctx.inject(searcher);
			final List<SearchResult> results = searcher.search(query, false);
			assertEquals(3, results.size());
			assertEquals("https://forum.image.sc/t/251/1", results.get(0).properties().get("URL"));
			assertEquals("https://forum.image.sc/t/465/1", results.get(1).properties().get("URL"));
			assertEquals("https://forum.image.sc/t/162/1", results.get(2).properties().get("URL"));
		}
	}
}
