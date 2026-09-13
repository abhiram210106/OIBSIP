package com.oasis.exam.model;

import java.awt.Color;

/**
 * Represents the status of a question in the CBT examination palette,
 * identical to industry-standard examination systems (TCS iON / NTA / GATE).
 */
public enum QuestionStatus {
    NOT_VISITED("Not Visited", new Color(148, 163, 184), Color.WHITE, "⚪"),
    NOT_ANSWERED("Not Answered", new Color(220, 38, 38), Color.WHITE, "🔴"),
    ANSWERED("Answered", new Color(22, 163, 74), Color.WHITE, "🟢"),
    MARKED_FOR_REVIEW("Marked for Review", new Color(124, 58, 237), Color.WHITE, "🟣"),
    ANSWERED_AND_MARKED_FOR_REVIEW("Answered & Marked for Review", new Color(168, 85, 247), Color.WHITE, "🟣✔");

    private final String displayName;
    private final Color badgeColor;
    private final Color textColor;
    private final String iconSymbol;

    QuestionStatus(String displayName, Color badgeColor, Color textColor, String iconSymbol) {
        this.displayName = displayName;
        this.badgeColor = badgeColor;
        this.textColor = textColor;
        this.iconSymbol = iconSymbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Color getBadgeColor() {
        return badgeColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }
}
