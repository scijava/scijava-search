/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2020 SciJava developers.
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

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.MenuPath;
import org.scijava.command.CommandInfo;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.search.SearchResult;
import org.scijava.search.Searcher;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ModuleSearcherTest {

	private ModuleService moduleService;
	private Searcher moduleSearcher;

	@Before
	public void init() throws InstantiableException {
		Context context = new Context();
		moduleService = context.getService(ModuleService.class);
		PluginInfo<Searcher> info = context.getService(PluginService.class).getPlugin(ModuleSearcher.class, Searcher.class);
		moduleSearcher = info.createInstance();
		context.inject(moduleSearcher);
	}

	@Test
	public void testMatchingBeginning() {
		createTestModule("Do something silly", "");
		List<SearchResult> results = moduleSearcher.search("Do", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "Do something silly"));
	}

	@Test
	public void testMatchingParts() {
		createTestModule("Do something silly", "");
		List<SearchResult> results = moduleSearcher.search("Do silly", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "Do something silly"));
	}

	@Test
	public void testMatchingEnd() {
		createTestModule("Do something silly", "");
		List<SearchResult> results = moduleSearcher.search("silly", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "Do something silly"));
	}

	@Test
	public void testCaseMismatch() {
		createTestModule("Do something silly", "");
		List<SearchResult> results = moduleSearcher.search("do", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "Do something silly"));
	}

	@Test
	public void testMenu() {
		createTestModule("nolabel", "Do>something>silly");
		List<SearchResult> results = moduleSearcher.search("silly", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "nolabel"));
	}

	@Test
	public void testMatchingPartsInMenu() {
		createTestModule("nolabel", "Do>something>silly");
		List<SearchResult> results = moduleSearcher.search("Do silly", true);
		assertNotNull(results);
		assertTrue(results.size()>=1);
		assertTrue(containsModule(results, "nolabel"));
	}

	@Test
	public void testNonMatchingPartsInMenu() {
		createTestModule("nolabel", "Do>something>silly");
		List<SearchResult> results = moduleSearcher.search("Do nothing", true);
		assertNotNull(results);
		assertFalse(containsModule(results, "nolabel"));
	}

	private boolean containsModule(List<SearchResult> results, String moduleName) {
		boolean foundModule = false;
		for(SearchResult result : results) {
			if(moduleName.equals(result.identifier())) foundModule = true;
		}
		return foundModule;
	}

	private void createTestModule(String label, String menuPath) {
		ModuleInfo info = new CommandInfo(TestCommand.class);
		info.setLabel(label);
		info.setMenuPath(new MenuPath(menuPath));
		moduleService.addModule(info);
	}

}
