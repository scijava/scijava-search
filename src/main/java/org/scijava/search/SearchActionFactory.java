
package org.scijava.search;

import org.scijava.plugin.FactoryPlugin;

/**
 * TODO
 *
 * @author Curtis Rueden
 */
public interface SearchActionFactory extends
	FactoryPlugin<SearchResult, SearchAction>
{

	@Override
	default Class<SearchResult> getType() {
		return SearchResult.class;
	}
}
