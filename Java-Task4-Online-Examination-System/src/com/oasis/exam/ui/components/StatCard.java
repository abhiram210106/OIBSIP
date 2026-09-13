package com.oasis.exam.ui.components;

import com.oasis.exam.ui.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Metric card displaying exam KPIs with clean modern aesthetics.
 */
public class StatCard extends JPanel {
    private final JLabel valueLabel;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(8, 6));
        setBackground(UIConstants.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(170, 105));
        setMinimumSize(new Dimension(150, 95));

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        titleLabel.setForeground(UIConstants.TEXT_SECONDARY);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accentColor != null ? accentColor : UIConstants.TEXT_MAIN);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UIConstants.FONT_BODY_SMALL);
        subtitleLabel.setForeground(UIConstants.TEXT_MUTED);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BorderLayout(0, 4));
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);
        content.add(subtitleLabel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
    }

    public void updateData(String value, String subtitle) {
        valueLabel.setText(value);
        if (subtitle != null) {
            subtitleLabel.setText(subtitle);
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyQualityRenderingHints(g2);

        // Rounded white card background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        // Subtle border
        g2.setColor(UIConstants.BORDER_LIGHT);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        // Accent top bar
        if (accentColor != null) {
            g2.setColor(accentColor);
            g2.fillRoundRect(0, 0, getWidth(), 4, 16, 16);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
