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
