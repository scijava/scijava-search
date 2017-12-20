/*
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

package org.scijava.ui.swing.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;

import net.miginfocom.swing.MigLayout;

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
	
	private static final Color SEARCHBAR_FONT_COLOR = new Color(0, 0, 0);
	private static final Color SEARCHBAR_FONT_DEFAULT_COLOR = new Color(150, 150, 150);

	private static final Color SELECTED_COLOR = new Color(70, 152, 251);
	private static final int ICON_SIZE = 16;
	private static final int PAD = 5;

	@Parameter
	private SearchService searchService;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private PluginService pluginService;

	private SwingSearchPanel searchPanel;
	
	private DocumentListener documentListener;
	
	private final JButton exitButton;
	
	private String searchTerm;

	public SwingSearchBar(final Context context) {
		super(DEFAULT_MESSAGE, 12);
		context.inject(this);
		setText(DEFAULT_MESSAGE);
		setForeground(SEARCHBAR_FONT_DEFAULT_COLOR);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(), 
				BorderFactory.createEmptyBorder(5,5,5,5)));

		addActionListener(e -> run());
		addKeyListener(new SearchBarKeyAdapter());
		documentListener = new DocumentListener() {

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

		};
		getDocument().addDocumentListener(documentListener);
		addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				if (DEFAULT_MESSAGE.equals(getText())) setText("");
				setForeground(SEARCHBAR_FONT_COLOR);
			}

			@Override
			public void focusLost(final FocusEvent e) {
				if (getText().equals("")) {
					reset();
				}
			}

		});
		
		exitButton = new JButton("🞩");
		exitButton.setBackground(new Color(255,255,255));
		exitButton.setBorder(BorderFactory.createLineBorder(new Color(255,255,255),5));
		exitButton.addActionListener(ae -> {
			reset();
			loseFocus();
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

	private Window window() {
		return SwingUtilities.getWindowAncestor(this);
	}

	/** Called whenever the user types something. */
	private void search() {
		assertDispatchThread();
		if (searchPanel == null) {
			if (getText().equals("") || getText().equals(DEFAULT_MESSAGE)) {
				// NB: Defer creating a new search dialog until something is typed.
				return;
			}

			searchPanel = new SwingSearchPanel(); // Spawns the SearchOperation!
			getParent().add(searchPanel, "south,height 300!", getParent().getComponentCount()-1);
			getParent().doLayout();
			getParent().revalidate();
			window().pack();
			getParent().repaint();
			threadService.queue(() -> {
				searchPanel.setVisible(true);
				try { Thread.sleep(100); }
				catch (InterruptedException exc) {}
				grabFocus();
				requestFocus();
			});
		}
		searchPanel.search(getText());
	}

	/** Called when the user hits ENTER. */
	private void run() {
		assertDispatchThread();
		searchPanel.execute();
		reset();
	}

	private void reset() {
		assertDispatchThread();
		if (searchPanel == null){
			loseFocus();
			getDocument().removeDocumentListener(documentListener);
			setText(DEFAULT_MESSAGE);
			setForeground(SEARCHBAR_FONT_DEFAULT_COLOR);
			getDocument().addDocumentListener(documentListener);
		}
		else {
			threadService.queue(() -> {
				getParent().remove(searchPanel);
				getParent().revalidate();
				getParent().repaint();
				final Window w = window();
				w.revalidate();
				w.pack();
				w.repaint();
				searchPanel = null;
			});
			setText("");
			requestFocusInWindow();
		}
	}

	private void loseFocus() {
		SwingUtilities.getWindowAncestor(this).requestFocusInWindow();
	}

	// -- Helper classes --

	private class SwingSearchPanel extends JPanel {

		private final SearchOperation operation;
		private final Map<Class<?>, SearchEvent> allResults;
		private final Map<Class<?>, JCheckBox> headerCheckboxes;
		private final JList<SearchResult> resultsList;
		
		private SearchResult selected = null;

		public SwingSearchPanel() {
			
			setLayout(new BorderLayout());
			setPreferredSize(new Dimension(800, 300));
			setBorder(BorderFactory.createEmptyBorder());
			
			operation = searchService.search(//
				event -> threadService.queue(() -> update(event)));

			allResults = new HashMap<>();
			headerCheckboxes = new HashMap<>();

			resultsList = new JList<>();
			resultsList.setCellRenderer((list, value, index, isSelected,
				cellHasFocus) -> {
				if (isHeader(value)) {
					final Searcher searcher = ((SearchResultHeader) value).searcher();

					final Container parent = getParent();

					final JCheckBox headerBox = //
						new JCheckBox(searcher.title(), searchService.enabled(searcher));
					headerBox.setFont(smaller(headerBox.getFont(), 2));
					if (parent != null) headerBox.setBackground(parent.getBackground());
					headerCheckboxes.put(searcher.getClass(), headerBox);

					final JPanel headerInnerPane = new JPanel();
					headerInnerPane.setLayout(new GridLayout(1, 1));
					headerInnerPane.add(headerBox);
					if (parent != null) headerInnerPane.setBackground(parent.getBackground());

					final JPanel headerOuterPane = new JPanel();
					headerOuterPane.setLayout(new GridLayout(1, 1));
					headerOuterPane.add(headerInnerPane);
					headerOuterPane.setBackground(list.getBackground());
					headerOuterPane.setBorder(new EmptyBorder(index == 0? 0 : PAD, 0, 0, 0));
					return headerOuterPane;
				}
				final JPanel item = new JPanel();
				item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
				item.setBorder(new EmptyBorder(1, PAD, 0, PAD));
				item.add(icon(value.iconPath()));
				item.add(Box.createHorizontalStrut(3));
				final JTextArea name = new JTextArea();
				name.setText(value.identifier());
				name.setEditable(false);
				name.setBackground(null);
				item.add(name);
				item.setBackground(isSelected ? SELECTED_COLOR : list.getBackground());
				return item;
			});
			resultsList.setBorder(new EmptyBorder(0, 0, 0, 0));
			final JScrollPane resultsPane = new JScrollPane(resultsList);
			resultsPane.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			// uncomment to move scrollbar to the left
//			resultsPane.setVerticalScrollBarPolicy(
//					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
//			resultsPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			resultsPane.setBorder(null);

			final JPanel detailsPane = new JPanel();
			final JLabel detailsTitle = new JLabel();
			final JPanel detailsProps = new JPanel();
			final JScrollPane detailsScrollPane = new JScrollPane(detailsProps);
			final JPanel detailsButtons = new JPanel();

			detailsScrollPane.setHorizontalScrollBarPolicy(
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			detailsScrollPane.setBorder(null);

			detailsProps.setLayout(new MigLayout("wrap 1, ins 0, wmin 0, hmin 0", "[grow]", ""));
			detailsButtons.setLayout(new MigLayout("fill, ins " + PAD + " 0 0 0"));
			detailsPane.setLayout(new MigLayout("wrap, ins 0 " + PAD + " " + PAD + " " + PAD + ", fill, wmin 0, hmin 0, hmax 100%, wmax 100%",
					"[grow]", "[fill][fill,grow][fill]"));

			resultsList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked( MouseEvent e ) {
					final SearchResult result = resultsList.getSelectedValue();
					if (result == null || isHeader(result)) {
						if (result != null) {
							final Searcher searcher = ((SearchResultHeader) result).searcher();
							searchService.setEnabled(searcher, !searchService.enabled(searcher));
							SwingSearchBar.this.search();
						}
					}
				}
			});

			resultsList.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved( MouseEvent e ) {
					int index = resultsList.locationToIndex(e.getPoint());
					SearchResult _selected = resultsList.getModel().getElementAt(index);
					if(selected != _selected) {
						selected = _selected;
						if(selected != null && !isHeader(selected)) {
							resultsList.setSelectedValue(selected, false);
						}
					}
				}
			});

			resultsList.addListSelectionListener(lse -> {
				if (lse.getValueIsAdjusting()) return;
				final SearchResult result = resultsList.getSelectedValue();
				if (result == null || isHeader(result)) {
					if(result != null){
						threadService.queue(() -> {
							down();
						});
						return;
					}
					// clear details pane
					detailsTitle.setText("");
					detailsProps.removeAll();
					detailsButtons.removeAll();
				}
				else {
					// populate details pane
					detailsTitle.setText("<html><h2>" + highlightSearchUnderline(escapeHtml(result.name()), searchTerm) + "</h2>");
					detailsProps.removeAll();
					result.properties().forEach((k, v) -> {
						if(v != "") {
							if (k == null) {
								final JTextPane textPane = new JTextPane();
								textPane.setContentType("text/html");
								textPane.setText(highlightSearchBold(v, searchTerm));
								Font font = UIManager.getFont("Label.font");
								String bodyRule = "body { font-family: " + font.getFamily() + "; " + 
										"font-size: " + font.getSize() + "pt; }";
								((HTMLDocument)textPane.getDocument()).getStyleSheet().addRule(bodyRule);
								textPane.setBorder(BorderFactory.createCompoundBorder(
										BorderFactory.createMatteBorder(1, 0, 1, 0, Color.DARK_GRAY),
										BorderFactory.createEmptyBorder(PAD, 0, PAD, 0)));
								textPane.setEditable(false);
								textPane.setOpaque(false);
								detailsProps.add(textPane, "growx, wmax 100%");
							}
							else {
								final JLabel keyLabel = new JLabel("<html>" +
									"<strong style=\"color: gray;\">" + k +
									"&nbsp;&nbsp;</strong>");
								keyLabel.setFont(smaller(keyLabel.getFont(), 1));
								detailsProps.add(keyLabel, "growx, pad 0 0 10 0");
								final JTextArea valueField = new JTextArea();
								valueField.setText(v);
								valueField.setLineWrap(true);
								valueField.setWrapStyleWord(true);
								valueField.setEditable(false);
								valueField.setBackground(null);
								valueField.setBorder(null);
								detailsProps.add(valueField, "growx, wmax 100%");
							}
						}
					});
					detailsButtons.removeAll();
					final List<SearchAction> actions = searchService.actions(result);
					boolean first = true;
					for(SearchAction action : actions){
						final JButton button = new JButton(action.toString());
						button.addActionListener(ae -> {
							action.run();
							if(action.willCloseSearch()){
								reset();
							}
						});
						button.addKeyListener(new SearchBarKeyAdapter());
						if(first){
							detailsButtons.add(button, "grow, spanx");
							JRootPane rootPane = this.getRootPane();
							if(rootPane != null){
								rootPane.setDefaultButton(button);
							}
							first = false;
						}else{
							detailsButtons.add(button, "growx");
						}
					}
				}
			});
			
			detailsPane.add(exitButton, "pos n 0 100% n");
			detailsPane.add(detailsTitle,"growx, pad 0 0 0 -20");
			detailsPane.add(detailsScrollPane, "growx, hmin 0, wmin 0");
			detailsPane.add(detailsButtons, "growx");
			
			resultsList.addKeyListener(new SearchBarKeyAdapter());

			// uncomment the following lines to hide the JSplitPane divider borders
