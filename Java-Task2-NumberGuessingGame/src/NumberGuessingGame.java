import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class NumberGuessingGame extends JFrame {

    // Game Logic Variables
    private int targetNumber;
    private int maxAttempts;
    private int currentAttempts;
    private int currentRound = 1;
    private int minRange = 1;
    private int maxRange = 100;
    private boolean gameEnded = false;

    // Score Tracking
    private int score = 0;
    private int wins = 0;
    private int losses = 0;

    // UI Components
    private JLabel titleLabel, hintLabel, attemptsLabel, roundLabel,
            targetDisplayLabel, scoreLabel;

    private JTextField guessField;
    private JButton guessButton, playAgainButton;
    private JComboBox<String> difficultyBox;
    private JTextArea historyArea;
    private StatusBannerPanel statusPanel;

    // Modern Dark Theme Colors
    private final Color COLOR_BG = new Color(18, 22, 36);
    private final Color COLOR_CARD = new Color(30, 38, 58);
    private final Color COLOR_ACCENT = new Color(99, 102, 241);
    private final Color COLOR_HOVER = new Color(79, 70, 229);
    private final Color COLOR_TEXT = new Color(243, 244, 246);
    private final Color COLOR_MUTED = new Color(156, 163, 175);
    private final Color COLOR_SUCCESS = new Color(34, 197, 94);
    private final Color COLOR_DANGER = new Color(239, 68, 68);

    public NumberGuessingGame() {

        setTitle("Number Guessing Game - Oasis Infobyte Task 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Background Panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(mainPanel);

        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Center Area
        JPanel centerContainer = new JPanel(new GridLayout(1, 2, 15, 0));
        centerContainer.setOpaque(false);

        centerContainer.add(createGameCard());
        centerContainer.add(createHistoryCard());

        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Start initial round
        startNewRound();
    }

    // =========================================================
    // HEADER
    // =========================================================

    private JPanel createHeaderPanel() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        titleLabel = new JLabel(
                "NUMBER GUESSING GAME",
                SwingConstants.LEFT
        );

        titleLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 22)
        );

        titleLabel.setForeground(COLOR_TEXT);

        scoreLabel = new JLabel(
                "Score: 0 | Wins: 0 | Losses: 0",
                SwingConstants.LEFT
        );

        scoreLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        scoreLabel.setForeground(COLOR_MUTED);

        JPanel headerLeft = new JPanel();
        headerLeft.setOpaque(false);
        headerLeft.setLayout(
                new BoxLayout(headerLeft, BoxLayout.Y_AXIS)
        );

        headerLeft.add(titleLabel);
        headerLeft.add(scoreLabel);

        JPanel rightControls = new JPanel(
                new FlowLayout(
                        FlowLayout.RIGHT,
                        10,
                        0
                )
        );

        rightControls.setOpaque(false);

        JLabel diffLabel = new JLabel("Difficulty:");

        diffLabel.setForeground(COLOR_MUTED);

        diffLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        String[] difficulties = {
                "Easy (1-50, 10 attempts)",
                "Medium (1-100, 7 attempts)",
                "Hard (1-200, 5 attempts)"
        };

        difficultyBox = new JComboBox<>(difficulties);

        difficultyBox.setSelectedIndex(1);

        difficultyBox.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        difficultyBox.setBackground(COLOR_CARD);
        difficultyBox.setForeground(COLOR_TEXT);
        difficultyBox.setFocusable(false);

        difficultyBox.addActionListener(e -> {

            playSound(400, 80);

            startNewRound();
        });

        rightControls.add(diffLabel);
        rightControls.add(difficultyBox);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);

        return header;
    }

    // =========================================================
    // GAME CARD
    // =========================================================

    private JPanel createGameCard() {

        ModernCardPanel card =
                new ModernCardPanel(COLOR_CARD);

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        statusPanel = new StatusBannerPanel();

        statusPanel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        roundLabel = new JLabel(
                "Round 1",
                SwingConstants.CENTER
        );

        roundLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        roundLabel.setForeground(COLOR_ACCENT);

        roundLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        hintLabel = new JLabel(
                "Enter a number to start!",
                SwingConstants.CENTER
        );

        hintLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        15
                )
        );

        hintLabel.setForeground(COLOR_TEXT);

        hintLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        attemptsLabel = new JLabel(
                "Attempts Left: 7",
                SwingConstants.CENTER
        );

        attemptsLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        attemptsLabel.setForeground(COLOR_MUTED);

        attemptsLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        targetDisplayLabel = new JLabel(
                "?",
                SwingConstants.CENTER
        );

        targetDisplayLabel.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        36
                )
        );

        targetDisplayLabel.setForeground(COLOR_TEXT);

        targetDisplayLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        // Input Field
        guessField = new JTextField();

        guessField.setMaximumSize(
                new Dimension(200, 40)
        );

        guessField.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        18
                )
        );

        guessField.setHorizontalAlignment(
                JTextField.CENTER
        );

        guessField.setBackground(COLOR_BG);
        guessField.setForeground(COLOR_TEXT);
        guessField.setCaretColor(COLOR_TEXT);

        guessField.setBorder(
                BorderFactory.createLineBorder(
                        COLOR_ACCENT,
                        1
                )
        );

        guessField.addActionListener(
                e -> processGuess()
        );

        // Guess Button
        guessButton = createStyledButton(
                "GUESS",
                COLOR_ACCENT,
                COLOR_HOVER
        );

        guessButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        guessButton.addActionListener(
                e -> processGuess()
        );

        // Play Again Button
        playAgainButton = createStyledButton(
                "PLAY AGAIN",
                COLOR_SUCCESS,
                new Color(22, 163, 74)
        );

        playAgainButton.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        playAgainButton.setVisible(false);

        playAgainButton.addActionListener(e -> {

            playSound(600, 100);

            startNewRound();
        });

        card.add(roundLabel);

        card.add(
                Box.createVerticalStrut(10)
        );

        card.add(statusPanel);

        card.add(
                Box.createVerticalStrut(15)
        );

        card.add(targetDisplayLabel);

        card.add(
                Box.createVerticalStrut(10)
        );

        card.add(hintLabel);

        card.add(
                Box.createVerticalStrut(5)
        );

        card.add(attemptsLabel);

        card.add(
                Box.createVerticalStrut(20)
        );

        card.add(guessField);

        card.add(
                Box.createVerticalStrut(15)
        );

        card.add(guessButton);

        card.add(playAgainButton);

        return card;
    }

    // =========================================================
    // HISTORY CARD
    // =========================================================

    private JPanel createHistoryCard() {

        ModernCardPanel card =
                new ModernCardPanel(COLOR_CARD);

        card.setLayout(
                new BorderLayout(
                        10,
                        10
                )
        );

        card.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        JLabel historyTitle =
                new JLabel(
                        "Score History & Summary"
                );

        historyTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        historyTitle.setForeground(COLOR_TEXT);

        historyArea = new JTextArea();

        historyArea.setEditable(false);

        historyArea.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13
                )
        );

        historyArea.setBackground(COLOR_BG);
        historyArea.setForeground(COLOR_TEXT);

        historyArea.setBorder(
                new EmptyBorder(
                        10,
                        10,
                        10,
                        10
                )
        );

        JScrollPane scrollPane =
                new JScrollPane(historyArea);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(
                                50,
                                60,
                                80
                        )
                )
        );

        scrollPane.getVerticalScrollBar()
                .setUI(
                        new javax.swing.plaf.basic.BasicScrollBarUI() {

                            @Override
                            protected void configureScrollBarColors() {

                                this.thumbColor =
                                        COLOR_ACCENT;

                                this.trackColor =
                                        COLOR_BG;
                            }
                        }
                );

        card.add(
                historyTitle,
                BorderLayout.NORTH
        );

        card.add(
                scrollPane,
                BorderLayout.CENTER
        );

        return card;
    }

    // =========================================================
    // BUTTON
    // =========================================================

    private JButton createStyledButton(
            String text,
            Color bg,
            Color hover
    ) {

        JButton btn = new JButton(text);

        btn.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);

        btn.setFocusPainted(false);

        btn.setBorder(
                new EmptyBorder(
                        10,
                        25,
                        10,
                        25
                )
        );

        btn.setCursor(
                new Cursor(
                        Cursor.HAND_CURSOR
                )
        );

        btn.setMaximumSize(
                new Dimension(
                        200,
                        40
                )
        );

        btn.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    public void mouseEntered(
                            java.awt.event.MouseEvent evt
                    ) {

                        if (btn.isEnabled()) {

                            btn.setBackground(hover);
                        }
                    }

                    public void mouseExited(
                            java.awt.event.MouseEvent evt
                    ) {

                        if (btn.isEnabled()) {

                            btn.setBackground(bg);
                        }
                    }
                }
        );

        return btn;
    }

    // =========================================================
    // START NEW ROUND
    // =========================================================

    private void startNewRound() {

        int diffIndex =
                difficultyBox.getSelectedIndex();

        if (diffIndex == 0) {

            // Easy
            minRange = 1;
            maxRange = 50;
            maxAttempts = 10;

        } else if (diffIndex == 1) {

            // Medium
            minRange = 1;
            maxRange = 100;
            maxAttempts = 7;

        } else {

            // Hard
            minRange = 1;
            maxRange = 200;
            maxAttempts = 5;
        }

        // Generate random number
        Random rand = new Random();

        targetNumber =
                rand.nextInt(
                        maxRange - minRange + 1
                ) + minRange;

        currentAttempts = 0;

        gameEnded = false;

        roundLabel.setText(
                "Round " + currentRound
        );

        scoreLabel.setText(
                "Score: " + score
                        + " | Wins: " + wins
                        + " | Losses: " + losses
        );

        hintLabel.setText(
                "Guess a number between "
                        + minRange
                        + " & "
                        + maxRange
        );

        hintLabel.setForeground(
                COLOR_TEXT
        );

        attemptsLabel.setText(
                "Attempts Left: "
                        + maxAttempts
        );

        targetDisplayLabel.setText("?");

        targetDisplayLabel.setForeground(
                COLOR_TEXT
        );

        guessField.setText("");

        guessField.setEnabled(true);

        guessButton.setVisible(true);

        playAgainButton.setVisible(false);

        statusPanel.setStatus(
                Status.NEUTRAL
        );

        guessField.requestFocus();
    }

    // =========================================================
    // PROCESS GUESS
    // =========================================================

    private void processGuess() {

        if (gameEnded) {
            return;
        }

        String input =
                guessField.getText().trim();

        try {

            int guess =
                    Integer.parseInt(input);

            // Range validation
            if (
                    guess < minRange
                            ||
                    guess > maxRange
            ) {

                playSound(
                        200,
                        100
                );

                hintLabel.setText(
                        "Out of range! ("
                                + minRange
                                + "-"
                                + maxRange
                                + ")"
                );

                hintLabel.setForeground(
                        COLOR_DANGER
                );

                return;
            }

            currentAttempts++;

            int remaining =
                    maxAttempts
                            - currentAttempts;

            // =================================================
            // CORRECT
            // =================================================

            if (guess == targetNumber) {

                playWinSound();

                statusPanel.setStatus(
                        Status.WIN
                );

                hintLabel.setText(
                        "Correct! You nailed it!"
                );

                hintLabel.setForeground(
                        COLOR_SUCCESS
                );

                targetDisplayLabel.setText(
                        String.valueOf(
                                targetNumber
                        )
                );

                targetDisplayLabel.setForeground(
                        COLOR_SUCCESS
                );

                // Score calculation
                int pointsEarned =
                        (maxAttempts
                                - currentAttempts
                                + 1) * 10;

                score += pointsEarned;

                wins++;

                historyArea.append(
                        "Round "
                                + currentRound
                                + " - Guessed in "
                                + currentAttempts
                                + " attempts! (SUCCESS)"
                                + " - +"
                                + pointsEarned
                                + " points\n"
                );

                endGame();

            }

            // =================================================
            // LOST
            // =================================================

            else if (
                    currentAttempts
                            >= maxAttempts
            ) {

                playLoseSound();

                statusPanel.setStatus(
                        Status.LOSE
                );

                hintLabel.setText(
                        "You Lost! No attempts left."
                );

                hintLabel.setForeground(
                        COLOR_DANGER
                );

                targetDisplayLabel.setText(
                        String.valueOf(
                                targetNumber
                        )
                );

                targetDisplayLabel.setForeground(
                        COLOR_DANGER
                );

                losses++;

                historyArea.append(
                        "Round "
                                + currentRound
                                + " - Failed "
                                + "(Number was "
                                + targetNumber
                                + ")\n"
                );

                endGame();

            }

            // =================================================
            // TOO HIGH / TOO LOW
            // =================================================

            else {

                playSound(
                        350,
                        80
                );

                if (guess > targetNumber) {

                    hintLabel.setText(
                            "Too High! Try lower."
                    );

                    hintLabel.setForeground(
                            new Color(
                                    251,
                                    146,
                                    60
                            )
                    );

                } else {

                    hintLabel.setText(
                            "Too Low! Try higher."
                    );

                    hintLabel.setForeground(
                            new Color(
                                    56,
                                    189,
                                    248
                            )
                    );
                }

                attemptsLabel.setText(
                        "Attempts Left: "
                                + remaining
                );
            }

            // Update score display
            scoreLabel.setText(
                    "Score: " + score
                            + " | Wins: " + wins
                            + " | Losses: " + losses
            );

            guessField.setText("");

            guessField.requestFocus();

        } catch (NumberFormatException ex) {

            playSound(
                    200,
                    100
            );

            hintLabel.setText(
                    "Invalid input! Enter numbers only."
            );

            hintLabel.setForeground(
                    COLOR_DANGER
            );
        }
    }

    // =========================================================
    // END GAME
    // =========================================================

    private void endGame() {

        gameEnded = true;

        guessField.setEnabled(false);

        guessButton.setVisible(false);

        playAgainButton.setVisible(true);

        currentRound++;

        scoreLabel.setText(
                "Score: " + score
                        + " | Wins: " + wins
                        + " | Losses: " + losses
        );
    }

    // =========================================================
    // AUDIO
    // =========================================================

    private void playSound(
            int hz,
            int msecs
    ) {

        new Thread(() -> {

            try {

                float sampleRate = 8000f;

                byte[] buf =
                        new byte[msecs * 8];

                for (
                        int i = 0;
                        i < buf.length;
                        i++
                ) {

                    double angle =
                            i
                                    / (sampleRate / hz)
                                    * 2.0
                                    * Math.PI;

                    buf[i] =
                            (byte)
                                    (Math.sin(angle)
                                            * 80);
                }

                AudioFormat af =
                        new AudioFormat(
                                sampleRate,
                                8,
                                1,
                                true,
                                false
                        );

                SourceDataLine sdl =
                        AudioSystem
                                .getSourceDataLine(af);

                sdl.open(af);

                sdl.start();

                sdl.write(
                        buf,
                        0,
                        buf.length
                );

                sdl.drain();

                sdl.close();

            } catch (Exception ignored) {
            }

        }).start();
    }

    private void playWinSound() {

        new Thread(() -> {

            try {

                int[] notes = {
                        523,
                        659,
                        783,
                        1046
                };

                for (int note : notes) {

                    playSound(
                            note,
                            90
                    );

                    Thread.sleep(80);
                }

            } catch (Exception ignored) {
            }

        }).start();
    }

    private void playLoseSound() {

        new Thread(() -> {

            try {

                int[] notes = {
                        400,
                        350,
                        300,
                        250
                };

                for (int note : notes) {

                    playSound(
                            note,
                            120
                    );

                    Thread.sleep(110);
                }

            } catch (Exception ignored) {
            }

        }).start();
    }

    // =========================================================
    // CUSTOM CARD PANEL
    // =========================================================

    private static class ModernCardPanel
            extends JPanel {

        private final Color backgroundColor;

        public ModernCardPanel(Color bg) {

            this.backgroundColor = bg;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(
                    backgroundColor
            );

            g2.fill(
                    new RoundRectangle2D.Float(
                            0,
                            0,
                            getWidth(),
                            getHeight(),
                            20,
                            20
                    )
            );

            g2.dispose();
        }
    }

    // =========================================================
    // STATUS
    // =========================================================

    enum Status {
        NEUTRAL,
        WIN,
        LOSE
    }

    private class StatusBannerPanel
            extends JPanel {

        private Status status =
                Status.NEUTRAL;

        public StatusBannerPanel() {

            setPreferredSize(
                    new Dimension(
                            80,
                            80
                    )
            );

            setOpaque(false);
        }

        public void setStatus(
                Status status
        ) {

            this.status = status;

            repaint();
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int cx =
                    getWidth() / 2;

            int cy =
                    getHeight() / 2;

            int r = 35;

            if (status == Status.NEUTRAL) {

                g2.setColor(
                        new Color(
                                99,
                                102,
                                241,
                                40
                        )
                );

                g2.fillOval(
                        cx - r,
                        cy - r,
                        r * 2,
                        r * 2
                );

                g2.setColor(
                        COLOR_ACCENT
                );

                g2.setStroke(
                        new BasicStroke(3)
                );

                g2.drawOval(
                        cx - r,
                        cy - r,
                        r * 2,
                        r * 2
                );

                // Target Icon
                g2.drawOval(
                        cx - 15,
                        cy - 15,
                        30,
                        30
                );

                g2.fillOval(
                        cx - 5,
                        cy - 5,
                        10,
                        10
                );

            } else if (
                    status == Status.WIN
            ) {

                g2.setColor(
                        COLOR_SUCCESS
                );

                g2.fillOval(
                        cx - r,
                        cy - r,
                        r * 2,
                        r * 2
                );

                // Checkmark
                g2.setColor(
                        Color.WHITE
                );

                g2.setStroke(
                        new BasicStroke(
                                5,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g2.drawLine(
                        cx - 15,
                        cy,
                        cx - 5,
                        cy + 12
                );

                g2.drawLine(
                        cx - 5,
                        cy + 12,
                        cx + 18,
                        cy - 12
                );

            } else if (
                    status == Status.LOSE
            ) {

                g2.setColor(
                        COLOR_DANGER
                );

                g2.fillOval(
                        cx - r,
                        cy - r,
                        r * 2,
                        r * 2
                );

                // Cross
                g2.setColor(
                        Color.WHITE
                );

                g2.setStroke(
                        new BasicStroke(
                                5,
                                BasicStroke.CAP_ROUND,
                                BasicStroke.JOIN_ROUND
                        )
                );

                g2.drawLine(
                        cx - 14,
                        cy - 14,
                        cx + 14,
                        cy + 14
                );

                g2.drawLine(
                        cx + 14,
                        cy - 14,
                        cx - 14,
                        cy + 14
                );
            }

            g2.dispose();
        }
    }

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            NumberGuessingGame game =
                    new NumberGuessingGame();

            game.setVisible(true);
        });
    }
}