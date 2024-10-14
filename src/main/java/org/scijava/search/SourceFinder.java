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

package org.scijava.search;

import java.net.MalformedURLException;
import java.net.URL;

import org.scijava.log.Logger;
import org.scijava.util.Manifest;
import org.scijava.util.POM;

/**
 * Static utility class for finding source URL of a given class.
 *
 * @author Curtis Rueden
 */
public final class SourceFinder {

	/**
	 * Discerns a URL where the source for a given class can be browsed.
	 * 
	 * @param c The class for which a source URL is desired.
	 * @param log The logger to use for any debug messages.
	 * @return URL of the class's source.
	 * @throws SourceNotFoundException If the source location cannot be found.
	 */
	public static URL sourceLocation(final Class<?> c, final Logger log)
		throws SourceNotFoundException
	{
		try {
			final POM pom = POM.getPOM(c);
			if (pom == null) {
				log.debug("No Maven POM found for class: " + c.getName());
				throw new SourceNotFoundException(c, null);
			}

			final String scmURL = pom.getSCMURL();
			if (scmURL == null) {
				if (log.isDebug()) log.debug("No <scm><url> for " + coord(pom));
				throw new SourceNotFoundException(c, null);
			}
			if (!scmURL.matches("^(git|http|https)://github.com/[^/]+/[^/]+/?$")) {
				log.debug("Not a standard GitHub project URL: " + scmURL);
				return new URL(scmURL);
			}

			// Try to extract a tag or commit hash.
			final String tag;
			final String scmTag = pom.getSCMTag();
			if (scmTag == null || scmTag.equals("HEAD")) {
				if (log.isDebug()) {
					log.debug(scmTag == null ? //
						"No SCM tag available; using commit hash." : //
						"Weird SCM tag '" + scmTag + "'; using commit hash.");
				}
				final Manifest m = Manifest.getManifest(c);
				tag = m == null ? null : m.getImplementationBuild();
				if (tag == null) log.debug("No commit hash found.");
			}
			else tag = scmTag;
			if (tag == null) {
				// No tag or commit hash could be extracted.
				return new URL(scmURL);
			}

			// Build a precise GitHub URL.
			final StringBuilder url = new StringBuilder();
			url.append(scmURL);
			if (!scmURL.endsWith("/")) url.append("/");
			url.append("blob/");
			url.append(tag);
			final String sourceDir = pom.cdata("//build/sourceDirectory");
			url.append(sourceDir == null ? "/src/main/java/" : sourceDir.replace("${project.basedir}", "") + "/");
			url.append(c.getName().replaceAll("\\.", "/"));
			url.append(".java");
			return new URL(url.toString());
		}
		catch (final MalformedURLException exc) {
			log.debug(exc);
			throw new SourceNotFoundException(c, exc);
		}
	}

	// -- Helper methods --

	private static String coord(final POM pom) {
		final String g = pom.getGroupId();
		final String a = pom.getArtifactId();
		final String v = pom.getVersion();
		return g + ":" + a + ":" + v;
	}

}
