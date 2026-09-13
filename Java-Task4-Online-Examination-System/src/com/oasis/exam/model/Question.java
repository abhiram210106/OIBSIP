package com.oasis.exam.model;

/**
 * Represents a single multiple-choice question in the examination.
 */
public class Question {
    private final int id;
    private final String questionText;
    private final String[] options;
    private final int correctOptionIndex; // 0 to 3
    private final String explanation;
    private final String category;
    private final double marks;

    public Question(int id, String questionText, String[] options, int correctOptionIndex, String explanation, String category, double marks) {
        this.id = id;
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.explanation = explanation;
        this.category = category;
        this.marks = marks;
    }

    public int getId() {
        return id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getCategory() {
        return category;
    }

    public double getMarks() {
        return marks;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctOptionIndex;
    }

    public String getCorrectOptionLetter() {
        return switch (correctOptionIndex) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "C";
            case 3 -> "D";
            default -> "?";
        };
    }
}
