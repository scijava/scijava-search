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

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.command.CommandInfo;
import org.scijava.module.ModuleInfo;
import org.scijava.platform.AppEventService;
import org.scijava.platform.Platform;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.PluginInfo;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;

/**
 * Tests {@link HelpSearchActionFactory}
 *
 * @author Gabriel Selzer
 */
public class HelpSearchActionTest {

	private AppService appService;
	private SearchActionFactory factory = new HelpSearchActionFactory();

	@Before
	public void init() {
		Context context = new Context();
		appService = context.getService(AppService.class);
		factory = new HelpSearchActionFactory();
	}

	@Test
	public void testSupports() {
		final SearchResult good = createTestResult();
		assertTrue(factory.supports(good));
		final SearchResult bad = new SearchResult() {

			@Override
			public String name() {
				return null;
			}

			@Override
			public String iconPath() {
				return null;
			}

			@Override
			public Map<String, String> properties() {
				return null;
			}
		};
		assertFalse(factory.supports(bad));
	}

	@Test
	public void testCreate() {
		final SearchResult result = createTestResult();
		SearchAction action = factory.create(result);
		assertTrue(action instanceof DefaultSearchAction);
		assertEquals("Help", action.toString());
	}

	@Test
	public void testHelp() throws NoSuchFieldException, IllegalAccessException {
		final SearchResult result = createTestResult();
		final MockPlatformService mockService = new MockPlatformService();
		Field platformService = factory.getClass().getDeclaredField(
			"platformService");
		platformService.setAccessible(true);
		platformService.set(factory, mockService);
		final SearchAction action = factory.create(result);
		action.run();
		assertEquals(1, mockService.getOpenedURLs().size());
		assertEquals("https://github.com/scijava/scijava-search", mockService
			.getOpenedURLs().get(0));
	}

	private SearchResult createTestResult() {
		ModuleInfo info = new CommandInfo(TestCommand.class);
		return new ModuleSearchResult(info, appService.getApp().getBaseDirectory()
			.getAbsolutePath());
	}

	private static class MockPlatformService implements PlatformService {

		private final List<String> openedURLs;

		public MockPlatformService() {
			openedURLs = new ArrayList<>();
		}

		@Override
		public List<Platform> getTargetPlatforms() {
			return null;
		}

		@Override
		public void open(URL url) {
			openedURLs.add(url.toString());
		}

		public List<String> getOpenedURLs() {
			return openedURLs;
		}

		@Override
		public int exec(String... args) {
			return 0;
		}

		@Override
		public boolean registerAppMenus(Object menus) {
			return false;
		}

		@Override
		public AppEventService getAppEventService() {
			return null;
		}

		@Override
		public List<Platform> getInstances() {
			return null;
		}

		@Override
		public <P extends Platform> P getInstance(Class<P> pluginClass) {
			return null;
		}

		@Override
		public Class<Platform> getPluginType() {
			return null;
		}

		@Override
		public Context context() {
			return null;
		}

		@Override
		public Context getContext() {
			return null;
		}

		@Override
		public double getPriority() {
			return 0;
		}

		@Override
		public void setPriority(double priority) {

		}

		@Override
		public PluginInfo<?> getInfo() {
			return null;
		}

		@Override
		public void setInfo(PluginInfo<?> info) {

		}
	}

}
