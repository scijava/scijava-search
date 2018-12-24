/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 - 2018 SciJava developers.
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

package org.scijava.search.javadoc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.thread.ThreadService;

/**
 * Default implementation of {@link JavadocService}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultJavadocService extends AbstractService implements
	JavadocService
{

	@Parameter
	private ThreadService threadService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private LogService log;

	/** Mapping from Java class name to Javadoc URL. */
	private Map<String, String> classURLs;

	// -- JavadocService methods --

	@Override
	public String url(final String className) {
		return classURLs().get(className);
	}

	// -- Helper methods --

	private Map<String, String> classURLs() {
		if (classURLs == null) initClassURLs();
		return classURLs;
	}

	private synchronized void initClassURLs() {
		if (classURLs != null) return;
		classURLs = discoverJavadoc();
	}

	private Map<String, String> discoverJavadoc() {
		final Map<String, String> classLinks = new HashMap<>();
		final String prefix = "https://javadoc.scijava.org/";
		final Pattern iPattern = Pattern.compile(
			".*<a href=\"([A-Za-z0-9_-]+)/\">.*");
		final Pattern pPattern = Pattern.compile(".*<[Aa]" +
			" [Hh][Rr][Ee][Ff]=\"([^\"]+)\"" +
			" [Tt][Ii][Tt][Ll][Ee]=\"[^\"]* ([^\"]*)\"" +
			">(<[^>]+>)*([^<>]+)(<[^>]+>)*</[Aa]>.*");
		final Map<String, Future<List<String>>> futures = new LinkedHashMap<>();

		// Scan index page for project links.
		statusService.showStatus(0, 1, "Querying javadoc");
		for (final String line : lines(prefix + "index.html")) {
			final Matcher iMatcher = iPattern.matcher(line);
			if (!iMatcher.matches()) continue;
			// Found a project; download its allclasses-noframe.html asynchronously.
			final String project = iMatcher.replaceAll("$1");
			if (project.equals("Java6") || project.equals("Java7")) continue;
			futures.put(project, //
				threadService.run(() -> lines(prefix + project + "/allclasses-noframe.html")));
		}

		// Process the downloaded allclasses-noframe pages.
		int i = 0;
		final int max = futures.size() + 1;
		statusService.showProgress(++i, max);
		for (final String project : futures.keySet()) {
			try {
				statusService.showStatus(++i, max, "Processing javadoc: " + project);
				for (final String line : futures.get(project).get()) {
					final Matcher pMatcher = pPattern.matcher(line);
					if (!pMatcher.matches()) continue;
					// Found a class link; add it to the map.
					final String link = pMatcher.group(1);
					final String pkg = pMatcher.group(2);
					final String clazz = pMatcher.group(4).replace('.', '$');
					final String fqcn = "&lt;Unnamed&gt;".equals(pkg) ? clazz : pkg + "." + clazz;
					classLinks.putIfAbsent(fqcn, prefix + project + "/?" + link);
				}
			}
			catch (final InterruptedException | ExecutionException exc) {
				if (log.isDebug()) {
					log.debug("Error processing javadoc for " + project, exc);
				}
			}
		}
		statusService.clearStatus();
		log.debug("Discovered " + classLinks.size() + " javadoc class links");
		return classLinks;
	}

	/** Reads URL content as a list of lines with UTF-8 encoding. */
	private List<String> lines(final String url) {
		try (final BufferedReader r = new BufferedReader(new InputStreamReader(
			new URL(url).openStream(), StandardCharsets.UTF_8)))
		{
			final List<String> lines = r.lines().collect(Collectors.toList());
			if (log.isDebug()) {
				log.debug("Read " + lines.size() + " lines from URL: " + url);
			}
			return lines;
		}
		catch (final IOException exc) {
			if (log.isDebug()) log.debug("Error reading from URL: " + url, exc);
			return Collections.emptyList();
		}
	}
}
