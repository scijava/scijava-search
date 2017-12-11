
package org.scijava.search;

/**
 * Default implementation of {@link SearchAction}.
 *
 * @author Curtis Rueden
 */
public class DefaultSearchAction implements SearchAction {

	private final String label;
	private final Runnable r;
	private final boolean closeSearch;

	public DefaultSearchAction(final String label, final Runnable r, final boolean closeSearch) {
		this.label = label;
		this.r = r;
		this.closeSearch = closeSearch;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public void run() {
		r.run();
	}
	
	@Override 
	public boolean willCloseSearch(){
		return closeSearch;
	}
}
