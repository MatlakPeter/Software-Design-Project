package ui;

import core.Crawler;
import core.Indexer;
import core.QueryProcessor;
import model.FileData;
import observer.FilePopularityScorer;
import observer.SearchHistoryManager;
import repository.FileRepository;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class SearchEngineUI extends JFrame {

    // ── Core engine ──────────────────────────────────────────────────────────
    private final FileRepository     repository;
    private final QueryProcessor     queryProcessor;
    private final SearchHistoryManager historyManager;

    // ── Top bar ───────────────────────────────────────────────────────────────
    private JTextField  searchField;
    private JButton     searchButton;
    private JButton     indexButton;

    // ── Suggestion chips ──────────────────────────────────────────────────────
    private JPanel      suggestionsPanel;

    // ── Sort controls ─────────────────────────────────────────────────────────
    private JComboBox<String> sortCombo;

    // ── Results ───────────────────────────────────────────────────────────────
    private JPanel      resultsPanel;
    private JLabel      statusLabel;
    private JScrollPane resultsScroll;

    // ── Detail pane ───────────────────────────────────────────────────────────
    private JTextArea   detailArea;
    private JLabel      detailTitle;
    private JLabel      detailPath;
    private JLabel      detailMeta;

    // ── Colours & fonts ───────────────────────────────────────────────────────
    private static final Color BG          = new Color(18,  18,  27);
    private static final Color SURFACE      = new Color(28,  28,  42);
    private static final Color CARD         = new Color(36,  36,  54);
    private static final Color CARD_HOVER   = new Color(46,  46,  68);
    private static final Color ACCENT       = new Color(124, 93, 250);
    private static final Color ACCENT_DIM   = new Color(86,  64, 180);
    private static final Color TEXT_PRI     = new Color(230, 230, 245);
    private static final Color TEXT_SEC     = new Color(140, 140, 165);
    private static final Color TEXT_MUT     = new Color(90,  90, 120);
    private static final Color DIVIDER      = new Color(50,  50,  72);
    private static final Color TAG_BG       = new Color(50,  40, 100);
    private static final Color TAG_TEXT     = new Color(180, 160, 255);
    private static final Color SUCCESS      = new Color(80, 200, 120);
    private static final Color WARN         = new Color(255, 190, 80);

    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font  FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font  FONT_MONO    = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Font  FONT_SEARCH  = new Font("Segoe UI", Font.PLAIN, 15);

    // ─────────────────────────────────────────────────────────────────────────

    public SearchEngineUI() {
        repository    = new FileRepository();
        queryProcessor = new QueryProcessor(repository);
        historyManager = new SearchHistoryManager(repository);
        queryProcessor.addObserver(historyManager);
        queryProcessor.addObserver(new FilePopularityScorer(repository));

        setTitle("Local Search Engine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1200, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        buildUI();
        setVisible(true);
        refreshSuggestions();
    }

    // ─────────────────────────────────── UI assembly ──────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterSplit(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);
    }

    // ── Top panel: title + search bar + index button ──────────────────────────

    private JPanel buildTopPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SURFACE);
        outer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVIDER));

        // ── title row ──
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        titleRow.setBackground(SURFACE);
        JLabel logo = new JLabel("⚡ Local Search Engine");
        logo.setFont(FONT_TITLE);
        logo.setForeground(ACCENT);
        titleRow.add(logo);

        // ── search row ──
        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setBackground(SURFACE);
        searchRow.setBorder(new EmptyBorder(0, 20, 14, 20));

        searchField = new JTextField();
        searchField.setFont(FONT_SEARCH);
        searchField.setBackground(CARD);
        searchField.setForeground(TEXT_PRI);
        searchField.setCaretColor(ACCENT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        searchField.setToolTipText("Use content:term  path:term  sort:score|name|date  or free text");

        searchButton = accentButton("Search", ACCENT);
        indexButton  = accentButton("⚙ Index…", ACCENT_DIM);

        // Sort combo
        sortCombo = new JComboBox<>(new String[]{"Default", "Score", "Name", "Date Modified"});
        sortCombo.setFont(FONT_SMALL);
        sortCombo.setBackground(CARD);
        sortCombo.setForeground(TEXT_PRI);
        sortCombo.setPreferredSize(new Dimension(130, 36));

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setBackground(SURFACE);
        rightControls.add(new JLabel("Sort:") {{ setForeground(TEXT_SEC); setFont(FONT_SMALL); }});
        rightControls.add(sortCombo);
        rightControls.add(searchButton);
        rightControls.add(indexButton);

        searchRow.add(searchField,   BorderLayout.CENTER);
        searchRow.add(rightControls, BorderLayout.EAST);

        // ── suggestion chips ──
        suggestionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        suggestionsPanel.setBackground(SURFACE);
        suggestionsPanel.setBorder(new EmptyBorder(0, 18, 10, 18));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(SURFACE);
        wrap.add(titleRow,        BorderLayout.NORTH);
        wrap.add(searchRow,       BorderLayout.CENTER);
        wrap.add(suggestionsPanel, BorderLayout.SOUTH);

        outer.add(wrap, BorderLayout.CENTER);

        // ── wire events ──
        searchButton.addActionListener(e -> doSearch());
        searchField.addActionListener(e -> doSearch());
        indexButton.addActionListener(e -> showIndexDialog());

        // live-filter: update suggestions as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshSuggestions(); }
            public void removeUpdate(DocumentEvent e)  { refreshSuggestions(); }
            public void changedUpdate(DocumentEvent e) { refreshSuggestions(); }
        });

        return outer;
    }

    // ── Center: results list + detail pane ────────────────────────────────────

    private JSplitPane buildCenterSplit() {
        // ── left: results ──
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(BG);
        resultsPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(null);
        resultsScroll.getViewport().setBackground(BG);
        resultsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        styleScrollBar(resultsScroll);

        // ── right: detail ──
        JPanel detailPanel = buildDetailPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, resultsScroll, detailPanel);
        split.setDividerLocation(520);
        split.setDividerSize(4);
        split.setBorder(null);
        split.setBackground(BG);
        split.setContinuousLayout(true);
        return split;
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 1, 0, 0, DIVIDER),
                new EmptyBorder(16, 18, 16, 18)));

        detailTitle = new JLabel("Select a result");
        detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        detailTitle.setForeground(TEXT_PRI);

        detailPath = new JLabel(" ");
        detailPath.setFont(FONT_SMALL);
        detailPath.setForeground(TEXT_SEC);

        detailMeta = new JLabel(" ");
        detailMeta.setFont(FONT_SMALL);
        detailMeta.setForeground(TEXT_MUT);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(SURFACE);
        header.add(detailTitle);
        header.add(Box.createVerticalStrut(4));
        header.add(detailPath);
        header.add(Box.createVerticalStrut(2));
        header.add(detailMeta);

        detailArea = new JTextArea();
        detailArea.setFont(FONT_MONO);
        detailArea.setBackground(CARD);
        detailArea.setForeground(TEXT_PRI);
        detailArea.setCaretColor(ACCENT);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setEditable(false);
        detailArea.setBorder(new EmptyBorder(10, 12, 10, 12));
        detailArea.setText("Click a search result to preview its content here.");

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(new LineBorder(DIVIDER, 1, true));
        detailScroll.getViewport().setBackground(CARD);
        styleScrollBar(detailScroll);

        panel.add(header,       BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        bar.setBackground(SURFACE);
        bar.setBorder(new MatteBorder(1, 0, 0, 0, DIVIDER));

        statusLabel = new JLabel("Ready – enter a query to search the index.");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_MUT);
        bar.add(statusLabel);

        JLabel hint = new JLabel("Tip: use  content:word  path:dir  sort:score|name|date");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_MUT);
        bar.add(hint);

        return bar;
    }

    // ─────────────────────────────────── logic ────────────────────────────────

    private void doSearch() {
        String raw = searchField.getText().trim();
        if (raw.isEmpty()) return;

        // Inject sort prefix if the combo differs from Default
        String sortPrefix = switch (sortCombo.getSelectedIndex()) {
            case 1 -> " sort:score";
            case 2 -> " sort:name";
            case 3 -> " sort:date";
            default -> "";
        };
        String fullQuery = raw + sortPrefix;

        statusLabel.setText("Searching…");
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();

        SwingWorker<List<FileData>, Void> worker = new SwingWorker<>() {
            @Override protected List<FileData> doInBackground() {
                return queryProcessor.executeQuery(fullQuery);
            }
            @Override protected void done() {
                try {
                    List<FileData> results = get();
                    renderResults(results, raw);
                    refreshSuggestions();
                } catch (Exception ex) {
                    statusLabel.setText("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void renderResults(List<FileData> results, String queryText) {
        resultsPanel.removeAll();

        if (results.isEmpty()) {
            JLabel empty = new JLabel("No results found for \"" + queryText + "\"");
            empty.setFont(FONT_BODY);
            empty.setForeground(TEXT_MUT);
            empty.setBorder(new EmptyBorder(40, 0, 0, 0));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsPanel.add(empty);
            statusLabel.setText("No results.");
        } else {
            statusLabel.setText(results.size() + " result(s) for \"" + queryText + "\"");
            for (FileData f : results) {
                resultsPanel.add(buildResultCard(f));
                resultsPanel.add(Box.createVerticalStrut(8));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
        // scroll back to top
        SwingUtilities.invokeLater(() ->
                resultsScroll.getVerticalScrollBar().setValue(0));
    }

    private JPanel buildResultCard(FileData f) {
        JPanel card = new JPanel(new BorderLayout(10, 4));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // ── left: file icon + score badge ──
        JLabel icon = new JLabel(fileIcon(f.getFilename()));
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JLabel scoreBadge = new JLabel("Score " + f.getPathScore());
        scoreBadge.setFont(FONT_SMALL);
        scoreBadge.setForeground(TAG_TEXT);
        scoreBadge.setBackground(TAG_BG);
        scoreBadge.setOpaque(true);
        scoreBadge.setBorder(new EmptyBorder(1, 6, 1, 6));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setBackground(CARD);
        left.add(icon,       BorderLayout.NORTH);
        left.add(scoreBadge, BorderLayout.SOUTH);

        // ── center: name + path + preview ──
        JLabel name = new JLabel(f.getFilename());
        name.setFont(new Font("Segoe UI", Font.BOLD, 13));
        name.setForeground(TEXT_PRI);

        JLabel path = new JLabel(f.getFilepath());
        path.setFont(FONT_SMALL);
        path.setForeground(TEXT_SEC);

        String preview = core.PreviewGenerator.generatePreview(f.getContent());
        JLabel prev = new JLabel("<html><body style='width:300px;color:#8a8aa5;font-size:11px'>"
                + htmlEscape(preview).replace("\n", "<br>") + "</body></html>");

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(CARD);
        center.add(name);
        center.add(Box.createVerticalStrut(2));
        center.add(path);
        center.add(Box.createVerticalStrut(4));
        center.add(prev);

        // ── right: date ──
        JLabel date = new JLabel(f.getFormattedDate());
        date.setFont(FONT_SMALL);
        date.setForeground(TEXT_MUT);
        date.setVerticalAlignment(SwingConstants.TOP);

        card.add(left,   BorderLayout.WEST);
        card.add(center, BorderLayout.CENTER);
        card.add(date,   BorderLayout.EAST);

        // hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setCardBg(card, CARD_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { setCardBg(card, CARD); }
            @Override public void mouseClicked(MouseEvent e) { showDetail(f); }
        });

        return card;
    }

    private void showDetail(FileData f) {
        detailTitle.setText(f.getFilename());
        detailPath.setText(f.getFilepath());
        detailMeta.setText("Last modified: " + f.getFormattedDate()
                + "   |   Path score: " + f.getPathScore());
        String content = f.getContent();
        detailArea.setText(content == null || content.isBlank()
                ? "(no content available)" : content);
        detailArea.setCaretPosition(0);
    }

    // ── Suggestion chips ──────────────────────────────────────────────────────

    private void refreshSuggestions() {
        String prefix = searchField.getText().trim();
        List<String> suggestions = prefix.length() < 2
            ? historyManager.getTopSuggestions(5)
            : historyManager.getPredictions(prefix, 5);


        suggestionsPanel.removeAll();

        if (!suggestions.isEmpty()) {
            JLabel label = new JLabel("Suggested queries: ");
            label.setFont(FONT_SMALL);
            label.setForeground(TEXT_MUT);
            suggestionsPanel.add(label);

            for (String s : suggestions) {
                JButton chip = new JButton(s);
                chip.setFont(FONT_SMALL);
                chip.setForeground(TAG_TEXT);
                chip.setBackground(TAG_BG);
                chip.setBorderPainted(false);
                chip.setFocusPainted(false);
                chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                chip.setBorder(new EmptyBorder(3, 10, 3, 10));
                chip.addActionListener(e -> {
                    searchField.setText(s);
                    doSearch();
                });
                suggestionsPanel.add(chip);
            }
        }

        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    // ── Index dialog ──────────────────────────────────────────────────────────

    private void showIndexDialog() {
        JDialog dlg = new JDialog(this, "Index Directory", true);
        dlg.setSize(480, 210);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(SURFACE);
        dlg.setLayout(new BorderLayout(0, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(SURFACE);
        form.setBorder(new EmptyBorder(18, 18, 12, 18));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.WEST;

        // Root dir row
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.NONE;
        form.add(label("Root directory:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        JTextField dirField = styledField();
        form.add(dirField, c);
        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JButton browse = accentButton("Browse", ACCENT_DIM);
        form.add(browse, c);

        // Ignore ext row
        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE;
        form.add(label("Ignore extension:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        JTextField extField = styledField();
        extField.setToolTipText("e.g.  .log  – leave blank to index all types");
        form.add(extField, c);

        // Progress bar
        c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(false);
        bar.setBackground(CARD);
        bar.setForeground(ACCENT);
        bar.setVisible(false);
        form.add(bar, c);

        dlg.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(SURFACE);
        JButton start  = accentButton("Start Indexing", ACCENT);
        JButton cancel = accentButton("Cancel",         ACCENT_DIM);
        btns.add(cancel);
        btns.add(start);
        dlg.add(btns, BorderLayout.SOUTH);

        // Browse
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(dlg) == JFileChooser.APPROVE_OPTION)
                dirField.setText(fc.getSelectedFile().getAbsolutePath());
        });

        // Start
        start.addActionListener(e -> {
            String rootPath = dirField.getText().trim();
            String ignoreExt = extField.getText().trim();
            if (ignoreExt.isEmpty()) ignoreExt = ".NONE";

            if (rootPath.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please enter a root directory.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File rootDir = new File(rootPath);
            if (!rootDir.exists() || !rootDir.isDirectory()) {
                JOptionPane.showMessageDialog(dlg, "Path does not exist or is not a directory.", "Validation", JOptionPane.ERROR_MESSAGE);
                return;
            }

            bar.setIndeterminate(true);
            bar.setVisible(true);
            start.setEnabled(false);
            browse.setEnabled(false);
            dirField.setEditable(false);
            extField.setEditable(false);
            statusLabel.setText("Indexing " + rootPath + " …");

            final String finalIgnore = ignoreExt;
            SwingWorker<Void, String> worker = new SwingWorker<>() {
                IndexReport report;
                @Override protected Void doInBackground() {
                    Crawler crawler = new Crawler(finalIgnore, repository);
                    Indexer indexer = new Indexer(crawler, repository);
                    report = indexer.runIndexing(rootPath);
                    return null;
                }
                @Override protected void done() {
                    bar.setIndeterminate(false);
                    bar.setValue(100);
                    dlg.dispose();
                    if (report != null) {
                        statusLabel.setText("Indexed: +" + report.added + " added, "
                                + report.updated + " updated, "
                                + report.deleted + " deleted, "
                                + report.ignored + " unchanged.");
                        JOptionPane.showMessageDialog(SearchEngineUI.this,
                                "Indexing complete!\n\n"
                                        + "  Added   : " + report.added   + "\n"
                                        + "  Updated : " + report.updated + "\n"
                                        + "  Deleted : " + report.deleted + "\n"
                                        + "  Ignored : " + report.ignored,
                                "Index Report", JOptionPane.INFORMATION_MESSAGE);
                    }
                    refreshSuggestions();
                }
            };
            worker.execute();
        });

        cancel.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    // ─────────────────────────────────── helpers ──────────────────────────────

    private JButton accentButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BODY);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 18, 8, 18));
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_SEC);
        return l;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(22);
        f.setFont(FONT_BODY);
        f.setBackground(CARD);
        f.setForeground(TEXT_PRI);
        f.setCaretColor(ACCENT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DIVIDER, 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void setCardBg(JPanel card, Color color) {
        card.setBackground(color);
        for (Component comp : card.getComponents()) {
            if (comp instanceof JPanel) ((JPanel) comp).setBackground(color);
        }
        card.repaint();
    }

    private void styleScrollBar(JScrollPane sp) {
        sp.getVerticalScrollBar().setBackground(BG);
        sp.getHorizontalScrollBar().setBackground(BG);
    }

    private String fileIcon(String filename) {
        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase() : "";
        return switch (ext) {
            case "java", "py", "js", "ts", "c", "cpp", "h" -> "💻";
            case "md", "txt"  -> "📄";
            case "json", "xml", "yaml", "yml" -> "🗂";
            case "html", "htm", "css" -> "🌐";
            case "sql" -> "🗃";
            case "sh", "bat" -> "⚙";
            default -> "📁";
        };
    }

    private static String htmlEscape(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    // ── IndexReport inner class for returning stats from the worker ───────────
    public static class IndexReport {
        public int added, updated, deleted, ignored;
    }
}
