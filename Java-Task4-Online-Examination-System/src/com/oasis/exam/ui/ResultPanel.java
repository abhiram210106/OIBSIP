package com.oasis.exam.ui;

import com.oasis.exam.model.ExamSession;
import com.oasis.exam.model.Question;
import com.oasis.exam.model.User;
import com.oasis.exam.ui.components.ModernButton;
import com.oasis.exam.ui.components.StatCard;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Modern CBT Result & Performance Analytics Screen.
 * Displays total score (X out of Y), percentage, grade, time taken,
 * visual breakdown cards, and a filterable question-by-question review with explanations.
 */
public class ResultPanel extends JPanel {
    private final Runnable onLogout;
    private final Runnable onRetakeExam;

    private ExamSession session;
    private JLabel candidateHeaderLabel;
    private JLabel scoreBannerLabel;
    private JLabel gradeBadgeLabel;

    // Stat Cards
    private StatCard scoreCard;
    private StatCard percentageCard;
    private StatCard timeCard;
    private StatCard correctCard;
    private StatCard incorrectCard;
    private StatCard unattemptedCard;

    // Review Panel
    private JPanel reviewItemsContainer;
    private String currentFilter = "ALL";

    public ResultPanel(Runnable onLogout, Runnable onRetakeExam) {
        this.onLogout = onLogout;
        this.onRetakeExam = onRetakeExam;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_APP);
        initUI();
    }

    public void showResult(ExamSession session) {
        this.session = session;
        if (session == null) return;

        User user = session.getCandidate();
        candidateHeaderLabel.setText("Candidate: " + user.getDisplayName() + " | Roll No: " + user.getRollNumber());

        double score = session.calculateScore();
        double maxScore = session.getMaxScore();
        double pct = session.getPercentage();
        boolean passed = session.isPassed();

        scoreBannerLabel.setText(String.format("Final Score: %.1f / %.1f (%s)", score, maxScore, passed ? "PASSED" : "FAILED"));
        gradeBadgeLabel.setText("Grade: " + session.getGrade());

        // Update Stat Cards
        scoreCard.updateData(String.format("%.1f / %.1f", score, maxScore), "Points Awarded");
        percentageCard.updateData(String.format("%.1f%%", pct), passed ? "Passing Cutoff: 50%" : "Below Passing Cutoff");
        timeCard.updateData(session.getFormattedTimeSpent(), "Out of " + (session.getTotalDurationSeconds() / 60) + " mins");
        correctCard.updateData(String.valueOf(session.getCorrectCount()), "Accurate answers");
        incorrectCard.updateData(String.valueOf(session.getIncorrectCount()), "Mistakes made");
        unattemptedCard.updateData(String.valueOf(session.getUnansweredCount()), "Skipped / Blank");

        // Populate question review
        currentFilter = "ALL";
        renderReviewList();
    }

    private void initUI() {
        // TOP HERO BANNER
        JPanel hero = createHeroBanner();
        add(hero, BorderLayout.NORTH);

        // CENTER BODY (Stats + Detailed Review)
        JPanel centerBody = new JPanel(new BorderLayout(0, 16));
        centerBody.setOpaque(false);
        centerBody.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        // STATS CARDS ROW
        JPanel statsRow = new JPanel(new GridLayout(1, 6, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(800, 100));

        scoreCard = new StatCard("Score", "0 / 0", "Points", UIConstants.BRAND_PRIMARY);
        percentageCard = new StatCard("Percentage", "0%", "Score", UIConstants.BRAND_ACCENT);
        timeCard = new StatCard("Time Taken", "00m 00s", "Duration", UIConstants.TEXT_SECONDARY);
        correctCard = new StatCard("Correct", "0", "Right", UIConstants.STATUS_ANSWERED);
        incorrectCard = new StatCard("Incorrect", "0", "Wrong", UIConstants.STATUS_NOT_ANSWERED);
        unattemptedCard = new StatCard("Unanswered", "0", "Skipped", UIConstants.STATUS_NOT_VISITED);

        statsRow.add(scoreCard);
        statsRow.add(percentageCard);
        statsRow.add(timeCard);
        statsRow.add(correctCard);
        statsRow.add(incorrectCard);
        statsRow.add(unattemptedCard);

        centerBody.add(statsRow, BorderLayout.NORTH);

        // REVIEW SECTION
        JPanel reviewSection = createReviewSection();
        centerBody.add(reviewSection, BorderLayout.CENTER);

        add(centerBody, BorderLayout.CENTER);

        // BOTTOM ACTION BAR
        JPanel bottomBar = createBottomBar();
        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createHeroBanner() {
        JPanel hero = new JPanel(new BorderLayout(15, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        hero.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Official Scorecard & Performance Analysis");
        title.setFont(UIConstants.FONT_HEADER_TITLE);
        title.setForeground(Color.WHITE);

        candidateHeaderLabel = new JLabel("Candidate: Rahul Sharma | Roll No: OASIS-2026-041");
        candidateHeaderLabel.setFont(UIConstants.FONT_BODY_SMALL);
        candidateHeaderLabel.setForeground(new Color(203, 213, 225));

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(candidateHeaderLabel);
        hero.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        scoreBannerLabel = new JLabel("Final Score: 0 / 0 (PASSED)");
        scoreBannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreBannerLabel.setForeground(UIConstants.TIMER_NORMAL);

        gradeBadgeLabel = new JLabel("Grade: A+ (Outstanding)");
        gradeBadgeLabel.setFont(UIConstants.FONT_BODY_SMALL);
        gradeBadgeLabel.setForeground(new Color(226, 232, 240));

        right.add(scoreBannerLabel);
        right.add(Box.createVerticalStrut(4));
        right.add(gradeBadgeLabel);
        hero.add(right, BorderLayout.EAST);

        return hero;
    }

    private JPanel createReviewSection() {
        JPanel container = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Review Header + Filter Tabs
        JPanel rHeader = new JPanel(new BorderLayout());
        rHeader.setOpaque(false);

        JLabel rTitle = new JLabel("Question-by-Question Solution & Analysis");
        rTitle.setFont(UIConstants.FONT_SECTION_TITLE);
        rTitle.setForeground(UIConstants.TEXT_MAIN);
        rHeader.add(rTitle, BorderLayout.WEST);

        // Filter Buttons Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterBar.setOpaque(false);

        ButtonGroup bg = new ButtonGroup();
        filterBar.add(createFilterRadioButton("All Questions", "ALL", true, bg));
        filterBar.add(createFilterRadioButton("Incorrect Only", "INCORRECT", false, bg));
        filterBar.add(createFilterRadioButton("Correct Only", "CORRECT", false, bg));
        filterBar.add(createFilterRadioButton("Unanswered Only", "UNANSWERED", false, bg));

        rHeader.add(filterBar, BorderLayout.EAST);
        container.add(rHeader, BorderLayout.NORTH);

        // Review items list inside scroll pane
        reviewItemsContainer = new JPanel();
        reviewItemsContainer.setOpaque(false);
        reviewItemsContainer.setLayout(new BoxLayout(reviewItemsContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(reviewItemsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        container.add(scroll, BorderLayout.CENTER);
        return container;
    }

    private JRadioButton createFilterRadioButton(String label, String filterKey, boolean selected, ButtonGroup group) {
        JRadioButton rb = new JRadioButton(label, selected);
        rb.setFont(UIConstants.FONT_BODY_SMALL);
        rb.setForeground(UIConstants.TEXT_SECONDARY);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        group.add(rb);
        rb.addActionListener(e -> {
            this.currentFilter = filterKey;
            renderReviewList();
        });
        return rb;
    }

    private void renderReviewList() {
        if (session == null || reviewItemsContainer == null) return;
        reviewItemsContainer.removeAll();

        List<Question> questions = session.getQuestions();
        String[] prefixes = {"A", "B", "C", "D"};

        int renderedCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int chosen = session.getSelectedAnswer(i);
            boolean isAnswered = chosen >= 0;
            boolean isCorrect = isAnswered && q.isCorrect(chosen);

            // Filter logic
            if ("CORRECT".equals(currentFilter) && !isCorrect) continue;
            if ("INCORRECT".equals(currentFilter) && (!isAnswered || isCorrect)) continue;
            if ("UNANSWERED".equals(currentFilter) && isAnswered) continue;

            renderedCount++;
            JPanel itemCard = createReviewItemCard(i + 1, q, chosen, isCorrect, prefixes);
            reviewItemsContainer.add(itemCard);
            reviewItemsContainer.add(Box.createVerticalStrut(12));
        }

        if (renderedCount == 0) {
            JLabel emptyLabel = new JLabel("No questions match the selected filter.", SwingConstants.CENTER);
            emptyLabel.setFont(UIConstants.FONT_BODY);
            emptyLabel.setForeground(UIConstants.TEXT_MUTED);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
            reviewItemsContainer.add(emptyLabel);
        }

        reviewItemsContainer.revalidate();
        reviewItemsContainer.repaint();
    }

    private JPanel createReviewItemCard(int qNum, Question q, int chosenIndex, boolean isCorrect, String[] prefixes) {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        // Question header
        JPanel qHead = new JPanel(new BorderLayout());
        qHead.setOpaque(false);

        JLabel numLbl = new JLabel("Question " + qNum + " • " + q.getCategory());
        numLbl.setFont(UIConstants.FONT_BODY_BOLD);
        numLbl.setForeground(UIConstants.TEXT_MAIN);

        JLabel statusBadge;
        if (chosenIndex < 0) {
            statusBadge = new JLabel("⚪ Not Attempted (0.00 Marks)");
            statusBadge.setForeground(UIConstants.STATUS_NOT_VISITED);
        } else if (isCorrect) {
            statusBadge = new JLabel("✔ Correct (+1.00 Mark)");
            statusBadge.setForeground(UIConstants.STATUS_ANSWERED);
        } else {
            statusBadge = new JLabel("✖ Incorrect (0.00 Marks)");
            statusBadge.setForeground(UIConstants.STATUS_NOT_ANSWERED);
        }
        statusBadge.setFont(UIConstants.FONT_SMALL_BOLD);

        qHead.add(numLbl, BorderLayout.WEST);
        qHead.add(statusBadge, BorderLayout.EAST);
        card.add(qHead, BorderLayout.NORTH);

        // Question Statement
        JLabel qText = new JLabel("<html><body style='width: 650px; line-height: 1.3;'><b>" + q.getQuestionText() + "</b></body></html>");
        qText.setFont(UIConstants.FONT_BODY);
        qText.setForeground(UIConstants.TEXT_MAIN);

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(qText);
        details.add(Box.createVerticalStrut(10));

        // 4 Official Read-Only Option Rows
        String[] opts = q.getOptions();
        int correctIndex = q.getCorrectOptionIndex();

        for (int j = 0; j < 4; j++) {
            final boolean isChosen = (j == chosenIndex);
            final boolean isRight = (j == correctIndex);

            Color optBg = new Color(248, 250, 252);
            Color optBorder = UIConstants.BORDER_LIGHT;
            String tag = "";
            Color tagColor = UIConstants.TEXT_SECONDARY;

            if (isChosen && isRight) {
                optBg = new Color(220, 252, 231); // light green
                optBorder = new Color(34, 197, 94);
                tag = "  ✔ [Your Answer • Correct]";
                tagColor = new Color(22, 101, 52);
            } else if (isChosen && !isRight) {
                optBg = new Color(254, 226, 226); // light red
                optBorder = new Color(239, 68, 68);
                tag = "  ✖ [Your Answer • Incorrect]";
                tagColor = new Color(153, 27, 27);
            } else if (!isChosen && isRight) {
                optBg = new Color(240, 253, 244);
                optBorder = new Color(34, 197, 94);
                tag = "  ✔ [Correct Answer]";
                tagColor = new Color(22, 101, 52);
            }

            final Color fBg = optBg;
            final Color fBorder = optBorder;

            JPanel optRow = new JPanel(new BorderLayout(8, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    UIConstants.applyQualityRenderingHints(g2);
                    g2.setColor(fBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(fBorder);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                    g2.dispose();
                }
            };
            optRow.setOpaque(false);
            optRow.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            optRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            JLabel optLabel = new JLabel("<html><b>(" + prefixes[j] + ")</b> " + opts[j] + "<span style='font-size:11px; color:"
                + String.format("#%02x%02x%02x", tagColor.getRed(), tagColor.getGreen(), tagColor.getBlue())
                + ";'><b>" + tag + "</b></span></html>");
            optLabel.setFont(UIConstants.FONT_BODY_SMALL);
            optLabel.setForeground(UIConstants.TEXT_MAIN);

            optRow.add(optLabel, BorderLayout.CENTER);
            details.add(optRow);
            details.add(Box.createVerticalStrut(5));
        }

        // Explanation card
        JPanel explCard = new JPanel(new BorderLayout(8, 0));
        explCard.setOpaque(true);
        explCard.setBackground(new Color(241, 245, 249));
        explCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel expl = new JLabel("<html><body style='width: 630px; line-height: 1.3;'><b style='color:#1E40AF;'>💡 Explanation:</b> " + q.getExplanation() + "</body></html>");
        expl.setFont(UIConstants.FONT_BODY_SMALL);
        expl.setForeground(new Color(30, 41, 59));
        explCard.add(expl, BorderLayout.CENTER);

        details.add(Box.createVerticalStrut(6));
        details.add(explCard);

        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 24, 16, 24));

        ModernButton logoutBtn = new ModernButton("🚪 Logout & Exit", ModernButton.ButtonVariant.DANGER);
        logoutBtn.setPreferredSize(new Dimension(140, 42));
        logoutBtn.addActionListener(e -> onLogout.run());
        bar.add(logoutBtn, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        ModernButton exportBtn = new ModernButton("📄 Save Scorecard Report", ModernButton.ButtonVariant.OUTLINE);
        exportBtn.setPreferredSize(new Dimension(200, 42));
        exportBtn.addActionListener(e -> exportReport());

        ModernButton retakeBtn = new ModernButton("🔄 Retake Examination", ModernButton.ButtonVariant.PRIMARY);
        retakeBtn.setPreferredSize(new Dimension(180, 42));
        retakeBtn.addActionListener(e -> onRetakeExam.run());

        rightActions.add(exportBtn);
        rightActions.add(retakeBtn);
        bar.add(rightActions, BorderLayout.EAST);

        return bar;
    }

    private void exportReport() {
        if (session == null) return;
        User u = session.getCandidate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String filename = "Exam_Scorecard_" + u.getUsername() + "_" + sdf.format(new Date()) + ".txt";

        File file = new File(System.getProperty("user.home") + "/Desktop", filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("=========================================================\n");
            writer.write("          OASIS CBT ONLINE EXAMINATION SCORECARD         \n");
            writer.write("=========================================================\n");
            writer.write("Candidate Name : " + u.getDisplayName() + "\n");
            writer.write("Roll Number    : " + u.getRollNumber() + "\n");
            writer.write("Username       : " + u.getUsername() + "\n");
            writer.write("Exam Date      : " + new Date() + "\n");
            writer.write("Subject        : Java Professional Certification Exam\n");
            writer.write("---------------------------------------------------------\n");
            writer.write(String.format("Final Score    : %.1f / %.1f (%.1f%%)\n", session.calculateScore(), session.getMaxScore(), session.getPercentage()));
            writer.write("Result Status  : " + (session.isPassed() ? "PASSED (PASS CUTOFF: 50%)" : "FAILED") + "\n");
            writer.write("Grade Awarded  : " + session.getGrade() + "\n");
            writer.write("Time Spent     : " + session.getFormattedTimeSpent() + "\n");
            writer.write("Total Questions: " + session.getQuestionCount() + "\n");
            writer.write("Correct Count  : " + session.getCorrectCount() + "\n");
            writer.write("Incorrect Count: " + session.getIncorrectCount() + "\n");
            writer.write("Unanswered     : " + session.getUnansweredCount() + "\n");
            writer.write("=========================================================\n\n");
            writer.write("DETAILED QUESTION-BY-QUESTION BREAKDOWN:\n\n");

            List<Question> qs = session.getQuestions();
            String[] p = {"A", "B", "C", "D"};
            for (int i = 0; i < qs.size(); i++) {
                Question q = qs.get(i);
                int ans = session.getSelectedAnswer(i);
                boolean ok = ans >= 0 && q.isCorrect(ans);
                writer.write(String.format("Q%02d [%s] - %s\n", i + 1, q.getCategory(), ok ? "CORRECT" : (ans < 0 ? "SKIPPED" : "INCORRECT")));
                writer.write("Question : " + q.getQuestionText() + "\n");
                writer.write("Your Ans : " + (ans >= 0 ? "(" + p[ans] + ") " + q.getOptions()[ans] : "Not Attempted") + "\n");
                writer.write("Correct  : (" + p[q.getCorrectOptionIndex()] + ") " + q.getOptions()[q.getCorrectOptionIndex()] + "\n");
                writer.write("Reason   : " + q.getExplanation() + "\n");
                writer.write("---------------------------------------------------------\n");
            }
            JOptionPane.showMessageDialog(this, "Scorecard successfully exported to:\n" + file.getAbsolutePath(), "Scorecard Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving scorecard: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
