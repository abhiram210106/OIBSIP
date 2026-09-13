package com.oasis.exam.ui.components;

import com.oasis.exam.model.QuestionStatus;
import com.oasis.exam.ui.UIConstants;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JButton;

/**
 * Palette button displaying a question number with its real-time CBT status color.
 */
public class QuestionPaletteButton extends JButton {
    private final int questionIndex;
    private QuestionStatus status = QuestionStatus.NOT_VISITED;
    private boolean isCurrentQuestion = false;

    public QuestionPaletteButton(int questionIndex) {
        super(String.valueOf(questionIndex + 1));
        this.questionIndex = questionIndex;

        setPreferredSize(new Dimension(44, 44));
        setMinimumSize(new Dimension(44, 44));
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Question " + (questionIndex + 1) + " (" + status.getDisplayName() + ")");
    }

    public int getQuestionIndex() {
        return questionIndex;
    }

    public void setStatus(QuestionStatus status) {
        this.status = status;
        setToolTipText("Question " + (questionIndex + 1) + " - " + status.getDisplayName());
        repaint();
    }

    public void setCurrentQuestion(boolean current) {
        this.isCurrentQuestion = current;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyQualityRenderingHints(g2);

        int size = Math.min(getWidth(), getHeight()) - 6;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        // Background color based on QuestionStatus
        Color bg = status.getBadgeColor();
        g2.setColor(bg);
        g2.fillRoundRect(x, y, size, size, 12, 12);

        // If answered and marked for review, draw a small green indicator badge
        if (status == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW) {
            g2.setColor(UIConstants.STATUS_ANSWERED);
            g2.fillOval(x + size - 10, y + 2, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawOval(x + size - 10, y + 2, 8, 8);
        }

        // Active question highlight border
        if (isCurrentQuestion) {
            g2.setColor(new Color(15, 23, 42)); // Dark ring
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(x - 2, y - 2, size + 4, size + 4, 14, 14);
        }

        // Text
        g2.setColor(status.getTextColor());
        g2.setFont(getFont());
        String text = getText();
        int textWidth = g2.getFontMetrics().stringWidth(text);
        int textHeight = g2.getFontMetrics().getAscent();
        int tx = (getWidth() - textWidth) / 2;
        int ty = (getHeight() + textHeight) / 2 - 2;
        g2.drawString(text, tx, ty);

        g2.dispose();
    }
}
