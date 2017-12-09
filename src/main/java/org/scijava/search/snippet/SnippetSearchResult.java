package org.scijava.search.snippet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.script.ScriptLanguage;
import org.scijava.search.SearchResult;
import org.scijava.search.module.ModuleSearcher;

/**
 * Search result for the {@link ModuleSearcher}.
 *
 * @author Curtis Rueden
 */
public class SnippetSearchResult implements SearchResult {

	private final ScriptLanguage language;
	private final String snippet;
	private final HashMap<String, String> props;

	public SnippetSearchResult(final ScriptLanguage language,
		final String snippet)
	{
		this.language = language;
		this.snippet = snippet;

		props = new HashMap<>();
		props.put("Language", language.getLanguageName());
		props.put("Nicknames", s(language.getNames()));
		props.put("Extensions", s(language.getExtensions()));
		props.put("Engine Name", language.getEngineName());
		props.put("Engine Version", language.getEngineVersion());
		props.put("MIME Types", s(language.getMimeTypes()));
	}

	public ScriptLanguage language() {
		return language;
	}

	public String snippet() {
		return snippet;
	}

	@Override
	public String name() {
		return language.getLanguageName() + ": " + snippet;
	}

	@Override
	public String iconPath() {
		return null;
//		return "/icons/" + language.getNames().get(0);
	}

	@Override
	public Map<String, String> properties() {
		return props;
	}

	// -- Helper methods --

	private String s(final List<String> names) {
		if (names == null || names.isEmpty()) return "<None>";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.size(); i++) {
			if (i > 0) sb.append(", ");
			sb.append(names.get(i));
		}
		return sb.toString();
	}
}
