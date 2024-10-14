/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2024 SciJava developers.
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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.search.DefaultSearchAction;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchActionFactory;
import org.scijava.search.SearchResult;
import org.scijava.search.SearchService;
import org.scijava.search.classes.ClassSearchResult;

/**
 * Tests that duplicate labels are removed with the {@link SearchService}
 *
 * @author Gabriel Selzer
 */
public class DuplicateLabelsTest {

	/**
	 * Used to allow the search actions to change state. Run actions can't return
	 * anything, so we need another way to track that they run.
	 */
	public static Integer state = 0;

	private SearchService searchService;

	@Before
	public void init() {
		Context context = new Context();
		searchService = context.getService(SearchService.class);
	}

	@Test
	public void testDuplicateLabelRemoval() {
		SearchResult dummyResult = new ClassSearchResult(this.getClass(), "");
		List<SearchAction> actions = searchService.actions(dummyResult);
		actions.removeIf(a -> !a.toString().equals("test"));
		Assert.assertEquals(1, actions.size());
		actions.get(0).run();
		assert DuplicateLabelsTest.state == 1;
	}

	@Plugin(type = SearchActionFactory.class, priority = Priority.HIGH)
	public static class TestSearchActionFactoryHigh implements
		SearchActionFactory
	{

		@Override
		public SearchAction create(SearchResult data) {
			return new DefaultSearchAction("test", //
				() -> DuplicateLabelsTest.state = 1 //
			);
		}
	}

	@Plugin(type = SearchActionFactory.class, priority = Priority.LOW)
	public static class TestSearchActionFactoryLow implements
		SearchActionFactory
	{

		@Override
		public SearchAction create(SearchResult data) {
			return new DefaultSearchAction("test", //
				() -> DuplicateLabelsTest.state = 2 //
			);
		}

	}

}
