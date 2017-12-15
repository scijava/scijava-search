/*-
 * #%L
 * Search framework for SciJava applications.
 * %%
 * Copyright (C) 2017 SciJava developers.
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

package org.scijava.search.web;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.search.SearchResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A searcher for the <a href="http://biii.eu/search">Bio-Imaging Search
 * Engine</a>.
 *
 * @author Robert Haase (MPI-CBG)
 */
//@Plugin(type = Searcher.class, name = "BISE")
public class BISESearcher extends AbstractWebSearcher {
	
	@Parameter
	private LogService log;

	public BISESearcher() {
		super("BISE");
	}

	@Override
	public List<SearchResult> search(final String text, final boolean fuzzy) {
		try {
			// URL url = new URL("file:///c:/structure/temp/biii.eu_search2.html");
			final URL url = new URL("http://biii.eu/search?search_api_fulltext=" +
				URLEncoder.encode(text) + "&source=imagej");

			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final Document doc = db.parse(url.openStream());

			parse(doc.getDocumentElement());
			saveLastItem();
		}
		catch (final IOException e) {
			log.debug(e);
		}
		catch (final ParserConfigurationException e) {
			log.debug(e);
		}
		catch (final SAXException e) {
			log.debug(e);
		}
		return getSearchResults();
	}

	String currentHeading;
	String currentLink;

	private void parseHeading(final Node node) {

		if (node.getTextContent() != null && node.getTextContent().trim()
			.length() > 0)
		{
			currentHeading = node.getTextContent();
		}
		if (node.getAttributes() != null) {
			final Node href = node.getAttributes().getNamedItem("href");
			if (href != null) {
				currentLink = "http://biii.eu" + href.getNodeValue();
			}
		}

		final NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node childNode = nodeList.item(i);

			parseHeading(childNode);
		}
	}

	String currentContent;

	private void parseContent(final Node node) {
		if (node.getTextContent() != null) {
			currentContent = node.getTextContent();
		}

		final NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node childNode = nodeList.item(i);

			parse(childNode);
		}
	}

	private void saveLastItem() {
		if (currentHeading != null && currentHeading.length() > 0) {

			addResult(currentHeading, "", currentLink, currentContent);

		}
		currentHeading = "";
		currentLink = "";
		currentContent = "";
	}

	private void parse(final Node node) {
		if (node.getNodeName().equals("div")) {
			final Node item = node.getAttributes() == null ? null : node
				.getAttributes().getNamedItem("class");
			if (item != null && item.getNodeValue().equals(
				"views-field views-field-title"))
			{

				if (currentHeading != null) {
					saveLastItem();
				}
				parseHeading(node);

				return;
			}
			if (item != null && item.getNodeValue().equals(
				"views-field views-field-search-api-excerpt"))
			{
				parseContent(node);
				return;
			}
		}

		final NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			final Node childNode = nodeList.item(i);

			parse(childNode);
		}

	}

}
