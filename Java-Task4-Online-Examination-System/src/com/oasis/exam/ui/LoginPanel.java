package com.oasis.exam.ui;

import com.oasis.exam.model.User;
import com.oasis.exam.service.AuthService;
import com.oasis.exam.ui.components.ModernButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Modern CBT Candidate Login Screen with quick credentials,
 * registration, and branded side panel.
 */
public class LoginPanel extends JPanel {
    private final AuthService authService;
    private final Consumer<User> onLoginSuccess;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginPanel(AuthService authService, Consumer<User> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_APP);

        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(UIConstants.BG_APP);

        // Main Login Card (Split: Left Brand Banner + Right Form)
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(UIConstants.BORDER_LIGHT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(860, 520));

        // LEFT BANNER
        JPanel leftBanner = createLeftBanner();
        card.add(leftBanner, BorderLayout.WEST);

        // RIGHT FORM
        JPanel rightForm = createLoginForm();
        card.add(rightForm, BorderLayout.CENTER);

        container.add(card, new GridBagConstraints());
        add(container, BorderLayout.CENTER);
    }

    private JPanel createLeftBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                // Gradient dark slate background
                g2.setColor(UIConstants.BG_HEADER);
                g2.fillRoundRect(0, 0, getWidth() + 20, getHeight(), 24, 24);
                g2.setColor(UIConstants.BG_HEADER);
                g2.fillRect(getWidth() - 20, 0, 20, getHeight()); // square right edge
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(360, 520));
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(45, 35, 40, 35));

        JLabel badge = new JLabel("COMPUTER BASED TEST");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(UIConstants.BRAND_ACCENT);

        JLabel title = new JLabel("<html>Online Examination<br>Portal</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("<html>Standardized assessment engine with live countdown, real-time question palette, and instant score verification.</html>");
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(new Color(203, 213, 225));

        banner.add(badge);
        banner.add(Box.createVerticalStrut(12));
        banner.add(title);
        banner.add(Box.createVerticalStrut(16));
        banner.add(subtitle);
        banner.add(Box.createVerticalGlue());

        // Quick feature bullet points
        String[] features = {
            "✔ Real-time countdown timer & auto-submission",
            "✔ TCS iON / NTA style question palette",
            "✔ Profile management & password updates",
            "✔ Instant detailed scorecard with explanations"
        };
        for (String feat : features) {
            JLabel lbl = new JLabel(feat);
            lbl.setFont(UIConstants.FONT_BODY_SMALL);
            lbl.setForeground(new Color(226, 232, 240));
            banner.add(lbl);
            banner.add(Box.createVerticalStrut(8));
        }

        banner.add(Box.createVerticalStrut(15));
        JLabel versionLbl = new JLabel("OASIS SIP Task 4 • Java Swing CBT v2.0");
        versionLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLbl.setForeground(UIConstants.TEXT_MUTED);
        banner.add(versionLbl);

        return banner;
    }

    private JPanel createLoginForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(40, 45, 40, 45));

        JLabel headerTitle = new JLabel("Candidate Login");
        headerTitle.setFont(UIConstants.FONT_HEADER_TITLE);
        headerTitle.setForeground(UIConstants.TEXT_MAIN);

        JLabel headerSub = new JLabel("Enter your exam roll credentials to continue");
        headerSub.setFont(UIConstants.FONT_BODY_SMALL);
        headerSub.setForeground(UIConstants.TEXT_MUTED);

        form.add(headerTitle);
        form.add(Box.createVerticalStrut(4));
        form.add(headerSub);
        form.add(Box.createVerticalStrut(22));

        // Error message label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(UIConstants.FONT_BODY_SMALL);
        errorLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(6));

        // Username Field
        JLabel userLabel = new JLabel("Username / Roll No");
        userLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        userLabel.setForeground(UIConstants.TEXT_SECONDARY);

        usernameField = new JTextField("student1");
        usernameField.setFont(UIConstants.FONT_BODY);
        usernameField.setPreferredSize(new Dimension(380, 40));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));

        // Password Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        passLabel.setForeground(UIConstants.TEXT_SECONDARY);

        passwordField = new JPasswordField("pass123");
        passwordField.setFont(UIConstants.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(380, 40));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));

        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));

        // Show Password Checkbox
        JCheckBox showPassCheck = new JCheckBox("Show password");
        showPassCheck.setFont(UIConstants.FONT_BODY_SMALL);
        showPassCheck.setForeground(UIConstants.TEXT_SECONDARY);
        showPassCheck.setOpaque(false);
        showPassCheck.setFocusPainted(false);
        showPassCheck.addActionListener(e -> {
            if (showPassCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        form.add(showPassCheck);
        form.add(Box.createVerticalStrut(18));

        // Login Button
        ModernButton loginBtn = new ModernButton("Sign In & Continue →", ModernButton.ButtonVariant.PRIMARY);
        loginBtn.setPreferredSize(new Dimension(380, 42));
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.addActionListener(e -> handleLogin());

        // Press Enter to submit
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> handleLogin());

        form.add(loginBtn);
        form.add(Box.createVerticalStrut(18));

        // Quick Demo Accounts Fill Bar
        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        demoBar.setOpaque(false);
        JLabel demoLabel = new JLabel("Quick Demo Fill:");
        demoLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        demoLabel.setForeground(UIConstants.TEXT_MUTED);
        demoBar.add(demoLabel);

        ModernButton demoStudent1 = new ModernButton("student1", ModernButton.ButtonVariant.OUTLINE);
        demoStudent1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoStudent1.setCornerRadius(6);
        demoStudent1.addActionListener(e -> {
            usernameField.setText("student1");
            passwordField.setText("pass123");
            errorLabel.setText(" ");
        });

        ModernButton demoCandidate = new ModernButton("candidate", ModernButton.ButtonVariant.OUTLINE);
        demoCandidate.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoCandidate.setCornerRadius(6);
        demoCandidate.addActionListener(e -> {
            usernameField.setText("candidate");
            passwordField.setText("exam2026");
            errorLabel.setText(" ");
        });

        ModernButton demoAlice = new ModernButton("alice", ModernButton.ButtonVariant.OUTLINE);
        demoAlice.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demoAlice.setCornerRadius(6);
        demoAlice.addActionListener(e -> {
            usernameField.setText("alice");
            passwordField.setText("admin123");
            errorLabel.setText(" ");
        });

        demoBar.add(demoStudent1);
        demoBar.add(demoCandidate);
        demoBar.add(demoAlice);
        form.add(demoBar);

        form.add(Box.createVerticalGlue());

        // Register link
        JPanel registerBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerBar.setOpaque(false);
        JLabel noAccountLbl = new JLabel("New candidate?");
        noAccountLbl.setFont(UIConstants.FONT_BODY_SMALL);
        noAccountLbl.setForeground(UIConstants.TEXT_SECONDARY);

        JLabel registerLink = new JLabel("<html><u>Register here</u></html>");
        registerLink.setFont(UIConstants.FONT_BODY_SMALL);
        registerLink.setForeground(UIConstants.BRAND_PRIMARY);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openRegistrationDialog();
            }
        });

        registerBar.add(noAccountLbl);
        registerBar.add(registerLink);
        form.add(registerBar);

        return form;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("⚠ Please enter both username and password.");
            return;
        }

        User user = authService.authenticate(username, password);
        if (user != null) {
            errorLabel.setText(" ");
            onLoginSuccess.accept(user);
        } else {
            errorLabel.setText("⚠ Invalid username or password. Please try again.");
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void openRegistrationDialog() {
        JDialog dialog = new JDialog((java.awt.Frame) null, "Register Candidate", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Candidate Registration");
        title.setFont(UIConstants.FONT_SECTION_TITLE);
        panel.add(title);
        panel.add(Box.createVerticalStrut(15));

        JTextField uField = new JTextField();
        JTextField nField = new JTextField();
        JTextField eField = new JTextField();
        JPasswordField pField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(uField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Full Name:"));
        panel.add(nField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Email Address:"));
        panel.add(eField);
        panel.add(Box.createVerticalStrut(8));

        panel.add(new JLabel("Password:"));
        panel.add(pField);
        panel.add(Box.createVerticalStrut(18));

        ModernButton submitBtn = new ModernButton("Create Account", ModernButton.ButtonVariant.PRIMARY);
        submitBtn.addActionListener(e -> {
            String u = uField.getText().trim();
            String n = nField.getText().trim();
            String em = eField.getText().trim();
            String p = new String(pField.getPassword());

            if (u.isEmpty() || n.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean ok = authService.register(u, p, n, em);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "Registration successful! You can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(u);
                passwordField.setText(p);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Username already taken. Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(submitBtn);
        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public void resetFields() {
        errorLabel.setText(" ");
        passwordField.setText("");
    }
}
