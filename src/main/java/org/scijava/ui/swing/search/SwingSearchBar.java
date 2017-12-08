/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
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

package org.scijava.ui.swing.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.search.SearchOperation;
import org.scijava.search.SearchResult;
import org.scijava.search.SearchService;
import org.scijava.search.Searcher;
import org.scijava.thread.ThreadService;

/**
 * Swing-based search bar for an application window.
 *
 * @author Curtis Rueden
 */
public class SwingSearchBar extends JTextField {

	private static final String DEFAULT_MESSAGE = "Click here to search";
	private static final int MAX_RESULTS = 8;

	private static final Color SELECTED_COLOR = new Color(70, 152, 251);
	private static final Color HEADER_COLOR = new Color(128, 128, 128);
	private static final int ICON_SIZE = 16;
	private static final int PAD = 5;

	@Parameter
	private SearchService searchService;

	@Parameter
	private ThreadService threadService;

	private final Window parent;
	private JDialog dialog;
	private SwingSearchPanel searchPanel;

	public SwingSearchBar(final Context context, final Window parent) {
		super(DEFAULT_MESSAGE, 12);
		this.parent = parent;
		context.inject(this);

		addActionListener(e -> run());
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						if (searchPanel != null) searchPanel.up();
						e.consume();
						break;
					case KeyEvent.VK_DOWN:
						if (searchPanel != null) searchPanel.down();
						e.consume();
						break;
					case KeyEvent.VK_TAB:
						if (searchPanel != null) searchPanel.requestFocus();
						e.consume();
						break;
					case KeyEvent.VK_ESCAPE:
						reset();
						break;
				}
			}

		});
		getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(final DocumentEvent e) {
				search();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				search();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				search();
			}

		});
		addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				setText("");
			}

			@Override
			public void focusLost(final FocusEvent e) {
				if (getText().equals("")) reset();
			}

		});
	}

	/** Called externally to bring the search bar into focus. */
	public void activate() {
		requestFocus();
	}

	/** Called whenever the user types something. */
	private void search() {
		if (dialog == null) {
			if (getText().equals("") || getText().equals(DEFAULT_MESSAGE)) {
				// NB: Defer creating a new search dialog until something is typed.
				return;
			}

			dialog = new JDialog(parent, "Quick Search");
			searchPanel = new SwingSearchPanel(searchService.search(//
				(searcher, results) -> {
					threadService.queue(() -> {
						searchPanel.update(searcher, results);
					});
					// TODO Auto-generated method stub
				}));
			dialog.setContentPane(searchPanel);
			dialog.pack();

			// position below the parent window
			final int x = parent.getLocation().x;
			final int y = parent.getLocation().y + parent.getHeight() + 1;
			dialog.setLocation(x, y);
		}
		searchPanel.search(getText());
		if (!dialog.isVisible()) {
			dialog.setFocusableWindowState(false);
			dialog.setVisible(true);
			dialog.setFocusableWindowState(true);
		}
	}

	/** Called when the user hits ENTER. */
	private void run() {
		System.out.println("TODO: execute default search action");
		reset();
	}

	private void reset() {
		if (dialog != null) {
			searchPanel = null;
			dialog.dispose();
			dialog = null;
		}
		if (!getText().isEmpty()) setText("");
		else {
			// lose the focus!
			loseFocus();
//			Toolbar.getInstance().requestFocus();
		}
	}

	private void loseFocus() {
		// NB: Default action: do nothing.
	}

	// -- Helper classes --

	private class SwingSearchPanel extends JPanel {

private final SearchOperation operation;
		private final Map<Searcher, List<SearchResult>> allResults;
		private final JList<SearchResult> resultsList;

		public SwingSearchPanel(final SearchOperation operation) {
			this.operation = operation;
			allResults = new HashMap<>();
			resultsList = new JList<>();
			resultsList.setCellRenderer((list, value, index, isSelected,
				cellHasFocus) -> {
				if (isHeader(value)) {
					final JLabel header = new JLabel(value.name());
					header.setBorder(new EmptyBorder(PAD, PAD, 0, PAD));
					header.setBackground(HEADER_COLOR);
					header.setEnabled(false);
					return header;
				}
				final JPanel item = new JPanel();
				item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
				item.setBorder(new EmptyBorder(1, PAD, 0, PAD));
				item.add(icon(value.iconPath()));
				item.add(Box.createHorizontalStrut(3));
				item.add(new JLabel(value.name()));
				item.setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
				return item;
			});

			setPreferredSize(new Dimension(800, 300));

			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			resultsList.setBorder(new EmptyBorder(0, 0, PAD, 0));
			final JScrollPane resultsPane = new JScrollPane(resultsList);
			resultsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			final JPanel detailsPane = new JPanel();
			splitPane.setLeftComponent(resultsPane);
			splitPane.setRightComponent(detailsPane);

			setLayout(new BorderLayout());
			add(splitPane, BorderLayout.CENTER);
		}

		public void update(Searcher searcher, List<SearchResult> results) {
			allResults.put(searcher, //
				results.stream().limit(MAX_RESULTS).collect(Collectors.toList()));
			rebuild();
		}

		public void search(final String text) {
			operation.search(text);
		}

		public void up() {
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// Move upward in the list one element at a time, skipping headers.
			int index = resultsList.getSelectedIndex();
			do {
				index = (index + rowCount - 1) % rowCount;
			}
			while (result(index) instanceof SearchResultHeader);
			select(index);
		}

		public void down() {
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// Move downward in the list one element at a time, skipping headers.
			// TODO: skip disabled header rows.
			int index = resultsList.getSelectedIndex();
			do {
				index = (index + 1) % rowCount;
			}
			while (result(index) instanceof SearchResultHeader);
			select(index);
		}

		// -- Helper methods --

		private void rebuild() {
			final List<Searcher> searchers = new ArrayList<>(allResults.keySet());
			// TODO: pluginService.sort(searchers, Searcher.class)
			// Needs to cross-reference objects against PluginInfos of that type.

			System.out.println("--------------");
			DefaultListModel<SearchResult> listModel = new DefaultListModel<>();
			for (final Searcher searcher : searchers) {
				System.out.println("Processing searcher: " + searcher + " (" + searcher.title() + ")");
				// Add section header.
				listModel.addElement(new SearchResultHeader(searcher.title()));

				// Add results as entries.
				for (final SearchResult result : allResults.get(searcher)) {
					listModel.addElement(result);
				}
			}
			resultsList.setModel(listModel);
		}

		private Component icon(final String iconPath) {
			// TODO make icon() return URL, not String.
			if (iconPath == null || iconPath.isEmpty()) return emptyIcon();
//			final URL iconURL = getClass().getResource(iconPath);
			// TEMP FOR TESTING
			final URL iconURL = getClass().getResource("/icons/legacy.png");
			final ImageIcon icon = new ImageIcon(iconURL);
			if (icon.getIconWidth() != ICON_SIZE || //
				icon.getIconHeight() != ICON_SIZE)
			{
				return emptyIcon();
			}
			return new JLabel(icon);
		}

		private Component emptyIcon() {
			return Box.createRigidArea(new Dimension(ICON_SIZE, ICON_SIZE));
		}

		private boolean isHeader(final SearchResult value) {
			return value instanceof SearchResultHeader;
		}

		private SearchResult result(final int index) {
			return resultsList.getModel().getElementAt(index);
		}

		private void select(final int i) {
			resultsList.setSelectedIndex(i);
			resultsList.ensureIndexIsVisible(isFirstNonHeader(i) ? 0 : i);
		}

		private boolean isFirstNonHeader(final int index) {
			for (int i = index; i >= 0; i--) {
				if (!isHeader(result(i))) return false;
			}
			return true;
		}
	}

	/** A header dividing search result entries. */
	private class SearchResultHeader implements SearchResult {

		private final String title;

		public SearchResultHeader(final String title) {
			this.title = title;
		}

		@Override
		public String name() {
			return title;
		}

		@Override
		public String iconPath() {
			return null;
		}

		@Override
		public Map<String, String> properties() {
			return null;
		}
	}
}
