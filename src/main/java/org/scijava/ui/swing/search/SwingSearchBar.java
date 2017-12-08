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

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
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
						break;
					case KeyEvent.VK_DOWN:
						if (searchPanel != null) searchPanel.down();
						break;
					case KeyEvent.VK_TAB:
						if (searchPanel != null) searchPanel.requestFocus();
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
						searchPanel.updateResults(searcher, results);
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
		private final JList<JPanel> resultsList;

		public SwingSearchPanel(final SearchOperation operation) {
			this.operation = operation;
			allResults = new HashMap<>();
			resultsList = new JList<>();
			resultsList.setCellRenderer((list, value, index, isSelected,
				cellHasFocus) -> {
				JPanel renderer = value;
				renderer.setBackground(isSelected ? Color.blue : list.getBackground());
				return renderer;
			});

			setPreferredSize(new Dimension(800, 300));

			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			final JScrollPane resultsPane = new JScrollPane(resultsList);
			final JPanel detailsPane = new JPanel();
			splitPane.setLeftComponent(resultsPane);
			splitPane.setRightComponent(detailsPane);

			setLayout(new BorderLayout());
			add(splitPane, BorderLayout.CENTER);
		}

		private void updateResults(Searcher searcher, List<SearchResult> results) {
			allResults.put(searcher, results);
			rebuild();
		}

		private void rebuild() {
			final List<Searcher> searchers = new ArrayList<>(allResults.keySet());
			// TODO: pluginService.sort(searchers, Searcher.class)
			// Needs to cross-reference objects against PluginInfos of that type.

			DefaultListModel<JPanel> listModel = new DefaultListModel<>();
			for (final Searcher searcher : searchers) {
				// Add section header.
				final JPanel header = new JPanel();
				header.setBackground(Color.cyan.brighter().brighter());
				header.add(new JLabel(searcher.title()));
				header.setEnabled(false);
				listModel.addElement(header);

				// Add results as entries.
				for (final SearchResult result : allResults.get(searcher)) {
					final JPanel item = new JPanel();
					item.setLayout(new BorderLayout());
					item.add(icon(result.iconPath()));
					item.add(new JLabel(result.name()));
					listModel.addElement(item);
				}
			}
			resultsList.setModel(listModel);
		}

		private JLabel icon(String iconPath) {
			// TODO iconPath at exactly 16x16 or something like that
			// TODO make icon() return URL, not String.
			final URL iconURL = getClass().getResource("/icons/legacy.png");
			final ImageIcon icon = new ImageIcon(iconURL);
			return new JLabel(icon);
		}

		// -- Helper methods --

		private void search(final String text) {
			operation.search(text);
		}

		private void up() {
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// TODO: skip disabled header rows.
			select((resultsList.getSelectedIndex() + rowCount - 1) % rowCount);
		}

		private void down() {
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// TODO: skip disabled header rows.
			select((resultsList.getSelectedIndex() + 1) % rowCount);
		}

		private void select(final int i) {
			resultsList.setSelectedIndex(i);
			// TODO: scrollRectToVisible yuck
		}
	}
}
