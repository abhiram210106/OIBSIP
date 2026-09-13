package com.oasis.exam.ui;

import com.oasis.exam.model.ExamSession;
import com.oasis.exam.model.Question;
import com.oasis.exam.model.QuestionStatus;
import com.oasis.exam.model.User;
import com.oasis.exam.ui.components.ModernButton;
import com.oasis.exam.ui.components.QuestionPaletteButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Real-world CBT Examination Screen featuring:
 * - Live countdown timer (javax.swing.Timer) with warning colors and auto-submission
 * - Single MCQ display with 4 styled options (ButtonGroup + JRadioButton)
 * - Navigation: Next, Previous, Clear Response, Mark for Review & Next
 * - Interactive Question Palette (TCS iON / NTA style) with live color feedback
 * - Candidate profile bar and manual submit modal summary
 */
public class ExamPanel extends JPanel {
    private final Consumer<ExamSession> onExamSubmitted;
    private ExamSession currentSession;
    private int currentQuestionIndex = 0;

    // Timer
    private Timer countdownTimer;
    private JLabel timerLabel;
    private JPanel timerCard;

    // Header Candidate Details
    private JLabel candidateInfoLabel;
    private JLabel systemIdLabel;

    // Question Area Components
    private JLabel qNumberLabel;
    private JLabel qCategoryLabel;
    private JLabel qMarksLabel;
    private JLabel qTextLabel;

    // Options
    private final JRadioButton[] optionRadios = new JRadioButton[4];
    private final JPanel[] optionCards = new JPanel[4];
    private final JLabel[] optionLabels = new JLabel[4];
    private ButtonGroup optionButtonGroup;

    // Bottom Navigation Buttons
    private ModernButton prevButton;
    private ModernButton nextButton;
    private ModernButton markReviewButton;
    private ModernButton clearResponseButton;
    private ModernButton submitButton;

    // Question Palette
    private JPanel paletteGridPanel;
    private QuestionPaletteButton[] paletteButtons;
    private JLabel countAnsweredLabel;
    private JLabel countNotAnsweredLabel;
    private JLabel countNotVisitedLabel;
    private JLabel countReviewLabel;

    // Exam submission state lock
    private boolean isExamSubmitted = false;

    public ExamPanel(Consumer<ExamSession> onExamSubmitted) {
        this.onExamSubmitted = onExamSubmitted;
        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_APP);

