package com.oasis.exam.ui;

import com.oasis.exam.model.ExamSession;
import com.oasis.exam.model.Question;
import com.oasis.exam.model.User;
import com.oasis.exam.service.AuthService;
import com.oasis.exam.service.QuestionBankService;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main application window managing screen transitions via CardLayout
 * and handling session lifecycle & window close interception.
 */
public class MainFrame extends JFrame {
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_PROFILE = "PROFILE";
    public static final String CARD_INSTRUCTIONS = "INSTRUCTIONS";
    public static final String CARD_EXAM = "EXAM";
    public static final String CARD_RESULT = "RESULT";

    private final CardLayout cardLayout;
    private final JPanel cardsContainer;

    private final AuthService authService;
    private User currentUser;
    private ExamSession currentSession;

    private LoginPanel loginPanel;
    private ProfilePanel profilePanel;
    private InstructionsPanel instructionsPanel;
    private ExamPanel examPanel;
    private ResultPanel resultPanel;

    private String activeCardName = CARD_LOGIN;

    public MainFrame() {
        super("OASIS CBT • Online Examination System (Java Swing)");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1180, 780);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardsContainer = new JPanel(cardLayout);

        initCards();
        initWindowClosingListener();

        add(cardsContainer);
        showCard(CARD_LOGIN);
    }

    private void initCards() {
        // 1. LOGIN SCREEN
        loginPanel = new LoginPanel(authService, user -> {
            this.currentUser = user;
            profilePanel.setUser(user);
            showCard(CARD_PROFILE);
        });

        // 2. PROFILE SCREEN
        profilePanel = new ProfilePanel(
            authService,
            user -> {
                this.currentUser = user;
                instructionsPanel.setUser(user);
                showCard(CARD_INSTRUCTIONS);
            },
            this::handleLogout
        );

        // 3. INSTRUCTIONS SCREEN
        instructionsPanel = new InstructionsPanel(
            user -> startNewExamSession(user),
            () -> showCard(CARD_PROFILE)
        );

        // 4. EXAM SCREEN
        examPanel = new ExamPanel(session -> {
            this.currentSession = session;
            resultPanel.showResult(session);
            showCard(CARD_RESULT);
        });

        // 5. RESULT SCREEN
        resultPanel = new ResultPanel(
            this::handleLogout,
            () -> {
                if (currentUser != null) {
                    instructionsPanel.setUser(currentUser);
                    showCard(CARD_INSTRUCTIONS);
                } else {
                    showCard(CARD_LOGIN);
                }
            }
        );

        cardsContainer.add(loginPanel, CARD_LOGIN);
        cardsContainer.add(profilePanel, CARD_PROFILE);
        cardsContainer.add(instructionsPanel, CARD_INSTRUCTIONS);
        cardsContainer.add(examPanel, CARD_EXAM);
        cardsContainer.add(resultPanel, CARD_RESULT);
    }

    private void initWindowClosingListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private void handleWindowClosing() {
        if (CARD_EXAM.equals(activeCardName) && examPanel.isExamRunning()) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "⚠ An active examination is currently in progress!\n"
                + "If you close now, your answers will not be scored.\n\n"
                + "Are you sure you want to abandon and exit the examination?",
                "Active Exam Session Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                examPanel.stopTimer();
                dispose();
                System.exit(0);
            }
        } else {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the Online Examination System?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
        }
    }

    private void startNewExamSession(User user) {
        List<Question> questions = QuestionBankService.getCertificationQuestions();
        // 15 questions, 15 minutes (900 seconds)
        int durationSeconds = 15 * 60;
        this.currentSession = new ExamSession(user, questions, durationSeconds);

        examPanel.startExam(currentSession);
        showCard(CARD_EXAM);
    }

    private void handleLogout() {
        if (examPanel != null) {
            examPanel.stopTimer();
        }
        this.currentUser = null;
        this.currentSession = null;
        loginPanel.resetFields();
        showCard(CARD_LOGIN);
    }

    public void showCard(String cardName) {
        if (CARD_EXAM.equals(cardName) && (currentSession == null || currentSession.isSubmitted())) {
            return;
        }
        this.activeCardName = cardName;
        cardLayout.show(cardsContainer, cardName);
    }
}
