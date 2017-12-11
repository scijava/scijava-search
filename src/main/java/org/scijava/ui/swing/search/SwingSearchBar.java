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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.search.SearchAction;
import org.scijava.search.SearchEvent;
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

	@Parameter
	private PluginService pluginService;

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
				if (DEFAULT_MESSAGE.equals(getText())) setText("");
			}

			@Override
			public void focusLost(final FocusEvent e) {
				if (getText().equals("")) reset();
			}

		});
	}

	/** Called externally to bring the search bar into focus. */
	public void activate() {
		threadService.queue(() -> {
			setText("");
			requestFocus();
		});
	}

	// -- Utility methods --

	// TODO: Move this method to PluginService.
	public <PT extends SciJavaPlugin> void sort(final List<PT> instances,
		final Class<PT> type)
	{
		// Create a mapping from plugin classes to priorities.
		final List<PluginInfo<PT>> plugins = pluginService.getPluginsOfType(type);
		final Map<Class<?>, PluginInfo<PT>> infos = plugins.stream().collect(//
			Collectors.toMap(PluginInfo::getPluginClass, Function.identity()));

		// Compare plugin instances by priority via the mapping.
		final Comparator<PT> comparator = (o1, o2) -> Priority.compare(//
			infos.get(o1.getClass()), infos.get(o2.getClass()));
		Collections.sort(instances, comparator);
	}

	// -- Helper methods --

	/** Defensive programming check to avoid bugs. */
	private void assertDispatchThread() {
		if (!threadService.isDispatchThread()) {
			throw new IllegalStateException("Current thread is not EDT");
		}
	}

	/** Called whenever the user types something. */
	private void search() {
		assertDispatchThread();
		if (dialog == null) {
			if (getText().equals("") || getText().equals(DEFAULT_MESSAGE)) {
				// NB: Defer creating a new search dialog until something is typed.
				return;
			}

			dialog = new JDialog(parent, "Quick Search");
			searchPanel = new SwingSearchPanel(); // Spawns the SearchOperation!
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
		assertDispatchThread();
		searchPanel.execute();
		reset();
	}

	private void reset() {
		assertDispatchThread();
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
		assertDispatchThread();
		// NB: Default action: do nothing.
	}

	// -- Helper classes --

	private class SwingSearchPanel extends JPanel {

		private final SearchOperation operation;
		private final Map<Class<?>, SearchEvent> allResults;
		private final JList<SearchResult> resultsList;

		public SwingSearchPanel() {
			operation = searchService.search(//
				event -> threadService.queue(() -> update(event)));

			allResults = new HashMap<>();

			resultsList = new JList<>();
			resultsList.setCellRenderer((list, value, index, isSelected,
				cellHasFocus) -> {
				if (isHeader(value)) {
					final JLabel header = new JLabel(value.name());
					header.setBorder(new CompoundBorder(//
						new EmptyBorder(PAD, PAD, 0, PAD),
						new MatteBorder(0, 0, 1, 0, Color.gray)));
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
			resultsList.setBorder(new EmptyBorder(0, 0, PAD, 0));
			final JScrollPane resultsPane = new JScrollPane(resultsList);
			resultsPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

			final JPanel detailsPane = new JPanel();
			final JLabel detailsTitle = new JLabel();
			detailsTitle.setHorizontalAlignment(SwingConstants.CENTER);
			final JPanel detailsProps = new JPanel();
			final JPanel detailsButtons = new JPanel();
			detailsButtons.setLayout(new BoxLayout(detailsButtons, BoxLayout.X_AXIS));
			detailsTitle.setAlignmentX(0.5f);
			detailsProps.setAlignmentX(0.5f);
			detailsButtons.setAlignmentX(0.5f);

			detailsPane.setLayout(new BoxLayout(detailsPane, BoxLayout.Y_AXIS));
			detailsPane.add(detailsTitle);
			detailsPane.add(detailsProps);
			detailsPane.add(detailsButtons);
			detailsPane.add(Box.createVerticalGlue());

			resultsList.addListSelectionListener(lse -> {
				if (lse.getValueIsAdjusting()) return;
				final SearchResult result = resultsList.getSelectedValue();
				if (result == null || isHeader(result)) {
					// clear details pane
					detailsTitle.setText("");
					detailsProps.removeAll();
					detailsButtons.removeAll();
				}
				else {
					// populate details pane
					detailsTitle.setText("<html><h2>" + result.name() + "</h2>");
					detailsProps.removeAll();
					detailsProps.setLayout(new GridLayout(result.properties().size(), 2));
					result.properties().forEach((k, v) -> {
						final JLabel keyLabel = new JLabel("<html>" +
							"<strong style=\"color: gray; padding-right: 5px\">" + k +
							"&nbsp;&nbsp;</strong>", SwingConstants.RIGHT);
						detailsProps.add(keyLabel);
						final JTextField valueField = new JTextField();
						valueField.setText(v);
						valueField.setEditable(false);
						valueField.setBackground(null);
						valueField.setBorder(null);
						detailsProps.add(valueField);
					});
					detailsButtons.removeAll();
					final List<SearchAction> actions = searchService.actions(result);
					actions.forEach(action -> {
						final JButton button = new JButton(action.toString());
						button.addActionListener(ae -> {
							action.run();
							reset();
						});
						detailsButtons.add(button);
					});
				}
			});
			
			resultsList.addKeyListener(new SearchPanelKeyAdapter());

			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(800, 300));

			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(resultsPane);
			splitPane.setRightComponent(detailsPane);
			add(splitPane, BorderLayout.CENTER);
			
		}

		public void search(final String text) {
			assertDispatchThread();
			operation.search(text);
		}

		// -- Helper methods --

		/** Called whenever a new batch of search results comes in. */
		private void update(final SearchEvent event) {
			assertDispatchThread();
			if (event.exclusive()) allResults.clear();
			allResults.put(event.searcher().getClass(), event);
			rebuild();
		}

		private void up() {
			assertDispatchThread();
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// Move upward in the list one element at a time, skipping headers.
			int index = resultsList.getSelectedIndex();
			do {
				index = (index + rowCount - 1) % rowCount;
			}
			while (isHeader(result(index)));
			select(index);
		}

		private void down() {
			assertDispatchThread();
			final int rowCount = resultsList.getModel().getSize();
			if (rowCount == 0) return;
			// Move downward in the list one element at a time, skipping headers.
			int index = resultsList.getSelectedIndex();
			do {
				index = (index + 1) % rowCount;
			}
			while (isHeader(result(index)));
			select(index);
		}

		/** Executes the default search action. */
		private void execute() {
			assertDispatchThread();
			final SearchResult result = resultsList.getSelectedValue();
			if (result == null) return;
			final List<SearchAction> actions = searchService.actions(result);
			if (actions.isEmpty()) return;
			threadService.run(() -> actions.get(0).run());
		}

		private void rebuild() {
			assertDispatchThread();

			final SearchResult previous = resultsList.getSelectedValue();

			// Gets list of Searchers, sorted by priority.
			final List<Searcher> searchers = allResults.values().stream().map(
				event -> event.searcher()).collect(Collectors.toList());
			sort(searchers, Searcher.class);

			// Build the new list model.
			DefaultListModel<SearchResult> listModel = new DefaultListModel<>();
			for (final Searcher searcher : searchers) {
				// Look up the results list.
				final List<SearchResult> completeResults = //
					allResults.get(searcher.getClass()).results();
				if (completeResults.isEmpty()) continue;

				// Limit to the top MAX_RESULTS matches only.
				final List<SearchResult> results = completeResults.stream() //
					.limit(MAX_RESULTS).collect(Collectors.toList());

				// Add section header.
				listModel.addElement(new SearchResultHeader(searcher.title()));

				// Add results as entries.
				for (final SearchResult result : results) {
					listModel.addElement(result);
				}
			}
			resultsList.setModel(listModel);

			// TODO: Improve retainment of previous selection.
			if (previous == null) resultsList.setSelectedIndex(firstResultIndex());
			else resultsList.setSelectedValue(previous, true);
		}

		private Component icon(final String iconPath) {
			// TODO make icon() return URL, not String.
			if (iconPath == null || iconPath.isEmpty()) return emptyIcon();
			final URL iconURL = getClass().getResource(iconPath);
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
			assertDispatchThread();
			return resultsList.getModel().getElementAt(index);
		}

		private int firstResultIndex() {
			assertDispatchThread();
			for (int i = 0; i < resultsList.getModel().getSize(); i++) {
				if (!isHeader(result(i))) return i;
			}
			return -1;
		}

		private void select(final int i) {
			assertDispatchThread();
			resultsList.setSelectedIndex(i);
			resultsList.ensureIndexIsVisible(isFirstNonHeader(i) ? 0 : i);
		}

		private boolean isFirstNonHeader(final int index) {
			assertDispatchThread();
			for (int i = index; i >= 0; i--) {
				if (!isHeader(result(i))) return false;
			}
			return true;
		}
	}
	
	private class SearchPanelKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(final KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_ESCAPE:
					reset();
					break;
			}
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
