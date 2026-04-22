package crossword;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class CrosswordApp extends JFrame {

    // ── colours ──────────────────────────────────────────────────────────────
    private static final Color BG_DARK      = new Color(18,  18,  30);
    private static final Color BG_PANEL     = new Color(28,  28,  46);
    private static final Color BG_CELL      = new Color(38,  38,  60);
    private static final Color ACCENT_BLUE  = new Color(99, 179, 237);
    private static final Color ACCENT_GOLD  = new Color(246,173,  85);
    private static final Color ACCENT_GREEN = new Color(104,211,145);
    private static final Color ACCENT_RED   = new Color(252,129,129);
    private static final Color TEXT_MAIN    = new Color(226,232,240);
    private static final Color TEXT_DIM     = new Color(113,128,150);
    private static final Color CELL_BLANK   = new Color(12,  12,  22);
    private static final Color CELL_FILLED  = new Color(45,  55,  72);
    private static final Color CELL_PLAYER  = new Color(49,  87, 115);
    private static final Color NUM_FG       = new Color(246,173,  85);

    // ── core logic ───────────────────────────────────────────────────────────
    private Dictionary      dict       = new Dictionary();
    private Grid            grid;
    private WordPlacer      placer     = new WordPlacer();
    private ClueAssigner    ca         = new ClueAssigner();
    private AnswerValidator validator  = new AnswerValidator();
    private ScoreManager    scorer     = new ScoreManager();
    private FileHandler     fh         = new FileHandler();

    // ── UI components ────────────────────────────────────────────────────────
    private JPanel      gridPanel;
    private JTextArea   acrossArea, downArea;
    private JLabel      scoreLabel, statusLabel, themeLabel;
    private JButton     btnAnswer, btnHint, btnSolution, btnSave, btnLoad, btnNew;
    private JSpinner    clueNumSpinner;
    private JComboBox<String> dirCombo;
    private JTextField  answerField;
    private JPanel      mainContent;
    private CardLayout  cardLayout;
    private JPanel      setupPanel, gamePanel;

    // setup screen components
    private JComboBox<String> gridSizeCombo;
    private JTextField  themeField;
    private JComboBox<String> themeCombo;
    private JLabel      wordCountLabel;

    private boolean showingSolution = false;
    private String  dictPath = "Dictionary.txt";

    // ─────────────────────────────────────────────────────────────────────────
    public CrosswordApp() {
        super(" Crossword Puzzle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        loadDictionary();
        buildUI();
        setVisible(true);
    }

    // ── dictionary loading ────────────────────────────────────────────────────
    private void loadDictionary() {
        // 1. Try to load from JAR classpath resource
        try (java.io.InputStream is = getClass().getResourceAsStream("/resources/Dictionary.txt")) {
            if (is != null) {
                dict.loadFromStream(is);
                return;
            }
        } catch (Exception ignored) {}
        // 2. Try file system paths
        String[] paths = { "Dictionary.txt", "resources/Dictionary.txt",
                           "src/Dictionary.txt", dictPath };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists()) { dict.loadFromFile(p); dictPath = p; return; }
        }
        // 3. Let user pick
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Locate Dictionary.txt");
        jfc.setFileFilter(new FileNameExtensionFilter("Text files","txt"));
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            dictPath = jfc.getSelectedFile().getAbsolutePath();
            dict.loadFromFile(dictPath);
        }
    }

    // ── top-level UI build ────────────────────────────────────────────────────
    private void buildUI() {
        setLayout(new BorderLayout());

        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_DARK);
        titleBar.setBorder(new EmptyBorder(14, 20, 8, 20));

        JLabel title = new JLabel("✦ CROSSWORD");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(ACCENT_GOLD);

        scoreLabel = new JLabel("Score: 0");
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        scoreLabel.setForeground(ACCENT_GREEN);

        statusLabel = new JLabel("Set up your puzzle to begin");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_DIM);

        titleBar.add(title,       BorderLayout.WEST);
        titleBar.add(scoreLabel,  BorderLayout.EAST);
        titleBar.add(statusLabel, BorderLayout.CENTER);
        add(titleBar, BorderLayout.NORTH);

        // Card layout: setup vs game
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BG_DARK);

        setupPanel = buildSetupPanel();
        gamePanel  = buildGamePanel();

        mainContent.add(setupPanel, "setup");
        mainContent.add(gamePanel,  "game");
        add(mainContent, BorderLayout.CENTER);

        cardLayout.show(mainContent, "setup");
    }

    // ── SETUP PANEL ───────────────────────────────────────────────────────────
    private JPanel buildSetupPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_DARK);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_BLUE, 1, true),
            new EmptyBorder(30, 40, 30, 40)));

        JLabel heading = new JLabel("New Puzzle Setup");
        heading.setFont(new Font("Serif", Font.BOLD, 22));
        heading.setForeground(ACCENT_GOLD);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Grid size
        JLabel sizeLabel = styledLabel("Grid Size:");
        String[] sizes = {"10 × 10", "15 × 15", "21 × 21"};
        gridSizeCombo = new JComboBox<>(sizes);
        gridSizeCombo.setSelectedIndex(1);
        styleCombo(gridSizeCombo);

        // Theme input
        JLabel themeLbl = styledLabel("Theme (optional):");
        themeField = new JTextField();
        themeField.setFont(new Font("Monospaced", Font.PLAIN, 14));
        themeField.setBackground(BG_CELL);
        themeField.setForeground(TEXT_MAIN);
        themeField.setCaretColor(ACCENT_BLUE);
        themeField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_BLUE, 1), new EmptyBorder(6, 8, 6, 8)));
        themeField.setMaximumSize(new Dimension(300, 36));

        // Available themes drop-down
        JLabel availLbl = styledLabel("Or pick a theme:");
        List<String> themes = dict.getAvailableThemes();
        themes.add(0, "— all words —");
        themeCombo = new JComboBox<>(themes.toArray(new String[0]));
        styleCombo(themeCombo);
        themeCombo.addActionListener(e -> {
            String sel = (String) themeCombo.getSelectedItem();
            if (sel != null && !sel.startsWith("—")) themeField.setText(sel);
            else themeField.setText("");
            updateWordCount();
        });

        wordCountLabel = new JLabel("Words available: " + dict.size());
        wordCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wordCountLabel.setForeground(TEXT_DIM);
        wordCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        themeField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateWordCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateWordCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JButton startBtn = styledButton("  Generate Puzzle", ACCENT_GREEN);
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startBtn.addActionListener(e -> startNewGame());

        // layout
        card.add(heading);
        card.add(Box.createVerticalStrut(24));
        card.add(sizeLabel);   card.add(Box.createVerticalStrut(6));
        card.add(gridSizeCombo);
        card.add(Box.createVerticalStrut(18));
        card.add(themeLbl);    card.add(Box.createVerticalStrut(6));
        card.add(themeField);
        card.add(Box.createVerticalStrut(10));
        card.add(availLbl);    card.add(Box.createVerticalStrut(6));
        card.add(themeCombo);
        card.add(Box.createVerticalStrut(8));
        card.add(wordCountLabel);
        card.add(Box.createVerticalStrut(24));
        card.add(startBtn);

        p.add(card);
        return p;
    }

    private void updateWordCount() {
        String t = themeField.getText().trim();
        int cnt = t.isEmpty() ? dict.size() : dict.getWordsByTheme(t).size();
        wordCountLabel.setText("Words available: " + cnt);
        wordCountLabel.setForeground(cnt < 5 ? ACCENT_RED : TEXT_DIM);
    }

    // ── GAME PANEL ────────────────────────────────────────────────────────────
    private JPanel buildGamePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(6, 10, 10, 10));

        // Left: grid
        gridPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (grid != null) drawGrid((Graphics2D) g);
            }
        };
        gridPanel.setBackground(BG_DARK);
        gridPanel.setPreferredSize(new Dimension(520, 520));

        JScrollPane gridScroll = new JScrollPane(gridPanel);
        gridScroll.setBackground(BG_DARK);
        gridScroll.getViewport().setBackground(BG_DARK);
        gridScroll.setBorder(new LineBorder(BG_PANEL, 2));

        // Right: clues + controls
        JPanel rightPanel = new JPanel(new BorderLayout(0, 8));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setPreferredSize(new Dimension(340, 0));

        // Clues panel
        JPanel cluesPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        cluesPanel.setBackground(BG_DARK);

        acrossArea = buildClueArea();
        downArea   = buildClueArea();

        cluesPanel.add(wrapCluePanel("ACROSS", acrossArea));
        cluesPanel.add(wrapCluePanel("DOWN",   downArea));

        // Controls panel
        JPanel controls = buildControlsPanel();

        rightPanel.add(cluesPanel,  BorderLayout.CENTER);
        rightPanel.add(controls,    BorderLayout.SOUTH);

        p.add(gridScroll,  BorderLayout.CENTER);
        p.add(rightPanel,  BorderLayout.EAST);
        return p;
    }

    private JPanel buildControlsPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(60,60,90),1,true),
            new EmptyBorder(12,12,12,12)));

        themeLabel = new JLabel("Theme: —");
        themeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        themeLabel.setForeground(ACCENT_BLUE);
        themeLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Clue number + direction row
        JPanel clueRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        clueRow.setBackground(BG_PANEL);
        clueRow.add(styledLabel("Clue #"));
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, 99, 1);
        clueNumSpinner = new JSpinner(model);
        styleSpinner(clueNumSpinner);
        clueRow.add(clueNumSpinner);

        dirCombo = new JComboBox<>(new String[]{"Across", "Down"});
        styleCombo(dirCombo);
        clueRow.add(dirCombo);

        // Answer row
        JPanel ansRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ansRow.setBackground(BG_PANEL);
        ansRow.add(styledLabel("Answer:"));
        answerField = new JTextField(12);
        answerField.setFont(new Font("Monospaced", Font.BOLD, 14));
        answerField.setBackground(BG_CELL);
        answerField.setForeground(TEXT_MAIN);
        answerField.setCaretColor(ACCENT_BLUE);
        answerField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_BLUE,1), new EmptyBorder(4,6,4,6)));
        ansRow.add(answerField);

        // Enter on answer field
        answerField.addActionListener(e -> submitAnswer());

        btnAnswer   = styledButton("  Submit Answer", ACCENT_GREEN);
        btnHint     = styledButton("  Use Hint (−3)", ACCENT_BLUE);
        btnSolution = styledButton("  Show Solution", new Color(160,100,200));
        btnSave     = styledButton("  Save",          ACCENT_GOLD);
        btnLoad     = styledButton("  Load",          ACCENT_GOLD);
        btnNew      = styledButton("  New Puzzle",    ACCENT_RED);

        btnAnswer  .addActionListener(e -> submitAnswer());
        btnHint    .addActionListener(e -> useHint());
        btnSolution.addActionListener(e -> toggleSolution());
        btnSave    .addActionListener(e -> savePuzzle());
        btnLoad    .addActionListener(e -> loadPuzzle());
        btnNew     .addActionListener(e -> { cardLayout.show(mainContent,"setup"); showingSolution=false; });

        for (JButton b : new JButton[]{btnAnswer,btnHint,btnSolution,btnSave,btnLoad,btnNew})
            b.setAlignmentX(LEFT_ALIGNMENT);

        JPanel btnRow1 = row(btnSave, btnLoad);
        JPanel btnRow2 = row(btnNew);

        p.add(themeLabel);
        p.add(Box.createVerticalStrut(8));
        p.add(clueRow);
        p.add(Box.createVerticalStrut(6));
        p.add(ansRow);
        p.add(Box.createVerticalStrut(8));
        p.add(btnAnswer);
        p.add(Box.createVerticalStrut(4));
        p.add(btnHint);
        p.add(Box.createVerticalStrut(4));
        p.add(btnSolution);
        p.add(Box.createVerticalStrut(8));
        p.add(btnRow1);
        p.add(Box.createVerticalStrut(4));
        p.add(btnRow2);
        return p;
    }

    // ── game logic ────────────────────────────────────────────────────────────
    private void startNewGame() {
        int sizeIdx = gridSizeCombo.getSelectedIndex();
        int size = sizeIdx == 0 ? 10 : sizeIdx == 2 ? 21 : 15;
        String theme = themeField.getText().trim();

        List<String> words = theme.isEmpty()
            ? dict.getAllWords()
            : dict.getWordsByTheme(theme);

        if (words.size() < 3) {
            JOptionPane.showMessageDialog(this,
                "Not enough words for theme \"" + theme + "\".\n" +
                "Please choose a different theme or leave it blank.",
                "Too few words", JOptionPane.WARNING_MESSAGE);
            return;
        }

        scorer.reset();
        grid = new Grid(size);
        grid.init();
        showingSolution = false;

        // Run placer on a background thread so UI stays responsive
        statusLabel.setText("Generating puzzle…");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        disableGameButtons(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                placer.solve(grid, words);
                ca.assign(grid, placer.placed, dict);
                return null;
            }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                disableGameButtons(false);
                updateScore();
                refreshClues();
                String tLabel = theme.isEmpty() ? "all words" : theme;
                themeLabel.setText("Theme: " + tLabel + "  |  Words: " + placer.placed.size());
                statusLabel.setText("Puzzle ready — good luck!");
                cardLayout.show(mainContent, "game");
                gridPanel.repaint();
            }
        };
        worker.execute();
    }

    private void submitAnswer() {
        if (grid == null) return;
        int num = (Integer) clueNumSpinner.getValue();
        char dir = dirCombo.getSelectedIndex() == 0 ? 'A' : 'D';
        String ans = answerField.getText().trim().toUpperCase();
        if (ans.isEmpty()) { setStatus("Please enter an answer.", ACCENT_RED); return; }

        String correct = ca.getWord(num, dir);
        if (correct.isEmpty()) { setStatus("Clue #" + num + " not found.", ACCENT_RED); return; }

        if (validator.check(ans, correct)) {
            scorer.correct();
            setStatus(" Correct! +" + 10, ACCENT_GREEN);
            fillPlayerCells(num, dir, ans);
        } else {
            scorer.wrong();
            setStatus(" Wrong answer. −5", ACCENT_RED);
        }
        answerField.setText("");
        updateScore();
        gridPanel.repaint();

        if (validator.isComplete(grid)) {
            JOptionPane.showMessageDialog(this,
                " Puzzle Complete!\nFinal score: " + scorer.score,
                "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void fillPlayerCells(int num, char dir, String ans) {
        for (ClueEntry e : (dir == 'A' ? ca.acrossClues : ca.downClues)) {
            if (e.number != num) continue;
            int dr = dir == 'D' ? 1 : 0;
            int dc = dir == 'A' ? 1 : 0;
            for (int i = 0; i < ans.length(); i++)
                grid.playerCells[e.row + dr*i][e.col + dc*i] = ans.charAt(i);
            break;
        }
    }

    private void useHint() {
        if (grid == null) return;
        int num = (Integer) clueNumSpinner.getValue();
        char dir = dirCombo.getSelectedIndex() == 0 ? 'A' : 'D';
        String hint = ca.getHint(num, dir);
        scorer.usedHint();
        updateScore();
        String msg = (hint.isEmpty() || hint.equals("No hint"))
            ? "No hint available for this clue."
            : "Hint: " + hint;
        setStatus(msg + "  (−3)", ACCENT_BLUE);
    }

    private void toggleSolution() {
        if (grid == null) return;
        showingSolution = !showingSolution;
        btnSolution.setText(showingSolution ? "◉  Hide Solution" : "◉  Show Solution");
        gridPanel.repaint();
    }

    private void savePuzzle() {
        if (grid == null) return;
        JFileChooser jfc = new JFileChooser(".");
        jfc.setSelectedFile(new File("puzzle.save"));
        if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            boolean ok = fh.save(grid, scorer, jfc.getSelectedFile().getAbsolutePath());
            setStatus(ok ? "Puzzle saved." : "Save failed.", ok ? ACCENT_GREEN : ACCENT_RED);
        }
    }

    private void loadPuzzle() {
        JFileChooser jfc = new JFileChooser(".");
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (grid == null) grid = new Grid(15);
            boolean ok = fh.load(grid, scorer, jfc.getSelectedFile().getAbsolutePath());
            if (ok) {
                ca.assign(grid, placer.placed, dict);
                refreshClues();
                updateScore();
                setStatus("Puzzle loaded.", ACCENT_GREEN);
                cardLayout.show(mainContent, "game");
                gridPanel.repaint();
            } else {
                setStatus("Load failed.", ACCENT_RED);
            }
        }
    }

    // ── grid rendering ────────────────────────────────────────────────────────
    private void drawGrid(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelW = gridPanel.getWidth();
        int panelH = gridPanel.getHeight();
        int cellSize = Math.min((panelW - 20) / grid.size, (panelH - 20) / grid.size);
        int offsetX = (panelW - cellSize * grid.size) / 2;
        int offsetY = (panelH - cellSize * grid.size) / 2;

        Font numFont  = new Font("SansSerif", Font.BOLD, Math.max(7, cellSize / 4));
        Font cellFont = new Font("Monospaced", Font.BOLD, Math.max(10, cellSize * 55 / 100));

        for (int r = 0; r < grid.size; r++) {
            for (int c = 0; c < grid.size; c++) {
                int x = offsetX + c * cellSize;
                int y = offsetY + r * cellSize;
                char sol = grid.cells[r][c];
                char ply = grid.playerCells[r][c];

                if (sol == ' ') {
                    g.setColor(CELL_BLANK);
                    g.fillRect(x, y, cellSize, cellSize);
                } else {
                    if (ply != ' ')      g.setColor(CELL_PLAYER);
                    else                 g.setColor(CELL_FILLED);
                    g.fillRect(x, y, cellSize, cellSize);

                    // clue number
                    int num = grid.clueNums[r][c];
                    if (num > 0) {
                        g.setFont(numFont);
                        g.setColor(NUM_FG);
                        g.drawString(String.valueOf(num), x + 2, y + numFont.getSize() + 1);
                    }

                    // letter
                    g.setFont(cellFont);
                    char displayChar = ' ';
                    if (showingSolution)         displayChar = sol;
                    else if (ply != ' ')         displayChar = ply;

                    if (displayChar != ' ') {
                        g.setColor(showingSolution && ply == ' ' ? ACCENT_RED : TEXT_MAIN);
                        FontMetrics fm = g.getFontMetrics();
                        int tx = x + (cellSize - fm.charWidth(displayChar)) / 2;
                        int ty = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
                        g.drawString(String.valueOf(displayChar), tx, ty);
                    }
                }

                // grid border
                g.setColor(BG_DARK);
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }

    // ── clue list ─────────────────────────────────────────────────────────────
    private void refreshClues() {
        StringBuilder across = new StringBuilder();
        for (ClueEntry e : ca.acrossClues)
            across.append(e.number).append(". ").append(e.clue)
                  .append(" (").append(e.length).append(")\n");

        StringBuilder down = new StringBuilder();
        for (ClueEntry e : ca.downClues)
            down.append(e.number).append(". ").append(e.clue)
                .append(" (").append(e.length).append(")\n");

        acrossArea.setText(across.toString());
        downArea.setText(down.toString());
        acrossArea.setCaretPosition(0);
        downArea.setCaretPosition(0);
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private void updateScore() {
        scoreLabel.setText("Score: " + scorer.score);
        scoreLabel.setForeground(scorer.score >= 0 ? ACCENT_GREEN : ACCENT_RED);
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private void disableGameButtons(boolean disable) {
        if (btnAnswer != null) {
            btnAnswer.setEnabled(!disable);
            btnHint.setEnabled(!disable);
        }
    }

    // ── widget builders ───────────────────────────────────────────────────────
    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(TEXT_DIM);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton styledButton(String text, Color accent) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed())
                    g2.setColor(accent.darker());
                else if (getModel().isRollover())
                    g2.setColor(accent.darker());
                else
                    g2.setColor(BG_CELL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(accent);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(accent);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        b.setAlignmentX(LEFT_ALIGNMENT);
        return b;
    }

    private <T> void styleCombo(JComboBox<T> cb) {
        cb.setBackground(BG_CELL);
        cb.setForeground(TEXT_MAIN);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBorder(new LineBorder(ACCENT_BLUE, 1));
        cb.setMaximumSize(new Dimension(300, 32));
        cb.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setBackground(BG_CELL);
        sp.setForeground(TEXT_MAIN);
        sp.setFont(new Font("Monospaced", Font.BOLD, 13));
        sp.setBorder(new LineBorder(ACCENT_BLUE, 1));
        JFormattedTextField tf = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
        tf.setBackground(BG_CELL);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(ACCENT_BLUE);
        sp.setPreferredSize(new Dimension(60, 28));
    }

    private JTextArea buildClueArea() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(BG_PANEL);
        ta.setForeground(TEXT_MAIN);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setBorder(new EmptyBorder(4, 4, 4, 4));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private JPanel wrapCluePanel(String title, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(60,60,90),1,true),
            new EmptyBorder(4,4,4,4)));

        JLabel hdr = new JLabel(title);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 12));
        hdr.setForeground(ACCENT_GOLD);
        hdr.setBorder(new EmptyBorder(0,2,4,0));

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        sp.setBackground(BG_PANEL);
        sp.getViewport().setBackground(BG_PANEL);
        sp.setPreferredSize(new Dimension(0, 260));

        p.add(hdr, BorderLayout.NORTH);
        p.add(sp,  BorderLayout.CENTER);
        return p;
    }

    private JPanel row(JButton... btns) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setBackground(BG_PANEL);
        p.setAlignmentX(LEFT_ALIGNMENT);
        for (JButton b : btns) {
            b.setMaximumSize(new Dimension(140, 28));
            p.add(b);
        }
        return p;
    }

    // ── main ─────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Use system look then override with custom colours
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        UIManager.put("OptionPane.background",         BG_PANEL);
        UIManager.put("Panel.background",              BG_PANEL);
        UIManager.put("OptionPane.messageForeground",  TEXT_MAIN);

        SwingUtilities.invokeLater(CrosswordApp::new);
    }
}