        initUI();
    }

    public void startExam(ExamSession session) {
        this.currentSession = session;
        this.currentQuestionIndex = 0;
        this.isExamSubmitted = false;

        // Re-enable inputs for fresh exam session
        for (JRadioButton r : optionRadios) {
            if (r != null) r.setEnabled(true);
        }
        for (JPanel c : optionCards) {
            if (c != null) c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        if (prevButton != null) prevButton.setEnabled(true);
        if (nextButton != null) nextButton.setEnabled(true);
        if (markReviewButton != null) markReviewButton.setEnabled(true);
        if (clearResponseButton != null) clearResponseButton.setEnabled(true);
        if (submitButton != null) submitButton.setEnabled(true);

        // Setup candidate info
        User user = session.getCandidate();
        candidateInfoLabel.setText("👤 " + user.getDisplayName() + " (" + user.getRollNumber() + ")");
        systemIdLabel.setText("🖥 Node: C-042 | Hall: Lab 3");

        // Rebuild Question Palette Buttons
        setupQuestionPalette();

        // Load first question
        loadQuestion(0);

        // Start countdown timer
        startCountdownTimer();
    }

    private void initUI() {
        // TOP HEADER BAR
        JPanel topHeader = createHeaderBar();
        add(topHeader, BorderLayout.NORTH);

        // CENTER SPLIT: Left Question Area (70%) + Right Palette (30%)
        JPanel centerContainer = new JPanel(new BorderLayout(14, 0));
        centerContainer.setBackground(UIConstants.BG_APP);
        centerContainer.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // LEFT: Question Card + Bottom Navigation
        JPanel questionAreaWrapper = new JPanel(new BorderLayout(0, 10));
        questionAreaWrapper.setOpaque(false);

        JPanel questionCard = createQuestionCard();
        questionAreaWrapper.add(questionCard, BorderLayout.CENTER);

        JPanel navigationBar = createNavigationBar();
        questionAreaWrapper.add(navigationBar, BorderLayout.SOUTH);

        centerContainer.add(questionAreaWrapper, BorderLayout.CENTER);

        // RIGHT: Real-life Question Palette
        JPanel paletteSidebar = createPaletteSidebar();
        centerContainer.add(paletteSidebar, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(UIConstants.BG_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Left Brand & Exam Subject
        JPanel leftBrand = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftBrand.setOpaque(false);

        JLabel portalLogo = new JLabel("OASIS CBT");
        portalLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        portalLogo.setForeground(UIConstants.BRAND_ACCENT);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        sep.setForeground(UIConstants.BG_HEADER_BAR);

        JLabel examSubject = new JLabel("Java Professional Certification Exam 2026");
        examSubject.setFont(UIConstants.FONT_BODY_BOLD);
        examSubject.setForeground(Color.WHITE);

        leftBrand.add(portalLogo);
        leftBrand.add(sep);
        leftBrand.add(examSubject);
        header.add(leftBrand, BorderLayout.WEST);

        // Center Candidate Info
        JPanel centerInfo = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        centerInfo.setOpaque(false);

        candidateInfoLabel = new JLabel("👤 Candidate");
        candidateInfoLabel.setFont(UIConstants.FONT_BODY_SMALL);
        candidateInfoLabel.setForeground(new Color(226, 232, 240));

        systemIdLabel = new JLabel("🖥 Node: C-042");
        systemIdLabel.setFont(UIConstants.FONT_BODY_SMALL);
        systemIdLabel.setForeground(UIConstants.TEXT_MUTED);

        centerInfo.add(candidateInfoLabel);
        centerInfo.add(systemIdLabel);
        header.add(centerInfo, BorderLayout.CENTER);

        // Right Timer Card & Submit Button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        timerCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_HEADER_BAR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        timerCard.setOpaque(false);
        timerCard.setPreferredSize(new Dimension(180, 36));

        JLabel clockIcon = new JLabel("⏱ Time Left:");
        clockIcon.setFont(UIConstants.FONT_SMALL_BOLD);
        clockIcon.setForeground(Color.WHITE);

        timerLabel = new JLabel("15:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        timerLabel.setForeground(UIConstants.TIMER_NORMAL);

        timerCard.add(clockIcon);
        timerCard.add(timerLabel);

        ModernButton topSubmitBtn = new ModernButton("Submit Exam", ModernButton.ButtonVariant.DANGER);
        topSubmitBtn.setPreferredSize(new Dimension(125, 36));
        topSubmitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        topSubmitBtn.addActionListener(e -> promptSubmitExam(false));

        rightPanel.add(timerCard);
        rightPanel.add(topSubmitBtn);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createQuestionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Question Card Header
        JPanel qHeader = new JPanel(new BorderLayout());
        qHeader.setOpaque(false);

        JPanel leftMeta = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftMeta.setOpaque(false);

        qNumberLabel = new JLabel("Question 1 of 15");
        qNumberLabel.setFont(UIConstants.FONT_SECTION_TITLE);
        qNumberLabel.setForeground(UIConstants.TEXT_MAIN);

        qCategoryLabel = new JLabel("JVM Architecture");
        qCategoryLabel.setFont(UIConstants.FONT_BODY_SMALL);
        qCategoryLabel.setForeground(UIConstants.BRAND_PRIMARY);
        qCategoryLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BRAND_PRIMARY_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));

        leftMeta.add(qNumberLabel);
        leftMeta.add(qCategoryLabel);
        qHeader.add(leftMeta, BorderLayout.WEST);

        qMarksLabel = new JLabel("+1.00 Mark");
        qMarksLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        qMarksLabel.setForeground(UIConstants.STATUS_ANSWERED);
        qHeader.add(qMarksLabel, BorderLayout.EAST);

        card.add(qHeader, BorderLayout.NORTH);

        // Middle Question Text + Options
        JPanel qBody = new JPanel();
        qBody.setOpaque(false);
        qBody.setLayout(new BoxLayout(qBody, BoxLayout.Y_AXIS));

        // Question Statement
        qTextLabel = new JLabel("Question text will load here...");
        qTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        qTextLabel.setForeground(UIConstants.TEXT_MAIN);
        qTextLabel.setVerticalAlignment(SwingConstants.TOP);
        qTextLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 16, 0));
        qBody.add(qTextLabel);

        // Options List (4 radio options)
        optionButtonGroup = new ButtonGroup();
        String[] prefixes = {"A", "B", "C", "D"};

        for (int i = 0; i < 4; i++) {
            final int optIndex = i;
            JPanel optCard = new JPanel(new BorderLayout(12, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UIConstants.applyQualityRenderingHints(g2);
                    boolean isSelected = optionRadios[optIndex] != null && optionRadios[optIndex].isSelected();
                    g2.setColor(isSelected ? UIConstants.BRAND_PRIMARY_LIGHT : UIConstants.BG_CARD_HOVER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                    g2.setColor(isSelected ? UIConstants.BRAND_PRIMARY : UIConstants.BORDER_LIGHT);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                }
            };
            optCard.setOpaque(false);
            optCard.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            optCard.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            optCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

            JRadioButton radio = new JRadioButton();
            radio.setOpaque(false);
            radio.setFocusPainted(false);
            optionButtonGroup.add(radio);
            optionRadios[i] = radio;

            JLabel optText = new JLabel("(" + prefixes[i] + ") Option text");
            optText.setFont(UIConstants.FONT_BODY);
            optText.setForeground(UIConstants.TEXT_MAIN);
            optionLabels[i] = optText;

            optCard.add(radio, BorderLayout.WEST);
            optCard.add(optText, BorderLayout.CENTER);

            // Clicking card selects radio
            optCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
                    radio.setSelected(true);
                    onOptionSelected(optIndex);
                }
            });

            radio.addActionListener(e -> {
                if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
                onOptionSelected(optIndex);
            });

            optionCards[i] = optCard;
            qBody.add(optCard);
            qBody.add(Box.createVerticalStrut(10));
        }

        card.add(qBody, BorderLayout.CENTER);
        return card;
    }

    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setOpaque(false);

        // Left controls: Previous, Clear Response, Mark for Review
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        prevButton = new ModernButton("← Previous", ModernButton.ButtonVariant.SECONDARY);
        prevButton.setPreferredSize(new Dimension(110, 42));
        prevButton.addActionListener(e -> navigateToPrevious());

        clearResponseButton = new ModernButton("Clear Response", ModernButton.ButtonVariant.OUTLINE);
        clearResponseButton.setPreferredSize(new Dimension(135, 42));
        clearResponseButton.addActionListener(e -> clearCurrentResponse());

        markReviewButton = new ModernButton("Mark for Review & Next", ModernButton.ButtonVariant.PURPLE);
        markReviewButton.setPreferredSize(new Dimension(190, 42));
        markReviewButton.addActionListener(e -> markForReviewAndNext());

        leftControls.add(prevButton);
        leftControls.add(clearResponseButton);
        leftControls.add(markReviewButton);
        navBar.add(leftControls, BorderLayout.WEST);

        // Right controls: Save & Next, Submit Exam
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        nextButton = new ModernButton("Save & Next →", ModernButton.ButtonVariant.PRIMARY);
        nextButton.setPreferredSize(new Dimension(145, 42));
        nextButton.addActionListener(e -> saveAndNext());

        submitButton = new ModernButton("Submit Exam ✔", ModernButton.ButtonVariant.SUCCESS);
        submitButton.setPreferredSize(new Dimension(145, 42));
        submitButton.addActionListener(e -> promptSubmitExam(false));

        rightControls.add(nextButton);
        rightControls.add(submitButton);
        navBar.add(rightControls, BorderLayout.EAST);

        return navBar;
    }

    private JPanel createPaletteSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(310, 500));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Sidebar Header
        JPanel sbHeader = new JPanel(new BorderLayout());
        sbHeader.setOpaque(false);

        JLabel sbTitle = new JLabel("Question Palette");
        sbTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        sbTitle.setForeground(UIConstants.TEXT_MAIN);

        JLabel sbSub = new JLabel("Click any number to jump to question");
        sbSub.setFont(UIConstants.FONT_BODY_SMALL);
        sbSub.setForeground(UIConstants.TEXT_MUTED);

        sbHeader.add(sbTitle, BorderLayout.NORTH);
        sbHeader.add(sbSub, BorderLayout.SOUTH);
        sidebar.add(sbHeader, BorderLayout.NORTH);

        // Question Numbers Grid
        paletteGridPanel = new JPanel(new GridLayout(0, 5, 8, 8));
        paletteGridPanel.setOpaque(false);

        JScrollPane gridScroll = new JScrollPane(paletteGridPanel);
        gridScroll.setBorder(null);
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        sidebar.add(gridScroll, BorderLayout.CENTER);

        // Sidebar Footer: Real-life Legend counters (Answered, Not Answered, Not Visited, Marked)
        JPanel legendSummary = new JPanel(new GridLayout(2, 2, 8, 8));
        legendSummary.setOpaque(false);
        legendSummary.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        countAnsweredLabel = createPaletteCountBadge(UIConstants.STATUS_ANSWERED, "0 Answered");
        countNotAnsweredLabel = createPaletteCountBadge(UIConstants.STATUS_NOT_ANSWERED, "1 Not Answered");
        countNotVisitedLabel = createPaletteCountBadge(UIConstants.STATUS_NOT_VISITED, "14 Not Visited");
        countReviewLabel = createPaletteCountBadge(UIConstants.STATUS_MARKED_REVIEW, "0 Marked");

        legendSummary.add(countAnsweredLabel);
        legendSummary.add(countNotAnsweredLabel);
        legendSummary.add(countNotVisitedLabel);
        legendSummary.add(countReviewLabel);

        sidebar.add(legendSummary, BorderLayout.SOUTH);

        return sidebar;
    }

    private JLabel createPaletteCountBadge(Color dotColor, String text) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(dotColor);
                g2.fillOval(2, (getHeight() - 10) / 2, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        return lbl;
    }

    private void setupQuestionPalette() {
        if (currentSession == null) return;
        paletteGridPanel.removeAll();
        int total = currentSession.getQuestionCount();
        paletteButtons = new QuestionPaletteButton[total];

        for (int i = 0; i < total; i++) {
            final int qIndex = i;
            QuestionPaletteButton btn = new QuestionPaletteButton(qIndex);
            btn.addActionListener(e -> jumpToQuestion(qIndex));
            paletteButtons[i] = btn;
            paletteGridPanel.add(btn);
        }

        updatePaletteUI();
    }

    private void loadQuestion(int index) {
        if (currentSession == null || index < 0 || index >= currentSession.getQuestionCount()) {
            return;
        }

        currentQuestionIndex = index;
        currentSession.visitQuestion(index);

        Question q = currentSession.getQuestions().get(index);
        qNumberLabel.setText("Question " + (index + 1) + " of " + currentSession.getQuestionCount());
        qCategoryLabel.setText(q.getCategory());
        qMarksLabel.setText("+" + String.format("%.2f", q.getMarks()) + " Marks");
        qTextLabel.setText("<html><body style='width: 520px; line-height: 1.4;'>" + q.getQuestionText() + "</body></html>");

        // Clear existing button group selection temporarily
        optionButtonGroup.clearSelection();

        // Populate options
        String[] prefixes = {"A", "B", "C", "D"};
        String[] opts = q.getOptions();
        int selectedAns = currentSession.getSelectedAnswer(index);

        for (int i = 0; i < 4; i++) {
            optionLabels[i].setText("<html><b>(" + prefixes[i] + ")</b> " + opts[i] + "</html>");
            if (i == selectedAns) {
                optionRadios[i].setSelected(true);
            } else {
                optionRadios[i].setSelected(false);
            }
            optionCards[i].repaint();
        }

        // Button state
        prevButton.setEnabled(index > 0);
        if (index == currentSession.getQuestionCount() - 1) {
            nextButton.setText("Save & End →");
        } else {
            nextButton.setText("Save & Next →");
        }

        updatePaletteUI();
    }

    private void onOptionSelected(int optIndex) {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        for (JPanel card : optionCards) {
            card.repaint();
        }
    }

    private void saveAndNext() {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        saveCurrentSelection();
        if (currentQuestionIndex < currentSession.getQuestionCount() - 1) {
            loadQuestion(currentQuestionIndex + 1);
        } else {
            // Last question, ask user if they want to submit
            promptSubmitExam(false);
        }
    }

    private void navigateToPrevious() {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        saveCurrentSelection();
        if (currentQuestionIndex > 0) {
            loadQuestion(currentQuestionIndex - 1);
        }
    }

    private void clearCurrentResponse() {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        optionButtonGroup.clearSelection();
        currentSession.clearAnswer(currentQuestionIndex);
        for (JPanel card : optionCards) {
            card.repaint();
        }
        updatePaletteUI();
    }

    private void markForReviewAndNext() {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        int selected = getCurrentlySelectedOption();
        if (selected >= 0) {
            currentSession.saveAnswer(currentQuestionIndex, selected);
        }
        currentSession.markForReview(currentQuestionIndex);
        updatePaletteUI();

        if (currentQuestionIndex < currentSession.getQuestionCount() - 1) {
            loadQuestion(currentQuestionIndex + 1);
        }
    }

    private void jumpToQuestion(int index) {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        saveCurrentSelection();
        loadQuestion(index);
    }

    private void saveCurrentSelection() {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        int selected = getCurrentlySelectedOption();
        if (selected >= 0) {
            currentSession.saveAnswer(currentQuestionIndex, selected);
        }
        updatePaletteUI();
    }

    private int getCurrentlySelectedOption() {
        for (int i = 0; i < 4; i++) {
            if (optionRadios[i].isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void updatePaletteUI() {
        if (currentSession == null || paletteButtons == null) return;

        int answered = 0;
        int notAnswered = 0;
        int notVisited = 0;
        int marked = 0;

        for (int i = 0; i < currentSession.getQuestionCount(); i++) {
            QuestionStatus status = currentSession.getStatus(i);
            paletteButtons[i].setStatus(status);
            paletteButtons[i].setCurrentQuestion(i == currentQuestionIndex);

            switch (status) {
                case ANSWERED -> answered++;
                case NOT_ANSWERED -> notAnswered++;
                case NOT_VISITED -> notVisited++;
                case MARKED_FOR_REVIEW, ANSWERED_AND_MARKED_FOR_REVIEW -> marked++;
            }
        }

        countAnsweredLabel.setText(answered + " Answered");
        countNotAnsweredLabel.setText(notAnswered + " Not Answered");
        countNotVisitedLabel.setText(notVisited + " Not Visited");
        countReviewLabel.setText(marked + " Review");

        paletteGridPanel.revalidate();
        paletteGridPanel.repaint();
    }

    private void startCountdownTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        countdownTimer = new Timer(1000, e -> {
            if (currentSession == null) return;
            currentSession.decrementTimer();
            updateTimerDisplay();

            if (currentSession.getRemainingSeconds() <= 0) {
                countdownTimer.stop();
                handleAutoSubmitTimeUp();
            }
        });
        countdownTimer.start();
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        if (currentSession == null) return;
        int remaining = currentSession.getRemainingSeconds();
        timerLabel.setText(currentSession.getFormattedRemainingTime());

        if (remaining <= 60) {
            timerLabel.setForeground(UIConstants.TIMER_CRITICAL); // Pulsating Red under 1 min
        } else if (remaining <= 300) {
            timerLabel.setForeground(UIConstants.TIMER_WARNING); // Amber under 5 mins
        } else {
            timerLabel.setForeground(UIConstants.TIMER_NORMAL); // Normal Green
        }
    }

    private void handleAutoSubmitTimeUp() {
        saveCurrentSelection();
        JOptionPane.showMessageDialog(
            this,
            "⏰ Time is UP! Your examination session has ended.\nYour responses are now being auto-submitted.",
            "Time Expired - Auto Submission",
            JOptionPane.WARNING_MESSAGE
        );
        finishSubmission();
    }

    public void promptSubmitExam(boolean isFromWindowClosing) {
        if (isExamSubmitted || currentSession == null || currentSession.isSubmitted()) return;
        saveCurrentSelection();

        int total = currentSession.getQuestionCount();
        int answered = currentSession.getAnsweredCount();
        int notAnswered = currentSession.getUnansweredCount();
        int review = currentSession.getMarkedForReviewCount();

        String summaryHtml = "<html><body style='width: 320px; font-family: Segoe UI;'>"
            + "<h2 style='margin-bottom: 8px;'>Exam Submission Summary</h2>"
            + "<p style='color: #475569;'>Please verify your exam status before final submission:</p>"
            + "<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>"
            + "<tr><td><b>Total Questions:</b></td><td>" + total + "</td></tr>"
            + "<tr><td><b style='color: #16A34A;'>Answered:</b></td><td>" + answered + "</td></tr>"
            + "<tr><td><b style='color: #DC2626;'>Unanswered / Skipped:</b></td><td>" + notAnswered + "</td></tr>"
            + "<tr><td><b style='color: #7C3AED;'>Marked for Review:</b></td><td>" + review + "</td></tr>"
            + "<tr><td><b>Time Remaining:</b></td><td>" + currentSession.getFormattedRemainingTime() + "</td></tr>"
            + "</table>"
            + "<p style='margin-top: 14px; color: #DC2626;'><b>Are you sure you want to finalize and submit your test?</b></p>"
            + "<p style='font-size: 11px; color: #64748B;'>Once submitted, answers are permanently locked and cannot be changed.</p>"
            + "</body></html>";

        int opt = JOptionPane.showConfirmDialog(
            this,
            summaryHtml,
            "Confirm Exam Submission",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opt == JOptionPane.YES_OPTION) {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            finishSubmission();
        }
    }

    private void finishSubmission() {
        isExamSubmitted = true;
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        if (currentSession != null) {
            currentSession.submit();
        }

        // Lock all inputs immediately
        for (JRadioButton r : optionRadios) {
            if (r != null) r.setEnabled(false);
        }
        for (JPanel c : optionCards) {
            if (c != null) c.setCursor(Cursor.getDefaultCursor());
        }
        if (prevButton != null) prevButton.setEnabled(false);
        if (nextButton != null) nextButton.setEnabled(false);
        if (markReviewButton != null) markReviewButton.setEnabled(false);
        if (clearResponseButton != null) clearResponseButton.setEnabled(false);
        if (submitButton != null) submitButton.setEnabled(false);
        if (paletteButtons != null) {
            for (QuestionPaletteButton b : paletteButtons) {
                b.setEnabled(false);
            }
        }

        onExamSubmitted.accept(currentSession);
    }

    public boolean isExamRunning() {
        return countdownTimer != null && countdownTimer.isRunning() && currentSession != null && !currentSession.isSubmitted();
    }

    public void stopTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
    }
}
