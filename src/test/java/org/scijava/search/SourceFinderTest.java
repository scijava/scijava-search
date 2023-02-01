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
package org.scijava.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.log.LogService;

import ij.IJ;

public class SourceFinderTest {

	private Context context;
	private LogService logService;

	@Before
	public void setUp() {
		context = new Context(LogService.class);
		logService = context.service(LogService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	/**
	 * Tests {@link SourceFinder#sourceLocation(Class, LogService)} when the
	 * project uses the default {@code <sourceDirectory>}.
	 */
	@Test
	public void testSourceLocationDefaultSourceDirectory()
		throws SourceNotFoundException
	{
		URL url = SourceFinder.sourceLocation(Context.class, logService);
		String expected = "^/scijava/scijava-common/blob/scijava-common-[0-9\\.]+" +
			"/src/main/java/org/scijava/Context\\.java$";
		String actual = url.getPath();
		assertTrue("Unexpected path: " + actual, actual.matches(expected));
	}

	/**
	 * Tests {@link SourceFinder#sourceLocation(Class, LogService)} when the
	 * project has overridden the {@code <sourceDirectory>} property in its
	 * project POM.
	 */
	@Test
	public void testSourceLocationOverriddenSourceDirectory()
		throws SourceNotFoundException
	{
		URL url = SourceFinder.sourceLocation(IJ.class, logService);
		// NB: we expect the version as pinned in pom.xml
		String expected = "/imagej/ImageJ/blob/v1.54b/ij/IJ.java";
		assertEquals(expected, url.getPath());
	}
}
