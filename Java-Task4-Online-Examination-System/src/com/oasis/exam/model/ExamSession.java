package com.oasis.exam.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages the candidate's active exam attempt, responses, timer, and scoring.
 */
public class ExamSession {
    private final User candidate;
    private final List<Question> questions;
    private final int[] selectedAnswers; // -1 if not answered, 0-3 for selected option
    private final QuestionStatus[] statuses;
    private final int totalDurationSeconds;
    private int remainingSeconds;
    private boolean isSubmitted;
    private long startTimeMillis;
    private long submissionTimeMillis;

    public ExamSession(User candidate, List<Question> questions, int totalDurationSeconds) {
        this.candidate = candidate;
        this.questions = new ArrayList<>(questions);
        this.totalDurationSeconds = totalDurationSeconds;
        this.remainingSeconds = totalDurationSeconds;
        this.selectedAnswers = new int[questions.size()];
        this.statuses = new QuestionStatus[questions.size()];
        this.isSubmitted = false;

        Arrays.fill(this.selectedAnswers, -1);
        Arrays.fill(this.statuses, QuestionStatus.NOT_VISITED);

        // Mark the very first question as NOT_ANSWERED when starting
        if (this.statuses.length > 0) {
            this.statuses[0] = QuestionStatus.NOT_ANSWERED;
        }

        this.startTimeMillis = System.currentTimeMillis();
    }

    public synchronized void visitQuestion(int index) {
        if (isSubmitted || index < 0 || index >= questions.size()) return;
        if (statuses[index] == QuestionStatus.NOT_VISITED) {
            statuses[index] = QuestionStatus.NOT_ANSWERED;
        }
    }

    public synchronized void saveAnswer(int index, int optionIndex) {
        if (isSubmitted || index < 0 || index >= questions.size()) return;
        selectedAnswers[index] = optionIndex;
        if (optionIndex >= 0) {
            statuses[index] = QuestionStatus.ANSWERED;
        } else {
            statuses[index] = QuestionStatus.NOT_ANSWERED;
        }
    }

    public synchronized void markForReview(int index) {
        if (isSubmitted || index < 0 || index >= questions.size()) return;
        if (selectedAnswers[index] >= 0) {
            statuses[index] = QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW;
        } else {
            statuses[index] = QuestionStatus.MARKED_FOR_REVIEW;
        }
    }

    public synchronized void clearResponse(int index) {
        if (isSubmitted || index < 0 || index >= questions.size()) return;
        selectedAnswers[index] = -1;
        statuses[index] = QuestionStatus.NOT_ANSWERED;
    }

    public synchronized void clearAnswer(int index) {
        clearResponse(index);
    }

    public synchronized void decrementTimer() {
        if (isSubmitted) return;
        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
    }

    public void submit() {
        if (!isSubmitted) {
            isSubmitted = true;
            submissionTimeMillis = System.currentTimeMillis();
        }
    }

    public double calculateScore() {
        double score = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] >= 0 && questions.get(i).isCorrect(selectedAnswers[i])) {
                score += questions.get(i).getMarks();
            }
        }
        return score;
    }

    public double getMaxScore() {
        double max = 0;
        for (Question q : questions) {
            max += q.getMarks();
        }
        return max;
    }

    public int getCorrectCount() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] >= 0 && questions.get(i).isCorrect(selectedAnswers[i])) {
                count++;
            }
        }
        return count;
    }

    public int getIncorrectCount() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] >= 0 && !questions.get(i).isCorrect(selectedAnswers[i])) {
                count++;
            }
        }
        return count;
    }

    public int getUnansweredCount() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] < 0) {
                count++;
            }
        }
        return count;
    }

    public int getAnsweredCount() {
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers[i] >= 0) {
                count++;
            }
        }
        return count;
    }

    public int getMarkedForReviewCount() {
        int count = 0;
        for (QuestionStatus s : statuses) {
            if (s == QuestionStatus.MARKED_FOR_REVIEW || s == QuestionStatus.ANSWERED_AND_MARKED_FOR_REVIEW) {
                count++;
            }
        }
        return count;
    }

    public double getPercentage() {
        double max = getMaxScore();
        return (max > 0) ? (calculateScore() / max) * 100.0 : 0.0;
    }

    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "A+ (Outstanding)";
        if (pct >= 80) return "A (Excellent)";
        if (pct >= 70) return "B (Very Good)";
        if (pct >= 60) return "C (Good / Pass)";
        if (pct >= 50) return "D (Satisfactory)";
        return "F (Needs Improvement)";
    }

    public boolean isPassed() {
        return getPercentage() >= 50.0;
    }

    public int getTimeSpentSeconds() {
        return Math.max(0, totalDurationSeconds - remainingSeconds);
    }

    public String getFormattedTimeSpent() {
        int spent = getTimeSpentSeconds();
        int minutes = spent / 60;
        int seconds = spent % 60;
        return String.format("%02dm %02ds", minutes, seconds);
    }

    public String getFormattedRemainingTime() {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Getters
    public User getCandidate() {
        return candidate;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public int getSelectedAnswer(int index) {
        if (index < 0 || index >= selectedAnswers.length) return -1;
        return selectedAnswers[index];
    }

    public QuestionStatus getStatus(int index) {
        if (index < 0 || index >= statuses.length) return QuestionStatus.NOT_VISITED;
        return statuses[index];
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public boolean isSubmitted() {
        return isSubmitted;
    }
}
