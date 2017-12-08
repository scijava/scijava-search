package org.scijava.search.module;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.app.AppService;
import org.scijava.module.ModuleInfo;
import org.scijava.plugin.Parameter;
import org.scijava.search.SearchResult;
import org.scijava.util.ClassUtils;
import org.scijava.util.FileUtils;

/**
 * Search result for the {@link ModuleSearcher}.
 *
 * @author Curtis Rueden
 */
public class ModuleSearchResult implements SearchResult {

	@Parameter
	private AppService appService;

	private final ModuleInfo info;
	private HashMap<String, String> props;

	public ModuleSearchResult(final ModuleInfo info) {
		this.info = info;

		props = new HashMap<>();
		props.put("Hello", "World");
		props.put("Title", info.getTitle());
		final MenuPath menuPath = info.getMenuPath();
		if (menuPath != null) {
			props.put("Menu path", menuPath.getMenuString(false));
			final MenuEntry menuLeaf = menuPath.getLeaf();
			if (menuLeaf != null) {
				props.put("Shortcut", menuLeaf.getAccelerator().toString());
			}
		}
		props.put("Identifier", info.getIdentifier());
		props.put("Location", getLocation());
	}

	public ModuleInfo info() { return info; }

	@Override
	public String name() {
		return info.getTitle();
	}

	@Override
	public String iconPath() {
		final String iconPath = info.getIconPath();
		return iconPath != null ? iconPath : //
			info.getMenuPath().getLeaf().getIconPath();
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String getLocation() {
		final URL location = ClassUtils.getLocation(info.getDelegateClassName());
		final File file = FileUtils.urlToFile(location);
		if (file == null) return null;
		final String path = file.getAbsolutePath();
		if (path == null) return null;
		final String baseDir = //
			appService.getApp().getBaseDirectory().getAbsolutePath();
		if (path.startsWith(baseDir)) {
			if (path.length() == baseDir.length()) return "";
			return path.substring(baseDir.length() + 1);
		}
		return path;
	}
}
