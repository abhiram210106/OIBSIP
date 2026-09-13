package com.oasis.exam.ui;

import com.oasis.exam.model.User;
import com.oasis.exam.ui.components.ModernButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Real-world CBT Instructions & Guidelines screen explaining the
 * question palette color codes, timing rules, and candidate declaration.
 */
public class InstructionsPanel extends JPanel {
    private final Consumer<User> onStartExam;
    private final Runnable onBackToProfile;
    private User currentUser;
    private JLabel candidateWelcomeLabel;
    private JCheckBox declarationCheckBox;
    private ModernButton startExamBtn;

    public InstructionsPanel(Consumer<User> onStartExam, Runnable onBackToProfile) {
        this.onStartExam = onStartExam;
        this.onBackToProfile = onBackToProfile;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_APP);
        initUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            candidateWelcomeLabel.setText("Candidate: " + user.getDisplayName() + " | Roll: " + user.getRollNumber());
        }
        declarationCheckBox.setSelected(false);
        startExamBtn.setEnabled(false);
    }

    private void initUI() {
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIConstants.BG_APP);

        JPanel card = new JPanel(new BorderLayout(0, 16)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(840, 620));
        card.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Examination Instructions & Guidelines");
        title.setFont(UIConstants.FONT_HEADER_TITLE);
        title.setForeground(UIConstants.TEXT_MAIN);

        candidateWelcomeLabel = new JLabel("Candidate: Rahul Sharma | Roll: OASIS-2026-041");
        candidateWelcomeLabel.setFont(UIConstants.FONT_BODY_BOLD);
        candidateWelcomeLabel.setForeground(UIConstants.BRAND_PRIMARY);

        header.add(title, BorderLayout.NORTH);
        header.add(Box.createVerticalStrut(4), BorderLayout.CENTER);
        header.add(candidateWelcomeLabel, BorderLayout.SOUTH);

        card.add(header, BorderLayout.NORTH);

        // CONTENT BODY (Scrollable if needed)
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        // Exam Summary Info Bar
        JPanel infoGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        infoGrid.setOpaque(false);
        infoGrid.add(createMiniCard("Total Questions", "15 MCQs"));
        infoGrid.add(createMiniCard("Total Duration", "15 Minutes"));
        infoGrid.add(createMiniCard("Marking Scheme", "+1.00 / 0.00"));
        infoGrid.add(createMiniCard("Pass Standard", "50% (7.5+ pts)"));
        infoGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        body.add(infoGrid);
        body.add(Box.createVerticalStrut(20));

        // Legend Section (TCS iON style)
        JLabel legendHeader = new JLabel("1. Question Palette Color Scheme");
        legendHeader.setFont(UIConstants.FONT_SECTION_TITLE);
        legendHeader.setForeground(UIConstants.TEXT_MAIN);
        body.add(legendHeader);
        body.add(Box.createVerticalStrut(10));

        JPanel legendPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem(UIConstants.STATUS_ANSWERED, "Answered: Question has been answered and recorded."));
        legendPanel.add(createLegendItem(UIConstants.STATUS_NOT_ANSWERED, "Not Answered: You visited the question but have not answered yet."));
        legendPanel.add(createLegendItem(UIConstants.STATUS_NOT_VISITED, "Not Visited: You have not visited the question yet."));
        legendPanel.add(createLegendItem(UIConstants.STATUS_MARKED_REVIEW, "Marked for Review: Marked to be revisited later."));
        legendPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        body.add(legendPanel);
        body.add(Box.createVerticalStrut(20));

        // Guidelines Section
        JLabel rulesHeader = new JLabel("2. Navigation & Timing Rules");
        rulesHeader.setFont(UIConstants.FONT_SECTION_TITLE);
        rulesHeader.setForeground(UIConstants.TEXT_MAIN);
        body.add(rulesHeader);
        body.add(Box.createVerticalStrut(8));

        String[] rules = {
            "• Use 'Next' or 'Save & Next' to confirm your response and advance to subsequent questions.",
            "• Click any numbered button in the Question Palette to jump directly to that question.",
            "• 'Clear Response' will deselect your radio choice for that question.",
            "• The timer in the top header will count down continuously. When it reaches 00:00, the exam will AUTO-SUBMIT.",
            "• Attempting to close the test window will prompt an alert warning. Do not quit accidentally.",
            "• Click 'Submit Exam' when you finish. A confirmation summary of attempted questions will be shown."
        };
        for (String rule : rules) {
            JLabel rLbl = new JLabel(rule);
            rLbl.setFont(UIConstants.FONT_BODY);
            rLbl.setForeground(UIConstants.TEXT_SECONDARY);
            rLbl.setBorder(new EmptyBorder(2, 0, 2, 0));
            body.add(rLbl);
        }

        body.add(Box.createVerticalStrut(20));

        // Declaration checkbox
        declarationCheckBox = new JCheckBox("I have read and understood all instructions. I agree to abide by examination rules.");
        declarationCheckBox.setFont(UIConstants.FONT_BODY_BOLD);
        declarationCheckBox.setForeground(UIConstants.TEXT_MAIN);
        declarationCheckBox.setOpaque(false);
        declarationCheckBox.setFocusPainted(false);
        declarationCheckBox.addActionListener(e -> {
            startExamBtn.setEnabled(declarationCheckBox.isSelected());
        });
        body.add(declarationCheckBox);

        card.add(body, BorderLayout.CENTER);

        // FOOTER ACTIONS
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        ModernButton backBtn = new ModernButton("← Back to Profile", ModernButton.ButtonVariant.OUTLINE);
        backBtn.setPreferredSize(new Dimension(150, 42));
        backBtn.addActionListener(e -> onBackToProfile.run());
        footer.add(backBtn, BorderLayout.WEST);

        startExamBtn = new ModernButton("Start Examination Now 🚀", ModernButton.ButtonVariant.SUCCESS);
        startExamBtn.setPreferredSize(new Dimension(230, 42));
        startExamBtn.setEnabled(false);
        startExamBtn.addActionListener(e -> {
            if (currentUser != null && declarationCheckBox.isSelected()) {
                onStartExam.accept(currentUser);
            }
        });
        footer.add(startExamBtn, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);

        centerWrapper.add(card, new GridBagConstraints());
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createMiniCard(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        JLabel l1 = new JLabel(label.toUpperCase());
        l1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l1.setForeground(UIConstants.TEXT_MUTED);

        JLabel l2 = new JLabel(value);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l2.setForeground(UIConstants.TEXT_MAIN);

        p.add(l1, BorderLayout.NORTH);
        p.add(l2, BorderLayout.CENTER);
        return p;
    }

    private JPanel createLegendItem(Color color, String description) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setOpaque(false);

        JLabel swatch = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        swatch.setPreferredSize(new Dimension(22, 22));
        p.add(swatch);

        JLabel desc = new JLabel(description);
        desc.setFont(UIConstants.FONT_BODY_SMALL);
        desc.setForeground(UIConstants.TEXT_SECONDARY);
        p.add(desc);

        return p;
    }
}
