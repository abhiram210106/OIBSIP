package com.oasis.exam.ui.components;

import com.oasis.exam.ui.UIConstants;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Modern styled button with rounded corners, subtle hover elevation, and custom variants.
 */
public class ModernButton extends JButton {
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor = Color.WHITE;
    private int cornerRadius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isOutlined = false;
    private Color outlineColor = Color.GRAY;

    public enum ButtonVariant {
        PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, PURPLE, OUTLINE
    }

    public ModernButton(String text, ButtonVariant variant) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(UIConstants.FONT_BODY_BOLD);

        applyVariant(variant);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = false;
                    repaint();
                }
            }
        });
    }

    public void applyVariant(ButtonVariant variant) {
        switch (variant) {
            case PRIMARY -> {
                normalColor = UIConstants.BRAND_PRIMARY;
                hoverColor = UIConstants.BRAND_PRIMARY_DARK;
                pressedColor = new Color(23, 37, 84);
                textColor = Color.WHITE;
                isOutlined = false;
            }
            case SECONDARY -> {
                normalColor = new Color(226, 232, 240);
                hoverColor = new Color(203, 213, 225);
                pressedColor = new Color(148, 163, 184);
                textColor = UIConstants.TEXT_MAIN;
                isOutlined = false;
            }
            case SUCCESS -> {
                normalColor = UIConstants.STATUS_ANSWERED;
                hoverColor = new Color(21, 128, 61);
                pressedColor = new Color(20, 83, 45);
                textColor = Color.WHITE;
                isOutlined = false;
            }
            case DANGER -> {
                normalColor = UIConstants.STATUS_NOT_ANSWERED;
                hoverColor = new Color(185, 28, 28);
                pressedColor = new Color(127, 29, 29);
                textColor = Color.WHITE;
                isOutlined = false;
            }
            case WARNING -> {
                normalColor = UIConstants.TIMER_WARNING;
                hoverColor = new Color(217, 119, 6);
                pressedColor = new Color(180, 83, 9);
                textColor = Color.WHITE;
                isOutlined = false;
            }
            case PURPLE -> {
                normalColor = UIConstants.STATUS_MARKED_REVIEW;
                hoverColor = new Color(109, 40, 217);
                pressedColor = new Color(91, 33, 182);
                textColor = Color.WHITE;
                isOutlined = false;
            }
            case OUTLINE -> {
                normalColor = Color.WHITE;
                hoverColor = new Color(241, 245, 249);
                pressedColor = new Color(226, 232, 240);
                textColor = UIConstants.TEXT_MAIN;
                isOutlined = true;
                outlineColor = UIConstants.BORDER_LIGHT;
            }
        }
        setForeground(textColor);
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        UIConstants.applyQualityRenderingHints(g2);

        Color bg;
        if (!isEnabled()) {
            bg = new Color(203, 213, 225);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
            return;
        }

        if (isPressed) {
            bg = pressedColor;
        } else if (isHovered) {
            bg = hoverColor;
        } else {
            bg = normalColor;
        }

        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        if (isOutlined) {
            g2.setColor(outlineColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
