package com.oasis.exam.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Design system tokens, color palettes, typography, and rendering helpers.
 */
public class UIConstants {
    // Brand & Theme Colors
    public static final Color BRAND_PRIMARY = new Color(37, 99, 235);      // #2563EB Royal Blue
    public static final Color BRAND_PRIMARY_DARK = new Color(30, 64, 175); // #1E40AF
    public static final Color BRAND_PRIMARY_LIGHT = new Color(219, 234, 254); // #DBEAFE
    public static final Color BRAND_ACCENT = new Color(14, 165, 233);      // #0EA5E9 Sky Blue

    // Background & Surfaces
    public static final Color BG_APP = new Color(241, 245, 249);           // #F1F5F9 Light Slate
    public static final Color BG_CARD = Color.WHITE;
    public static final Color BG_CARD_HOVER = new Color(248, 250, 252);
    public static final Color BG_HEADER = new Color(15, 23, 42);           // #0F172A Dark Slate
    public static final Color BG_HEADER_BAR = new Color(30, 41, 59);       // #1E293B
    public static final Color BG_SIDEBAR = new Color(248, 250, 252);

    // Text Colors
    public static final Color TEXT_MAIN = new Color(15, 23, 42);           // #0F172A
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);     // #475569
    public static final Color TEXT_MUTED = new Color(148, 163, 184);       // #94A3B8
    public static final Color TEXT_WHITE = Color.WHITE;

    // Borders & Dividers
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);     // #E2E8F0
    public static final Color BORDER_ACTIVE = new Color(59, 130, 246);

    // CBT Question Palette Status Colors (TCS iON / NTA standard)
    public static final Color STATUS_ANSWERED = new Color(22, 163, 74);       // Emerald Green
    public static final Color STATUS_NOT_ANSWERED = new Color(220, 38, 38);   // Crimson Red
    public static final Color STATUS_NOT_VISITED = new Color(148, 163, 184);  // Slate Neutral
    public static final Color STATUS_MARKED_REVIEW = new Color(124, 58, 237); // Purple/Violet
    public static final Color STATUS_ANSWERED_REVIEW = new Color(168, 85, 247); // Light Purple

    // Alert & Timer Colors
    public static final Color TIMER_NORMAL = new Color(16, 185, 129);      // Green
    public static final Color TIMER_WARNING = new Color(245, 158, 11);     // Amber (< 5 min)
    public static final Color TIMER_CRITICAL = new Color(239, 68, 68);     // Pulsating Red (< 1 min)

    // Typography
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_TIMER = new Font("Segoe UI", Font.BOLD, 22);

    /**
     * Applies high quality antialiasing rendering hints to graphics context.
     */
    public static void applyQualityRenderingHints(Graphics g) {
        if (g instanceof Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }
}
