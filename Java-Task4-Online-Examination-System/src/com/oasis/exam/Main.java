package com.oasis.exam;

import com.oasis.exam.ui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Online Examination System.
 * Initializes the Swing Event Dispatch Thread and native look-and-feel.
 */
public class Main {
    public static void main(String[] args) {
        // Enable anti-aliased font rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to cross-platform LookAndFeel if system LAF fails
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