//			Border border = new CompoundBorder(new LineBorder(getParent().getBackground(), 1), new LineBorder(getParent().getBackground(), 5));
//			UIManager.put("SplitPaneDivider.border", border);
			final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setLeftComponent(resultsPane);
			splitPane.setRightComponent(detailsPane); 
			detailsPane.setMinimumSize(new Dimension(0, 0));
			resultsPane.setMinimumSize(new Dimension(0, 0));
			splitPane.setBorder(null);
			add(splitPane, BorderLayout.CENTER);
		}

		public void search(final String text) {
			assertDispatchThread();
			searchTerm = text;
			operation.search(text.toLowerCase());
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
			select(index -> (index + rowCount() - 1) % rowCount());
		}

		private void down() {
			select(index -> (index + 1) % rowCount());
		}

		private void select(final Function<Integer, Integer> stepper) {
			assertDispatchThread();
			if (resultCount() == 0) return;
			// Step through the list, skipping headers.
			int index = resultsList.getSelectedIndex();
			do {
				index = stepper.apply(index);
			}
			while (isHeader(result(index)));
			select(index);
		}

		private int rowCount() {
			return resultsList.getModel().getSize();
		}
		
		private int resultCount() {
			int count = 0;
			for(int i = 0; i < resultsList.getModel().getSize(); i++){
				SearchResult result = resultsList.getModel().getElementAt(i);
				if(!isHeader(result)) count++;
			}
			return count;
		}

		private Font smaller(Font font, int decrement) {
			return new Font(font.getFontName(), font.getStyle(), font.getSize() - decrement);
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

				if (completeResults == null) continue;

				// Add section header.
				listModel.addElement(new SearchResultHeader(searcher));

				if (completeResults.isEmpty()) continue;

				// Limit to the top MAX_RESULTS matches only.
				final List<SearchResult> results = completeResults.stream() //
					.limit(MAX_RESULTS).collect(Collectors.toList());

				// Add results as entries.
				for (final SearchResult result : results) {
					listModel.addElement(result);
				}
			}

			resultsList.setModel(listModel);

			// TODO: Improve retainment of previous selection.
			if(!searchTerm.isEmpty()) {
				if (previous == null) {
					if(listModel.getSize() > 0){
						resultsList.setSelectedIndex(firstResultIndex());
					}
				}
				else {
					if(listModel.contains(previous)){
						resultsList.setSelectedValue(previous, true);
					}
				}
			}
		}

		private Component icon(final String iconPath) {
			// TODO make icon() return URL, not String.
			if (iconPath == null || iconPath.isEmpty()) return emptyIcon();
			final URL iconURL = getClass().getResource(iconPath);
			if(iconURL == null) return emptyIcon();
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
			for (int i = 0; i < rowCount(); i++) {
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
			for (int i = index-1; i >= 0; i--) {
				if (!isHeader(result(i))) return false;
			}
			return true;
		}

		private String highlightSearchUnderline(String text, String search) {
			return highlightSearch(text, search, "<u>", "</u>");
		}

		private String highlightSearchBold(String text, String search) {
			return highlightSearch(text, search, "<b>", "</b>");
		}

		private String highlightSearch(String text, String search, String before, String after) {
			String[] terms = search.split(" ");
			String output = new String(text);
			for(String term : terms){
				List<Integer> res = new ArrayList<>();
				for (int index = output.toLowerCase().indexOf(term); index >= 0; 
						index = output.toLowerCase().indexOf(term, index + 1)) {
					res.add(index);
				}
				for(int i = res.size()-1; i >= 0; i--) {
					int index = res.get(i);
					output = output.substring(0, index) + before
							+ output.substring(index, index + term.length())
							+ after
							+ output.substring(index + term.length(), output.length());
				}
			}
			return output;
		}

		public String escapeHtml(String s) {
			StringBuilder out = new StringBuilder(Math.max(16, s.length()));
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
					out.append("&#");
					out.append((int) c);
					out.append(';');
				} else {
					out.append(c);
				}
			}
			return out.toString();
		}

	}

	private class SearchBarKeyAdapter extends KeyAdapter {
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
				case KeyEvent.VK_ENTER:
					if (searchPanel != null) run();
					e.consume();
					break;
				case KeyEvent.VK_ESCAPE:
					reset();
					break;
				case KeyEvent.VK_L:
					if(!hasFocus()){
						requestFocusInWindow();
						selectAll();
					}
					break;
			}
		}
	}

	/** A header dividing search result entries. */
	private class SearchResultHeader implements SearchResult {

		private final Searcher searcher;

		public SearchResultHeader(final Searcher searcher) {
			this.searcher = searcher;
		}

		public Searcher searcher() {
			return searcher;
		}

		@Override
		public String name() {
			return searcher.title();
		}

		@Override
		public String identifier() {
			return name();
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
