package com.oasis.exam.ui;

import com.oasis.exam.model.User;
import com.oasis.exam.service.AuthService;
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
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Screen allowing the candidate to review and update profile details
 * (display name and password) before entering the examination hall.
 */
public class ProfilePanel extends JPanel {
    private final AuthService authService;
    private final Consumer<User> onProceedToExam;
    private final Runnable onLogout;

    private User currentUser;
    private JTextField nameField;
    private JTextField rollField;
    private JTextField usernameField;
    private JPasswordField currentPassField;
    private JPasswordField newPassField;
    private JPasswordField confirmPassField;
    private JLabel avatarLabel;
    private JLabel statusMessageLabel;

    public ProfilePanel(AuthService authService, Consumer<User> onProceedToExam, Runnable onLogout) {
        this.authService = authService;
        this.onProceedToExam = onProceedToExam;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(UIConstants.BG_APP);
        initUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        if (user != null) {
            nameField.setText(user.getDisplayName());
            rollField.setText(user.getRollNumber());
            usernameField.setText(user.getUsername());
            currentPassField.setText("");
            newPassField.setText("");
            confirmPassField.setText("");
            statusMessageLabel.setText(" ");
            avatarLabel.setText(user.getInitials());
        }
    }

    private void initUI() {
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(UIConstants.BG_APP);

        JPanel card = new JPanel(new BorderLayout(0, 20)) {
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
        card.setPreferredSize(new Dimension(680, 580));
        card.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Header with Avatar & Title
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);

        avatarLabel = new JLabel("ST", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UIConstants.applyQualityRenderingHints(g2);
                g2.setColor(UIConstants.BRAND_PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatarLabel.setPreferredSize(new Dimension(65, 65));
        avatarLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        avatarLabel.setForeground(Color.WHITE);
        header.add(avatarLabel, BorderLayout.WEST);

        JPanel headerText = new JPanel();
        headerText.setOpaque(false);
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Candidate Profile Management");
        title.setFont(UIConstants.FONT_HEADER_TITLE);
        title.setForeground(UIConstants.TEXT_MAIN);

        JLabel sub = new JLabel("Verify candidate identity and update credentials before taking the test");
        sub.setFont(UIConstants.FONT_BODY_SMALL);
        sub.setForeground(UIConstants.TEXT_MUTED);

        headerText.add(title);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(sub);
        header.add(headerText, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);

        // Body Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Status message
        statusMessageLabel = new JLabel(" ");
        statusMessageLabel.setFont(UIConstants.FONT_SMALL_BOLD);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(statusMessageLabel, gbc);

        // Row 1: Roll Number (read only) & Username (read only)
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(createFieldGroup("Roll / Hall Ticket No", rollField = createStyledTextField(false)), gbc);

        gbc.gridx = 1;
        form.add(createFieldGroup("System Username", usernameField = createStyledTextField(false)), gbc);

        // Row 2: Full Display Name (Editable)
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        form.add(createFieldGroup("Display Name (Full Name)", nameField = createStyledTextField(true)), gbc);

        // Row 3: Security Password Change Section
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel passSection = new JLabel("Security & Password Update");
        passSection.setFont(UIConstants.FONT_SECTION_TITLE);
        passSection.setForeground(UIConstants.TEXT_MAIN);
        form.add(passSection, gbc);

        // Row 4: Current Password
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        currentPassField = createStyledPasswordField();
        form.add(createFieldGroup("Current Password (required to save changes)", currentPassField), gbc);

        // Row 5: New Password & Confirm Password
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        newPassField = createStyledPasswordField();
        form.add(createFieldGroup("New Password (optional)", newPassField), gbc);

        gbc.gridx = 1;
        confirmPassField = createStyledPasswordField();
        form.add(createFieldGroup("Confirm New Password", confirmPassField), gbc);

        card.add(form, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        ModernButton logoutBtn = new ModernButton("← Logout", ModernButton.ButtonVariant.OUTLINE);
        logoutBtn.setPreferredSize(new Dimension(110, 42));
        logoutBtn.addActionListener(e -> onLogout.run());
        bottomBar.add(logoutBtn, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        ModernButton saveBtn = new ModernButton("Save Profile", ModernButton.ButtonVariant.SECONDARY);
        saveBtn.setPreferredSize(new Dimension(130, 42));
        saveBtn.addActionListener(e -> handleSaveProfile());

        ModernButton proceedBtn = new ModernButton("Proceed to Exam Instructions →", ModernButton.ButtonVariant.PRIMARY);
        proceedBtn.setPreferredSize(new Dimension(240, 42));
        proceedBtn.addActionListener(e -> {
            if (currentUser != null) {
                onProceedToExam.accept(currentUser);
            }
        });

        rightActions.add(saveBtn);
        rightActions.add(proceedBtn);
        bottomBar.add(rightActions, BorderLayout.EAST);

        card.add(bottomBar, BorderLayout.SOUTH);

        centerWrapper.add(card, new GridBagConstraints());
        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createFieldGroup(String labelText, javax.swing.JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_SMALL_BOLD);
        lbl.setForeground(UIConstants.TEXT_SECONDARY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JTextField createStyledTextField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setEditable(editable);
        tf.setFont(UIConstants.FONT_BODY);
        tf.setPreferredSize(new Dimension(220, 38));
        tf.setBackground(editable ? Color.WHITE : new Color(241, 245, 249));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return tf;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(UIConstants.FONT_BODY);
        pf.setPreferredSize(new Dimension(220, 38));
        pf.setBackground(Color.WHITE);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_LIGHT, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return pf;
    }

    private void handleSaveProfile() {
        if (currentUser == null) return;

        String newName = nameField.getText().trim();
        String currPass = new String(currentPassField.getPassword()).trim();
        String newPass = new String(newPassField.getPassword()).trim();
        String confPass = new String(confirmPassField.getPassword()).trim();

        if (newName.isEmpty()) {
            statusMessageLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
            statusMessageLabel.setText("⚠ Display name cannot be empty.");
            return;
        }

        if (currPass.isEmpty()) {
            statusMessageLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
            statusMessageLabel.setText("⚠ Please enter your current password to authorize changes.");
            return;
        }

        if (!currPass.equals(currentUser.getPassword())) {
            statusMessageLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
            statusMessageLabel.setText("⚠ Incorrect current password.");
            return;
        }

        if (!newPass.isEmpty()) {
            if (!newPass.equals(confPass)) {
                statusMessageLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
                statusMessageLabel.setText("⚠ New passwords do not match.");
                return;
            }
            if (newPass.length() < 4) {
                statusMessageLabel.setForeground(UIConstants.STATUS_NOT_ANSWERED);
                statusMessageLabel.setText("⚠ New password must be at least 4 characters.");
                return;
            }
        }

        // Apply changes
        currentUser.setDisplayName(newName);
        if (!newPass.isEmpty()) {
            currentUser.setPassword(newPass);
        }

        avatarLabel.setText(currentUser.getInitials());
        currentPassField.setText("");
        newPassField.setText("");
        confirmPassField.setText("");

        statusMessageLabel.setForeground(UIConstants.STATUS_ANSWERED);
        statusMessageLabel.setText("✔ Profile updated successfully! You may now proceed.");
        JOptionPane.showMessageDialog(this, "Profile changes have been successfully saved!", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
    }
}
