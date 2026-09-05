import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class App {

    static final String DB_URL = "jdbc:sqlite:data/reservations.db";
    static final String IMAGE_HERO = "data/hero_banner.jpg";
    static final String IMAGE_LOGO = "data/irctc_logo.jpg";

    static JFrame mainFrame;
    static String loggedUser = null;

    // --- OFFICIAL IRCTC & SLATE COLOR PALETTE ---
    static final Color SIDEBAR_BG      = new Color(15, 23, 42);      // Slate 900
    static final Color SIDEBAR_HOVER   = new Color(30, 41, 59);      // Slate 800
    static final Color SIDEBAR_ACTIVE  = new Color(37, 99, 235);     // Accent Royal Blue
    static final Color SIDEBAR_TEXT    = new Color(203, 213, 225);   // Slate 300

    static final Color HEADER_BG       = new Color(255, 255, 255);
    static final Color CONTENT_BG      = new Color(241, 245, 249);    // Slate 100
    static final Color CARD_BG         = new Color(255, 255, 255);

    static final Color PRIMARY_BLUE    = new Color(37, 99, 235);     // Royal Blue
    static final Color ORANGE_ACCENT   = new Color(249, 115, 22);    // IRCTC Signature Orange
    static final Color SUCCESS_GREEN   = new Color(16, 185, 129);    // Emerald Green
    static final Color WARNING_AMBER   = new Color(245, 158, 11);    // Amber
    static final Color DANGER_RED      = new Color(239, 68, 68);     // Red
    static final Color TEAL_ACCENT     = new Color(14, 165, 233);    // Cyan / Teal
    static final Color SECONDARY_BTN   = new Color(241, 245, 249);   // Light Gray Button

    static final Color TEXT_DARK       = new Color(15, 23, 42);
    static final Color TEXT_MUTED      = new Color(100, 116, 139);
    static final Color BORDER_LIGHT    = new Color(226, 232, 240);
    static final Color READONLY_BG     = new Color(241, 245, 249);
    static final Color FOCUS_GLOW      = new Color(147, 197, 253, 180);

    // --- TYPOGRAPHY ---
    static final Font FONT_LOGO        = new Font("Segoe UI", Font.BOLD, 20);
    static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 22);
    static final Font FONT_HEADING     = new Font("Segoe UI", Font.BOLD, 16);
    static final Font FONT_SUBHEAD     = new Font("Segoe UI", Font.BOLD, 13);
    static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_BTN         = new Font("Segoe UI", Font.BOLD, 13);
    static final Font FONT_MONO        = new Font("Consolas", Font.BOLD, 14);

    // --- NAVIGATION CARDS ---
    static CardLayout contentCardLayout;
    static JPanel contentArea;
    static JPanel sidebarPanel;
    static JLabel headerTitleLabel;
    static Map<String, SidebarButton> navButtons = new HashMap<>();

    static final String CARD_DASHBOARD   = "CARD_DASHBOARD";
    static final String CARD_RESERVATION = "CARD_RESERVATION";
    static final String CARD_TIMETABLE   = "CARD_TIMETABLE";
    static final String CARD_PNR         = "CARD_PNR";
    static final String CARD_CANCEL      = "CARD_CANCEL";

    static final DecimalFormat CURRENCY_FMT = new DecimalFormat("₹#,##0");

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            try {
                setupDatabase();
                createMainGUI();
                showLoginView();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Initialization Error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // --- DATABASE UTILS ---
    static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    static void setupDatabase() {
        try {
            File folder = new File("data");
            if (!folder.exists()) folder.mkdirs();

            try (Connection con = connect(); Statement st = con.createStatement()) {
                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL)"
                );

                st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "pnr TEXT UNIQUE NOT NULL," +
                    "username TEXT NOT NULL," +
                    "passenger TEXT NOT NULL," +
                    "age INTEGER DEFAULT 30," +
                    "gender TEXT DEFAULT 'Male'," +
                    "train_no TEXT NOT NULL," +
                    "train_name TEXT NOT NULL," +
                    "class_type TEXT NOT NULL," +
                    "journey_date TEXT NOT NULL," +
                    "source TEXT NOT NULL," +
                    "destination TEXT NOT NULL," +
                    "coach TEXT DEFAULT 'B2'," +
                    "seat_no TEXT DEFAULT '24 LB'," +
                    "quota TEXT DEFAULT 'General (GN)'," +
                    "fare DOUBLE DEFAULT 0.0," +
                    "status TEXT NOT NULL)"
                );

                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN age INTEGER DEFAULT 30;"); } catch (Exception ignored) {}
                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN gender TEXT DEFAULT 'Male';"); } catch (Exception ignored) {}
                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN coach TEXT DEFAULT 'B2';"); } catch (Exception ignored) {}
                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN seat_no TEXT DEFAULT '24 LB';"); } catch (Exception ignored) {}
                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN quota TEXT DEFAULT 'General (GN)';"); } catch (Exception ignored) {}
                try { st.executeUpdate("ALTER TABLE reservations ADD COLUMN fare DOUBLE DEFAULT 0.0;"); } catch (Exception ignored) {}

                // Seed default demo user accounts
                try {
                    st.executeUpdate("INSERT OR IGNORE INTO users(username, password) VALUES('passenger', '1234')");
                    st.executeUpdate("INSERT OR IGNORE INTO users(username, password) VALUES('admin', '1234')");
                } catch (Exception ignored) {}
            }
        } catch (SQLException e) {
            message(null, "Database initialization error:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MAIN GUI BUILDER ---
    static void createMainGUI() {
        mainFrame = new JFrame("IRCTC Next Generation Ticketing System — Indian Railways");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 780);
        mainFrame.setMinimumSize(new Dimension(1060, 700));
        mainFrame.setLocationRelativeTo(null);
    }

    // --- REAL IRCTC SPLIT-SCREEN LOGIN & REGISTER VIEW ---
    static void showLoginView() {
        JPanel splitRoot = new JPanel(new GridLayout(1, 2));

        // LEFT HERO PANEL WITH REAL IMAGE BANNER & OVERLAY
        JPanel leftHeroPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                ImageIcon heroIcon = loadScaledImage(IMAGE_HERO, w, h);
                if (heroIcon != null) {
                    g2.drawImage(heroIcon.getImage(), 0, 0, null);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42, 190), 0, h, new Color(15, 23, 42, 235));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                } else {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(15, 23, 42), w, h, new Color(30, 41, 59));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, w, h);
                }

                g2.setColor(new Color(249, 115, 22, 35));
                g2.fillOval(-80, -80, 400, 400);

                g2.dispose();
            }
        };
        leftHeroPanel.setLayout(new BorderLayout());
        leftHeroPanel.setBorder(new EmptyBorder(40, 45, 40, 45));

        JPanel heroContent = new JPanel();
        heroContent.setOpaque(false);
        heroContent.setLayout(new BoxLayout(heroContent, BoxLayout.Y_AXIS));

        JLabel govTag = new JLabel("GOVERNMENT OF INDIA ENTERPRISE");
        govTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        govTag.setForeground(ORANGE_ACCENT);

        JLabel mainHeading = new JLabel("<html>INDIAN RAILWAYS<br>NEXT GEN PORTAL</html>");
        mainHeading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        mainHeading.setForeground(Color.WHITE);

        JLabel subHeading = new JLabel("Safety • Security • Punctuality");
        subHeading.setFont(FONT_BODY);
        subHeading.setForeground(SIDEBAR_TEXT);

        heroContent.add(govTag);
        heroContent.add(Box.createVerticalStrut(8));
        heroContent.add(mainHeading);
        heroContent.add(Box.createVerticalStrut(6));
        heroContent.add(subHeading);

        JPanel featurePills = new JPanel();
        featurePills.setOpaque(false);
        featurePills.setLayout(new BoxLayout(featurePills, BoxLayout.Y_AXIS));

        featurePills.add(createFeaturePill("⚡ Vande Bharat & Rajdhani Express Booking"));
        featurePills.add(Box.createVerticalStrut(10));
        featurePills.add(createFeaturePill("🎫 Digital E-Ticket & QR Boarding Pass"));
        featurePills.add(Box.createVerticalStrut(10));
        featurePills.add(createFeaturePill("📡 Real-Time PNR Tracker & Station Stoppages"));
        featurePills.add(Box.createVerticalStrut(10));
        featurePills.add(createFeaturePill("💳 Instant Fare Calculation & Refunds"));

        leftHeroPanel.add(heroContent, BorderLayout.NORTH);
        leftHeroPanel.add(featurePills, BorderLayout.CENTER);

        // RIGHT FORM PANEL WITH IRCTC EMBLEM & TABBED LOGIN
        JPanel rightFormPanel = new JPanel(new GridBagLayout());
        rightFormPanel.setBackground(CONTENT_BG);

        ModernCard formCard = new ModernCard(460, 580, 20);
        formCard.setLayout(new BorderLayout());

        JPanel logoHeaderPanel = new JPanel();
        logoHeaderPanel.setOpaque(false);
        logoHeaderPanel.setLayout(new BoxLayout(logoHeaderPanel, BoxLayout.Y_AXIS));
        logoHeaderPanel.setBorder(new EmptyBorder(25, 40, 10, 40));

        ImageIcon logoIcon = loadScaledImage(IMAGE_LOGO, 80, 80);
        JLabel logoLabel;
        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon);
        } else {
            logoLabel = new JLabel("🚆");
            logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portalTitle = new JLabel("IRCTC Passenger Portal");
        portalTitle.setFont(FONT_TITLE);
        portalTitle.setForeground(TEXT_DARK);
        portalTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portalSub = new JLabel("Ministry of Railways, Govt. of India");
        portalSub.setFont(FONT_SMALL);
        portalSub.setForeground(TEXT_MUTED);
        portalSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoHeaderPanel.add(logoLabel);
        logoHeaderPanel.add(Box.createVerticalStrut(8));
        logoHeaderPanel.add(portalTitle);
        logoHeaderPanel.add(Box.createVerticalStrut(4));
        logoHeaderPanel.add(portalSub);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_SUBHEAD);
        tabbedPane.setBackground(CARD_BG);
        tabbedPane.setForeground(TEXT_DARK);

        // LOGIN TAB
        JPanel loginTab = new JPanel();
        loginTab.setBackground(CARD_BG);
        loginTab.setLayout(new BoxLayout(loginTab, BoxLayout.Y_AXIS));
        loginTab.setBorder(new EmptyBorder(20, 35, 25, 35));

        JLabel uLabel = createLabel("Registered Username");
        ModernTextField usernameField = new ModernTextField("Enter your IRCTC username");
        usernameField.setText("passenger");

        JLabel pLabel = createLabel("Password");
        ModernPasswordField passwordField = new ModernPasswordField("Enter account password");
        passwordField.setText("1234");

        ModernButton loginBtn = new ModernButton("Sign In to IRCTC Portal", ORANGE_ACCENT, Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(350, 44));

        ModernButton quickDemoBtn = new ModernButton("⚡ Quick Demo Sign In", PRIMARY_BLUE, Color.WHITE);
        quickDemoBtn.setPreferredSize(new Dimension(350, 38));

        loginBtn.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                message(mainFrame, "Please enter both username and password.", "Input Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (loginUser(user, pass)) {
                showDashboardView();
            } else {
                message(mainFrame, "Invalid credentials. Please check your username and password.\n\nHint: Demo Credentials are Username: passenger | Password: 1234", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        quickDemoBtn.addActionListener(e -> {
            usernameField.setText("passenger");
            passwordField.setText("1234");
            if (loginUser("passenger", "1234")) {
                showDashboardView();
            }
        });

        passwordField.addActionListener(e -> loginBtn.doClick());
        usernameField.addActionListener(e -> loginBtn.doClick());

        loginTab.add(uLabel);
        loginTab.add(Box.createVerticalStrut(6));
        loginTab.add(usernameField);
        loginTab.add(Box.createVerticalStrut(12));
        loginTab.add(pLabel);
        loginTab.add(Box.createVerticalStrut(6));
        loginTab.add(passwordField);
        loginTab.add(Box.createVerticalStrut(18));
        loginTab.add(loginBtn);
        loginTab.add(Box.createVerticalStrut(10));
        loginTab.add(quickDemoBtn);

        // REGISTER TAB
        JPanel registerTab = new JPanel();
        registerTab.setBackground(CARD_BG);
        registerTab.setLayout(new BoxLayout(registerTab, BoxLayout.Y_AXIS));
        registerTab.setBorder(new EmptyBorder(15, 35, 20, 35));

        JLabel regULabel = createLabel("Choose Username");
        ModernTextField regUserField = new ModernTextField("3-20 characters (letters, numbers)");

        JLabel regPLabel = createLabel("Password");
        ModernPasswordField regPassField = new ModernPasswordField("Minimum 4 characters");

        JLabel regCPLabel = createLabel("Confirm Password");
        ModernPasswordField regConfirmField = new ModernPasswordField("Re-enter password");

        ModernButton registerBtn = new ModernButton("Create Passenger Account", PRIMARY_BLUE, Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(350, 44));

        registerBtn.addActionListener(e -> {
            String user = regUserField.getText().trim();
            String pass = new String(regPassField.getPassword());
            String confirm = new String(regConfirmField.getPassword());

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                message(mainFrame, "Please fill in all registration fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!user.matches("[A-Za-z0-9_]{3,20}")) {
                message(mainFrame, "Username must be 3-20 characters long (alphanumeric & underscore only).", "Invalid Username", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (pass.length() < 4) {
                message(mainFrame, "Password must be at least 4 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pass.equals(confirm)) {
                message(mainFrame, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (registerUser(user, pass)) {
                loggedUser = user;
                message(mainFrame, "Account created successfully! Logging you in now...", "Success", JOptionPane.INFORMATION_MESSAGE);
                showDashboardView();
            }
        });

        registerTab.add(regULabel);
        registerTab.add(Box.createVerticalStrut(4));
        registerTab.add(regUserField);
        registerTab.add(Box.createVerticalStrut(10));
        registerTab.add(regPLabel);
        registerTab.add(Box.createVerticalStrut(4));
        registerTab.add(regPassField);
        registerTab.add(Box.createVerticalStrut(10));
        registerTab.add(regCPLabel);
        registerTab.add(Box.createVerticalStrut(4));
        registerTab.add(regConfirmField);
        registerTab.add(Box.createVerticalStrut(18));
        registerTab.add(registerBtn);

        tabbedPane.addTab("Sign In", loginTab);
        tabbedPane.addTab("Register Passenger", registerTab);

        formCard.add(logoHeaderPanel, BorderLayout.NORTH);
        formCard.add(tabbedPane, BorderLayout.CENTER);

        rightFormPanel.add(formCard);

        splitRoot.add(leftHeroPanel);
        splitRoot.add(rightFormPanel);

        mainFrame.setContentPane(splitRoot);
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
        mainFrame.toFront();
        mainFrame.requestFocus();
    }

    static JPanel createFeaturePill(String text) {
        JPanel pill = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        pill.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SUBHEAD);
        lbl.setForeground(Color.WHITE);
        pill.add(lbl);
        return pill;
    }

    // --- MAIN DASHBOARD VIEW ---
    static void showDashboardView() {
        JPanel root = new JPanel(new BorderLayout());

        // 1. LEFT SIDEBAR PANEL
        sidebarPanel = new JPanel();
        sidebarPanel.setPreferredSize(new Dimension(255, 780));
        sidebarPanel.setBackground(SIDEBAR_BG);
        sidebarPanel.setLayout(new BorderLayout());

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 20));
        brandPanel.setOpaque(false);

        ImageIcon logoSmall = loadScaledImage(IMAGE_LOGO, 38, 38);
        JLabel logoIconLabel = (logoSmall != null) ? new JLabel(logoSmall) : new JLabel("🚆");

        JPanel brandTextPanel = new JPanel();
        brandTextPanel.setOpaque(false);
        brandTextPanel.setLayout(new BoxLayout(brandTextPanel, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel("IRCTC PORTAL");
        logoLabel.setFont(FONT_LOGO);
        logoLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Indian Railways Enterprise");
        subLabel.setFont(FONT_SMALL);
        subLabel.setForeground(SIDEBAR_TEXT);

        brandTextPanel.add(logoLabel);
        brandTextPanel.add(subLabel);

        brandPanel.add(logoIconLabel);
        brandPanel.add(brandTextPanel);

        JPanel navContainer = new JPanel();
        navContainer.setOpaque(false);
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setBorder(new EmptyBorder(10, 12, 10, 12));

        navButtons.clear();
        addNavButton(navContainer, "📊  Dashboard Overview", CARD_DASHBOARD);
        addNavButton(navContainer, "🎫  Book Train Ticket", CARD_RESERVATION);
        addNavButton(navContainer, "🚆  Train Timetable Catalog", CARD_TIMETABLE);
        addNavButton(navContainer, "🔎  PNR Status Lookup", CARD_PNR);
        addNavButton(navContainer, "✕  Cancel Reservation", CARD_CANCEL);

        // Sidebar Bottom User Profile Card
        JPanel userPanel = new JPanel(new BorderLayout(10, 0));
        userPanel.setOpaque(false);
        userPanel.setBorder(new EmptyBorder(16, 16, 20, 16));

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        avatarLabel.setForeground(Color.WHITE);

        JPanel userTextPanel = new JPanel(new GridLayout(2, 1));
        userTextPanel.setOpaque(false);

        JLabel uNameLabel = new JLabel(loggedUser != null ? loggedUser : "Passenger");
        uNameLabel.setFont(FONT_SUBHEAD);
        uNameLabel.setForeground(Color.WHITE);

        JLabel uRoleLabel = new JLabel("Verified Passenger");
        uRoleLabel.setFont(FONT_SMALL);
        uRoleLabel.setForeground(SIDEBAR_TEXT);

        userTextPanel.add(uNameLabel);
        userTextPanel.add(uRoleLabel);

        ModernButton logoutBtn = new ModernButton("Logout", new Color(51, 65, 85), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(72, 34));
        logoutBtn.setFont(FONT_SMALL);
        logoutBtn.addActionListener(e -> {
            loggedUser = null;
            showLoginView();
        });

        userPanel.add(avatarLabel, BorderLayout.WEST);
        userPanel.add(userTextPanel, BorderLayout.CENTER);
        userPanel.add(logoutBtn, BorderLayout.EAST);

        sidebarPanel.add(brandPanel, BorderLayout.NORTH);
        sidebarPanel.add(navContainer, BorderLayout.CENTER);
        sidebarPanel.add(userPanel, BorderLayout.SOUTH);

        // 2. RIGHT MAIN CONTENT AREA
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(CONTENT_BG);

        // Top Header Bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setPreferredSize(new Dimension(945, 64));
        headerBar.setBackground(HEADER_BG);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT),
            new EmptyBorder(0, 28, 0, 28)
        ));

        headerTitleLabel = new JLabel("Dashboard Overview");
        headerTitleLabel.setFont(FONT_TITLE);
        headerTitleLabel.setForeground(TEXT_DARK);

        JLabel dateLabel = new JLabel("📅 " + new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new java.util.Date()));
        dateLabel.setFont(FONT_BODY);
        dateLabel.setForeground(TEXT_MUTED);

        headerBar.add(headerTitleLabel, BorderLayout.WEST);
        headerBar.add(dateLabel, BorderLayout.EAST);

        // Content Area CardLayout
        contentCardLayout = new CardLayout();
        contentArea = new JPanel(contentCardLayout);
        contentArea.setBackground(CONTENT_BG);
        contentArea.setBorder(new EmptyBorder(20, 24, 20, 24));

        contentArea.add(buildDashboardCard(), CARD_DASHBOARD);
        contentArea.add(buildReservationCard(), CARD_RESERVATION);
        contentArea.add(buildTimetableCard(), CARD_TIMETABLE);
        contentArea.add(buildPNRCard(), CARD_PNR);
        contentArea.add(buildCancelCard(), CARD_CANCEL);

        rightPanel.add(headerBar, BorderLayout.NORTH);
        rightPanel.add(contentArea, BorderLayout.CENTER);

        root.add(sidebarPanel, BorderLayout.WEST);
        root.add(rightPanel, BorderLayout.CENTER);

        switchTab(CARD_DASHBOARD, "Dashboard Overview");

        mainFrame.setContentPane(root);
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setVisible(true);
        mainFrame.toFront();
        mainFrame.requestFocus();
    }

    static void addNavButton(JPanel container, String title, String cardKey) {
        SidebarButton btn = new SidebarButton(title);
        btn.addActionListener(e -> switchTab(cardKey, title.substring(4).trim()));
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
        navButtons.put(cardKey, btn);
    }

    static void switchTab(String cardKey, String headerTitle) {
        contentCardLayout.show(contentArea, cardKey);
        headerTitleLabel.setText(headerTitle);

        for (Map.Entry<String, SidebarButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(cardKey));
        }

        if (CARD_DASHBOARD.equals(cardKey)) {
            refreshDashboard();
        }
    }

    // --- TAB 1: DASHBOARD OVERVIEW ---
    static JPanel dashboardStatsContainer;
    static DefaultTableModel dashboardBookingsModel;

    static JPanel buildDashboardCard() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);

        ModernCard welcomeCard = new ModernCard(0, 100, 16);
        welcomeCard.setLayout(new BorderLayout());
        welcomeCard.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel welcomeText = new JPanel();
        welcomeText.setOpaque(false);
        welcomeText.setLayout(new BoxLayout(welcomeText, BoxLayout.Y_AXIS));

        JLabel wTitle = new JLabel("Welcome to IRCTC Portal, " + loggedUser + " 👋");
        wTitle.setFont(FONT_TITLE);
        wTitle.setForeground(TEXT_DARK);

        JLabel wSub = new JLabel("Book train tickets, view live schedules, verify PNR boarding passes, and manage journeys.");
        wSub.setFont(FONT_BODY);
        wSub.setForeground(TEXT_MUTED);

        welcomeText.add(wTitle);
        welcomeText.add(Box.createVerticalStrut(4));
        welcomeText.add(wSub);

        welcomeCard.add(welcomeText, BorderLayout.CENTER);

        dashboardStatsContainer = new JPanel(new GridLayout(1, 4, 16, 0));
        dashboardStatsContainer.setOpaque(false);
        dashboardStatsContainer.setPreferredSize(new Dimension(0, 105));

        ModernCard bookingsCard = new ModernCard(0, 0, 16);
        bookingsCard.setLayout(new BorderLayout(0, 12));
        bookingsCard.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel bookingsTitle = new JLabel("🎫 Your Recent Active Bookings");
        bookingsTitle.setFont(FONT_HEADING);
        bookingsTitle.setForeground(TEXT_DARK);

        String[] cols = {"PNR Number", "Passenger", "Train Details", "Journey Date", "Route", "Seat / Coach", "Total Fare", "Status"};
        dashboardBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable bookingsTable = new JTable(dashboardBookingsModel);
        styleTable(bookingsTable);
        bookingsTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        bookingsCard.add(bookingsTitle, BorderLayout.NORTH);
        bookingsCard.add(scrollPane, BorderLayout.CENTER);

        updateDashboardData();

        JPanel centerLayout = new JPanel(new BorderLayout(0, 16));
        centerLayout.setOpaque(false);
        centerLayout.add(dashboardStatsContainer, BorderLayout.NORTH);
        centerLayout.add(bookingsCard, BorderLayout.CENTER);

        root.add(welcomeCard, BorderLayout.NORTH);
        root.add(centerLayout, BorderLayout.CENTER);

        return root;
    }

    static void refreshDashboard() {
        updateDashboardData();
    }

    static void updateDashboardData() {
        if (dashboardStatsContainer == null) return;

        dashboardStatsContainer.removeAll();

        int userBookingsCount = countUserReservations();
        double totalSpent = countUserTotalSpent();

        dashboardStatsContainer.add(createStatCard("⚡ Train Catalog", TRAIN_DATA.size() + " Trains", "Active Express Routes", PRIMARY_BLUE));
        dashboardStatsContainer.add(createStatCard("🎫 My Bookings", userBookingsCount + " Tickets", "Confirmed Journeys", SUCCESS_GREEN));
        dashboardStatsContainer.add(createStatCard("💳 Total Spend", CURRENCY_FMT.format(totalSpent), "Ticket Reservations", ORANGE_ACCENT));
        dashboardStatsContainer.add(createStatCard("📡 Server Status", "ONLINE", "Database Synced", TEAL_ACCENT));

        dashboardStatsContainer.revalidate();
        dashboardStatsContainer.repaint();

        if (dashboardBookingsModel != null) {
            dashboardBookingsModel.setRowCount(0);
            List<Reservation> list = getUserReservations(loggedUser);
            for (Reservation r : list) {
                dashboardBookingsModel.addRow(new Object[]{
                    r.pnr,
                    r.passenger + " (" + r.age + "/" + r.gender.substring(0, 1) + ")",
                    r.trainNo + " - " + r.trainName,
                    r.date,
                    r.source + " ➔ " + r.destination,
                    r.coach + "-" + r.seatNo,
                    CURRENCY_FMT.format(r.fare),
                    r.status
                });
            }
        }
    }

    static ModernCard createStatCard(String title, String value, String subtitle, Color accentColor) {
        ModernCard card = new ModernCard(0, 105, 16);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(FONT_SUBHEAD);
        tLbl.setForeground(TEXT_MUTED);

        JLabel vLbl = new JLabel(value);
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        vLbl.setForeground(TEXT_DARK);

        JLabel sLbl = new JLabel(subtitle);
        sLbl.setFont(FONT_SMALL);
        sLbl.setForeground(TEXT_MUTED);

        left.add(tLbl);
        left.add(Box.createVerticalStrut(4));
        left.add(vLbl);
        left.add(Box.createVerticalStrut(2));
        left.add(sLbl);

        JPanel strip = new JPanel();
        strip.setPreferredSize(new Dimension(4, 0));
        strip.setBackground(accentColor);

        card.add(left, BorderLayout.CENTER);
        card.add(strip, BorderLayout.WEST);

        return card;
    }

    // --- TAB 2: BOOK TICKET WITH INTERACTIVE ROUTE DROPDOWNS & LIVE PREVIEW ---
    static ETicketCard liveTicketPreviewCard;
    static JLabel fareCalculatedLabel;

    static JPanel buildReservationCard() {
        JPanel root = new JPanel(new GridLayout(1, 2, 20, 0));
        root.setOpaque(false);

        ModernCard formCard = new ModernCard(0, 0, 16);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        ModernTextField passengerField = new ModernTextField("Passenger Full Name");
        ModernTextField ageField = new ModernTextField("Age");
        ageField.setText("30");
        ageField.setPreferredSize(new Dimension(75, 38));

        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        styleComboBox(genderBox);
        genderBox.setPreferredSize(new Dimension(115, 38));

        JComboBox<String> trainSelectBox = new JComboBox<>();
        styleComboBox(trainSelectBox);
        for (TrainInfo info : TRAIN_DATA.values()) {
            trainSelectBox.addItem(info.number + " - " + info.name);
        }

        ModernTextField trainNameField = new ModernTextField("Auto-filled");
        trainNameField.setEditable(false);
        trainNameField.setBackground(READONLY_BG);

        JComboBox<String> sourceBox = new JComboBox<>();
        styleComboBox(sourceBox);

        JComboBox<String> destBox = new JComboBox<>();
        styleComboBox(destBox);

        JComboBox<String> classBox = new JComboBox<>(new String[]{
            "AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC First Class (1A)", "Sleeper Class (SL)", "Executive Chair Car (EC)", "AC Chair Car (CC)"
        });
        styleComboBox(classBox);

        JComboBox<String> quotaBox = new JComboBox<>(new String[]{
            "General (GN)", "Tatkal (TQ)", "Senior Citizen (SS)", "Ladies (LD)"
        });
        styleComboBox(quotaBox);

        ModernTextField dateField = new ModernTextField("DD-MM-YYYY");
        dateField.setText(new SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()));

        fareCalculatedLabel = new JLabel("Total Fare: ₹0 (0 km)");
        fareCalculatedLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        fareCalculatedLabel.setForeground(PRIMARY_BLUE);

        ageField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || ageField.getText().length() >= 3) e.consume();
            }
        });

        passengerField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> cleanLetters(passengerField)));

        final double[] currentComputedFare = {0.0};
        final String[] currentSeatAssignment = {"B2", "24 LB"};
        final int[] currentTripDistance = {0};
        final boolean[] isUpdatingDropdowns = {false};

        Runnable updateTicketAndFare = () -> {
            if (isUpdatingDropdowns[0]) return;

            String p = passengerField.getText().trim();
            String ageStr = ageField.getText().trim();
            String gender = genderBox.getSelectedItem().toString();

            String selectedTrainStr = (trainSelectBox.getSelectedItem() != null) ? trainSelectBox.getSelectedItem().toString() : "";
            String tNo = selectedTrainStr.split(" - ")[0].trim();

            String src = (sourceBox.getSelectedItem() != null) ? sourceBox.getSelectedItem().toString() : "";
            String dest = (destBox.getSelectedItem() != null) ? destBox.getSelectedItem().toString() : "";
            String cl = classBox.getSelectedItem().toString();
            String quota = quotaBox.getSelectedItem().toString();
            String d = dateField.getText().trim();

            TrainInfo info = getTrain(tNo);
            if (info != null && !src.isEmpty() && !dest.isEmpty()) {
                currentTripDistance[0] = getTripDistance(info, src, dest);
                currentComputedFare[0] = calculateFare(info, src, dest, cl, quota);
                currentSeatAssignment[0] = generateCoach(cl);
                currentSeatAssignment[1] = generateSeat(cl);
                trainNameField.setText(info.name);
            } else {
                currentComputedFare[0] = 0.0;
                currentTripDistance[0] = 0;
            }

            fareCalculatedLabel.setText("Total Fare: " + CURRENCY_FMT.format(currentComputedFare[0]) + " (" + currentTripDistance[0] + " km)");

            int age = ageStr.isEmpty() ? 30 : Integer.parseInt(ageStr);

            liveTicketPreviewCard.updateTicketDetails(
                "PREVIEW-PNR",
                p.isEmpty() ? "Passenger Name" : p + " (" + age + "/" + gender.substring(0, 1) + ")",
                tNo.isEmpty() ? "----" : tNo,
                (info != null) ? info.name : "Select Train",
                cl,
                d.isEmpty() ? "DD-MM-YYYY" : d,
                src.isEmpty() ? "FROM" : src,
                dest.isEmpty() ? "TO" : dest,
                currentSeatAssignment[0],
                currentSeatAssignment[1],
                quota,
                currentComputedFare[0],
                "DRAFT"
            );
        };

        Runnable updateStationDropdowns = () -> {
            isUpdatingDropdowns[0] = true;
            String selectedTrainStr = (trainSelectBox.getSelectedItem() != null) ? trainSelectBox.getSelectedItem().toString() : "";
            String tNo = selectedTrainStr.split(" - ")[0].trim();
            TrainInfo info = getTrain(tNo);

            sourceBox.removeAllItems();
            destBox.removeAllItems();

            if (info != null && info.routeStops != null && !info.routeStops.isEmpty()) {
                List<String> validSources = getValidSources(info);
                for (String s : validSources) sourceBox.addItem(s);

                if (sourceBox.getItemCount() > 0) sourceBox.setSelectedIndex(0);

                String selSrc = (sourceBox.getSelectedItem() != null) ? sourceBox.getSelectedItem().toString() : "";
                List<String> validDests = getValidDestinations(info, selSrc);
                for (String d : validDests) destBox.addItem(d);

                if (destBox.getItemCount() > 0) destBox.setSelectedIndex(destBox.getItemCount() - 1);
            }
            isUpdatingDropdowns[0] = false;
            updateTicketAndFare.run();
        };

        sourceBox.addActionListener(e -> {
            if (isUpdatingDropdowns[0]) return;
            isUpdatingDropdowns[0] = true;

            String selectedTrainStr = (trainSelectBox.getSelectedItem() != null) ? trainSelectBox.getSelectedItem().toString() : "";
            String tNo = selectedTrainStr.split(" - ")[0].trim();
            TrainInfo info = getTrain(tNo);

            String selSrc = (sourceBox.getSelectedItem() != null) ? sourceBox.getSelectedItem().toString() : "";
            destBox.removeAllItems();

            if (info != null) {
                List<String> validDests = getValidDestinations(info, selSrc);
                for (String d : validDests) destBox.addItem(d);
                if (destBox.getItemCount() > 0) destBox.setSelectedIndex(destBox.getItemCount() - 1);
            }

            isUpdatingDropdowns[0] = false;
            updateTicketAndFare.run();
        });

        trainSelectBox.addActionListener(e -> updateStationDropdowns.run());
        destBox.addActionListener(e -> updateTicketAndFare.run());
        passengerField.getDocument().addDocumentListener(new SimpleDocumentListener(updateTicketAndFare));
        ageField.getDocument().addDocumentListener(new SimpleDocumentListener(updateTicketAndFare));
        dateField.getDocument().addDocumentListener(new SimpleDocumentListener(updateTicketAndFare));
        genderBox.addActionListener(e -> updateTicketAndFare.run());
        classBox.addActionListener(e -> updateTicketAndFare.run());
        quotaBox.addActionListener(e -> updateTicketAndFare.run());
        liveTicketPreviewCard = new ETicketCard();
        updateStationDropdowns.run();

        int r = 0;
        addRow(formCard, g, r++, "Passenger Name", passengerField);

        g.gridx = 0; g.gridy = r; g.weightx = 0; g.gridwidth = 1;
        JLabel ageLbl = new JLabel("Age & Gender");
        ageLbl.setFont(FONT_SUBHEAD);
        ageLbl.setForeground(TEXT_DARK);
        formCard.add(ageLbl, g);

        JPanel ageGenderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        ageGenderPanel.setOpaque(false);
        ageGenderPanel.add(ageField);
        ageGenderPanel.add(genderBox);

        g.gridx = 1; g.weightx = 1;
        formCard.add(ageGenderPanel, g);
        r++;

        addRow(formCard, g, r++, "Select Train Route", trainSelectBox);
        addRow(formCard, g, r++, "Source Station (Boarding)", sourceBox);
        addRow(formCard, g, r++, "Destination Station", destBox);
        addRow(formCard, g, r++, "Travel Class", classBox);
        addRow(formCard, g, r++, "Quota Category", quotaBox);
        addRow(formCard, g, r++, "Journey Date", dateField);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.insets = new Insets(8, 6, 4, 6);
        formCard.add(fareCalculatedLabel, g);
        r++;

        ModernButton bookBtn = new ModernButton("Confirm & Pay Ticket Fare", PRIMARY_BLUE, Color.WHITE);
        bookBtn.setPreferredSize(new Dimension(280, 42));

        bookBtn.addActionListener(e -> {
            String p = passengerField.getText().trim();
            String ageStr = ageField.getText().trim();
            String gender = genderBox.getSelectedItem().toString();

            String selectedTrainStr = (trainSelectBox.getSelectedItem() != null) ? trainSelectBox.getSelectedItem().toString() : "";
            String t = selectedTrainStr.split(" - ")[0].trim();
            String tn = trainNameField.getText().trim();

            String from = (sourceBox.getSelectedItem() != null) ? sourceBox.getSelectedItem().toString() : "";
            String to = (destBox.getSelectedItem() != null) ? destBox.getSelectedItem().toString() : "";

            String cl = classBox.getSelectedItem().toString();
            String quota = quotaBox.getSelectedItem().toString();
            String d = dateField.getText().trim();

            if (p.isEmpty() || t.isEmpty() || d.isEmpty() || ageStr.isEmpty() || from.isEmpty() || to.isEmpty()) {
                message(mainFrame, "Please fill in all mandatory fields.", "Form Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (from.equals(to)) {
                message(mainFrame, "Source and Destination stations cannot be the same.", "Invalid Stations", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!p.matches("[A-Za-z ]+")) {
                message(mainFrame, "Passenger name must contain letters only.", "Invalid Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) {
                message(mainFrame, "Please enter a valid passenger age (1-120).", "Invalid Age", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!validDate(d)) {
                message(mainFrame, "Enter date in valid DD-MM-YYYY format.", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String pnr = generatePNR();
            String coach = currentSeatAssignment[0];
            String seat = currentSeatAssignment[1];
            double finalFare = currentComputedFare[0];

            if (saveReservationFull(pnr, loggedUser, p, age, gender, t, tn, cl, d, from, to, coach, seat, quota, finalFare)) {
                message(mainFrame, 
                    "Reservation Confirmed Successfully! 🎉\n\n" +
                    "PNR Number: " + pnr + "\n" +
                    "Passenger: " + p + " (" + age + ", " + gender + ")\n" +
                    "Train: " + t + " - " + tn + "\n" +
                    "Boarding Station: " + from + "\n" +
                    "Alighting Station: " + to + " (" + currentTripDistance[0] + " km)\n" +
                    "Coach/Seat: " + coach + " / " + seat + "\n" +
                    "Journey Date: " + d + "\n" +
                    "Total Ticket Fare: " + CURRENCY_FMT.format(finalFare) + "\n" +
                    "Status: CONFIRMED", 
                    "Booking Success", JOptionPane.INFORMATION_MESSAGE);

                passengerField.setText("");
                updateDashboardData();
            }
        });

        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.weightx = 1;
        g.insets = new Insets(10, 6, 0, 6);
        formCard.add(bookBtn, g);

        JPanel rightCol = new JPanel(new BorderLayout(0, 10));
        rightCol.setOpaque(false);

        JLabel previewTitle = new JLabel("Live IRCTC E-Ticket Preview");
        previewTitle.setFont(FONT_HEADING);
        previewTitle.setForeground(TEXT_DARK);


        rightCol.add(previewTitle, BorderLayout.NORTH);
        rightCol.add(liveTicketPreviewCard, BorderLayout.CENTER);

        root.add(formCard);
        root.add(rightCol);

        return root;
    }

    // --- TAB 3: TRAIN TIMETABLE CATALOG ---
    static DefaultTableModel timetableModel;

    static JPanel buildTimetableCard() {
        ModernCard card = new ModernCard(0, 0, 16);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel heading = new JLabel("Indian Railways Official Train Timetables & Stoppages");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);

        ModernTextField searchField = new ModernTextField("Search train, station, route...");
        searchField.setPreferredSize(new Dimension(340, 42));

        topPanel.add(heading, BorderLayout.WEST);
        topPanel.add(searchField, BorderLayout.EAST);

        JPanel filterTabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterTabs.setOpaque(false);

        String[] categories = {"All Trains", "Vande Bharat ⚡", "Rajdhani 🚄", "Shatabdi 🚆", "Superfast / Express"};
        final String[] activeCategory = {"All Trains"};

        for (String cat : categories) {
            ModernButton catBtn = new ModernButton(cat, SECONDARY_BTN, TEXT_DARK);
            catBtn.setFont(FONT_SMALL);
            catBtn.setPreferredSize(new Dimension(140, 32));

            catBtn.addActionListener(e -> {
                activeCategory[0] = cat;
                filterTimetable(searchField.getText().trim(), activeCategory[0]);
            });
            filterTabs.add(catBtn);
        }

        String[] columns = {"Train No", "Train Name", "Category", "Origin ➔ Destination", "Route Stoppages", "Dep / Arr", "Distance", "Status"};

        timetableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        populateTimetable(timetableModel, TRAIN_DATA.values());

        JTable table = new JTable(timetableModel);
        styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            filterTimetable(searchField.getText().trim(), activeCategory[0]);
        }));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel headerContainer = new JPanel(new BorderLayout(0, 10));
        headerContainer.setOpaque(false);
        headerContainer.add(topPanel, BorderLayout.NORTH);
        headerContainer.add(filterTabs, BorderLayout.SOUTH);

        card.add(headerContainer, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    static void populateTimetable(DefaultTableModel model, Collection<TrainInfo> trains) {
        model.setRowCount(0);
        for (TrainInfo t : trains) {
            StringBuilder routeSummary = new StringBuilder();
            if (t.routeStops != null) {
                for (int i = 0; i < t.routeStops.size(); i++) {
                    routeSummary.append(t.routeStops.get(i).stationName);
                    if (i < t.routeStops.size() - 1) routeSummary.append(" ➔ ");
                }
            } else {
                routeSummary.append(t.from).append(" ➔ ").append(t.to);
            }

            model.addRow(new Object[]{
                t.number, t.name, t.type, t.from + " ➔ " + t.to, routeSummary.toString(), t.departure + " / " + t.arrival, t.distance + " km", liveStatus(t)
            });
        }
    }

    static void filterTimetable(String query, String category) {
        String q = query.toLowerCase();
        List<TrainInfo> filtered = new ArrayList<>();

        for (TrainInfo t : TRAIN_DATA.values()) {
            boolean matchesCat = true;
            if ("Vande Bharat ⚡".equals(category)) matchesCat = "Vande Bharat".equalsIgnoreCase(t.type);
            else if ("Rajdhani 🚄".equals(category)) matchesCat = "Rajdhani".equalsIgnoreCase(t.type);
            else if ("Shatabdi 🚆".equals(category)) matchesCat = "Shatabdi".equalsIgnoreCase(t.type);
            else if ("Superfast / Express".equals(category)) matchesCat = t.type.contains("Superfast") || t.type.contains("Express");

            StringBuilder routeSummary = new StringBuilder();
            if (t.routeStops != null) {
                for (StationStop st : t.routeStops) routeSummary.append(" ").append(st.stationName);
            }

            String full = (t.number + " " + t.name + " " + t.type + " " + t.from + " " + t.to + routeSummary).toLowerCase();
            boolean matchesQuery = q.isEmpty() || full.contains(q);

            if (matchesCat && matchesQuery) {
                filtered.add(t);
            }
        }
        populateTimetable(timetableModel, filtered);
    }

    // --- TAB 4: PNR STATUS LOOKUP ---
    static ETicketCard pnrResultTicketCard;

    static JPanel buildPNRCard() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);

        ModernCard searchCard = new ModernCard(0, 95, 16);
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 20));
        searchCard.setBorder(new EmptyBorder(10, 24, 10, 24));

        JLabel searchLabel = new JLabel("Enter 10-Digit PNR:");
        searchLabel.setFont(FONT_HEADING);
        searchLabel.setForeground(TEXT_DARK);

        ModernTextField pnrSearchField = new ModernTextField("10-digit numeric PNR");
        pnrSearchField.setPreferredSize(new Dimension(300, 44));

        pnrSearchField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || pnrSearchField.getText().length() >= 10) {
                    e.consume();
                }
            }
        });

        ModernButton searchBtn = new ModernButton("🔎  Get PNR Status", PRIMARY_BLUE, Color.WHITE);
        searchBtn.setPreferredSize(new Dimension(190, 44));

        searchCard.add(searchLabel);
        searchCard.add(pnrSearchField);
        searchCard.add(searchBtn);

        pnrResultTicketCard = new ETicketCard();

        searchBtn.addActionListener(e -> {
            String pnr = pnrSearchField.getText().trim();
            if (!pnr.matches("\\d{10}")) {
                message(mainFrame, "PNR must contain exactly 10 numeric digits.", "Invalid PNR", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Reservation r = findReservation(pnr);
            if (r == null) {
                message(mainFrame, "No reservation found for this PNR number.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (!r.username.equals(loggedUser)) {
                message(mainFrame, "Access denied. This PNR belongs to another passenger account.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            pnrResultTicketCard.updateTicketDetails(
                r.pnr,
                r.passenger + " (" + r.age + "/" + r.gender.substring(0, 1) + ")",
                r.trainNo, r.trainName, r.classType, r.date, r.source, r.destination,
                r.coach, r.seatNo, r.quota, r.fare, r.status
            );
        });

        root.add(searchCard, BorderLayout.NORTH);
        root.add(pnrResultTicketCard, BorderLayout.CENTER);

        return root;
    }

    // --- TAB 5: CANCEL RESERVATION ---
    static ETicketCard cancelTicketPreviewCard;
    static final Reservation[] fetchedForCancel = {null};

    static JPanel buildCancelCard() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);

        ModernCard searchCard = new ModernCard(0, 95, 16);
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 20));
        searchCard.setBorder(new EmptyBorder(10, 24, 10, 24));

        JLabel pnrLabel = new JLabel("Enter PNR to Cancel:");
        pnrLabel.setFont(FONT_HEADING);
        pnrLabel.setForeground(TEXT_DARK);

        ModernTextField cancelPnrField = new ModernTextField("10-digit PNR");
        cancelPnrField.setPreferredSize(new Dimension(280, 44));

        cancelPnrField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) || cancelPnrField.getText().length() >= 10) {
                    e.consume();
                }
            }
        });

        ModernButton fetchBtn = new ModernButton("🔎  Fetch Details", PRIMARY_BLUE, Color.WHITE);
        fetchBtn.setPreferredSize(new Dimension(170, 44));

        ModernButton confirmCancelBtn = new ModernButton("Confirm Ticket Cancellation", DANGER_RED, Color.WHITE);
        confirmCancelBtn.setPreferredSize(new Dimension(230, 44));
        confirmCancelBtn.setEnabled(false);

        searchCard.add(pnrLabel);
        searchCard.add(cancelPnrField);
        searchCard.add(fetchBtn);
        searchCard.add(confirmCancelBtn);

        cancelTicketPreviewCard = new ETicketCard();

        fetchBtn.addActionListener(e -> {
            String pnr = cancelPnrField.getText().trim();
            if (!pnr.matches("\\d{10}")) {
                fetchedForCancel[0] = null;
                confirmCancelBtn.setEnabled(false);
                message(mainFrame, "PNR must contain exactly 10 numeric digits.", "Invalid PNR", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Reservation r = findReservation(pnr);
            if (r == null) {
                fetchedForCancel[0] = null;
                confirmCancelBtn.setEnabled(false);
                message(mainFrame, "No reservation found for this PNR.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (!r.username.equals(loggedUser)) {
                fetchedForCancel[0] = null;
                confirmCancelBtn.setEnabled(false);
                message(mainFrame, "Access Denied. This PNR belongs to another account.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            fetchedForCancel[0] = r;
            cancelTicketPreviewCard.updateTicketDetails(
                r.pnr, r.passenger, r.trainNo, r.trainName, r.classType, r.date, r.source, r.destination,
                r.coach, r.seatNo, r.quota, r.fare, r.status
            );
            confirmCancelBtn.setEnabled(true);
        });

        confirmCancelBtn.addActionListener(e -> {
            Reservation r = fetchedForCancel[0];
            if (r == null) return;

            int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to cancel booking for PNR " + r.pnr + "?\nPassenger: " + r.passenger + "\nTrain: " + r.trainNo + " (" + r.trainName + ")\nRefund Amount: " + CURRENCY_FMT.format(r.fare * 0.90) + "\n\nThis action will permanently remove the reservation record.",
                "Confirm Ticket Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                if (cancelReservation(r.pnr)) {
                    message(mainFrame, "Ticket PNR " + r.pnr + " cancelled successfully.\nRefund of " + CURRENCY_FMT.format(r.fare * 0.90) + " initiated.", "Cancellation Complete", JOptionPane.INFORMATION_MESSAGE);
                    fetchedForCancel[0] = null;
                    cancelPnrField.setText("");
                    confirmCancelBtn.setEnabled(false);
                    cancelTicketPreviewCard.updateTicketDetails("----", "----", "----", "----", "----", "----", "----", "----", "--", "--", "--", 0.0, "CANCELLED");
                    updateDashboardData();
                } else {
                    message(mainFrame, "Failed to cancel ticket.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        root.add(searchCard, BorderLayout.NORTH);
        root.add(cancelTicketPreviewCard, BorderLayout.CENTER);

        return root;
    }

    // --- STYLING & IMAGE HELPERS ---

    static ImageIcon loadScaledImage(String path, int w, int h) {
        try {
            File f = new File(path);
            if (f.exists()) {
                Image img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SUBHEAD);
        label.setForeground(TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static void addRow(JPanel panel, GridBagConstraints g, int row, String labelText, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; g.gridwidth = 1;
        JLabel l = new JLabel(labelText);
        l.setFont(FONT_SUBHEAD);
        l.setForeground(TEXT_DARK);
        panel.add(l, g);

        g.gridx = 1; g.weightx = 1;
        panel.add(comp, g);
    }

    static void styleComboBox(JComboBox<String> box) {
        box.setFont(FONT_BODY);
        box.setPreferredSize(new Dimension(300, 38));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.setBackground(Color.WHITE);
        box.setForeground(TEXT_DARK);
    }

    static void styleTable(JTable table) {
        table.setRowHeight(42);
        table.setFont(FONT_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader th = table.getTableHeader();
        th.setFont(FONT_SUBHEAD);
        th.setBackground(SIDEBAR_BG);
        th.setForeground(Color.WHITE);
        th.setPreferredSize(new Dimension(0, 44));
        th.setReorderingAllowed(false);
    }

    static void cleanLetters(JTextField field) {
        String val = field.getText();
        if (!val.matches("[A-Za-z ]*")) {
            String cleaned = val.replaceAll("[^A-Za-z ]", "");
            SwingUtilities.invokeLater(() -> {
                field.setText(cleaned);
                field.setCaretPosition(cleaned.length());
            });
        }
    }

    static void message(Component parent, String text, String title, int type) {
        JOptionPane.showMessageDialog(parent, text, title, type);
    }

    // --- ROUTE & FARE CALCULATION ENGINE ---

    static List<String> getValidSources(TrainInfo train) {
        List<String> list = new ArrayList<>();
        if (train == null || train.routeStops == null) return list;
        for (int i = 0; i < train.routeStops.size() - 1; i++) {
            list.add(train.routeStops.get(i).stationName);
        }
        return list;
    }

    static List<String> getValidDestinations(TrainInfo train, String selectedSource) {
        List<String> list = new ArrayList<>();
        if (train == null || train.routeStops == null) return list;
        boolean foundSource = false;
        for (StationStop stop : train.routeStops) {
            if (foundSource) {
                list.add(stop.stationName);
            } else if (stop.stationName.equalsIgnoreCase(selectedSource)) {
                foundSource = true;
            }
        }
        return list;
    }

    static int getTripDistance(TrainInfo train, String source, String dest) {
        if (train == null || train.routeStops == null) return 0;
        int srcDist = -1, dstDist = -1;
        for (StationStop stop : train.routeStops) {
            if (stop.stationName.equalsIgnoreCase(source)) srcDist = stop.distance;
            if (stop.stationName.equalsIgnoreCase(dest)) dstDist = stop.distance;
        }
        if (srcDist != -1 && dstDist != -1 && dstDist > srcDist) {
            return dstDist - srcDist;
        }
        return train.distance;
    }

    static double calculateFare(TrainInfo train, String source, String dest, String classType, String quota) {
        int tripDist = getTripDistance(train, source, dest);
        double perKmRate = (train.distance > 0) ? (train.baseFare / train.distance) : 1.0;
        double baseTripFare = Math.max(120.0, tripDist * perKmRate);

        double multiplier = 1.0;
        if (classType.contains("3A")) multiplier = 1.6;
        else if (classType.contains("2A")) multiplier = 2.4;
        else if (classType.contains("1A")) multiplier = 4.0;
        else if (classType.contains("EC")) multiplier = 3.2;
        else if (classType.contains("CC")) multiplier = 1.5;

        double quotaFee = quota.contains("Tatkal") ? 250.0 : 0.0;
        return Math.round((baseTripFare * multiplier) + quotaFee);
    }

    static String generateCoach(String classType) {
        Random rnd = new Random();
        if (classType.contains("1A")) return "H" + (1 + rnd.nextInt(2));
        if (classType.contains("2A")) return "A" + (1 + rnd.nextInt(4));
        if (classType.contains("3A")) return "B" + (1 + rnd.nextInt(8));
        if (classType.contains("EC")) return "E" + (1 + rnd.nextInt(2));
        if (classType.contains("CC")) return "C" + (1 + rnd.nextInt(5));
        return "S" + (1 + rnd.nextInt(10));
    }

    static String generateSeat(String classType) {
        Random rnd = new Random();
        int num = 1 + rnd.nextInt(72);
        String berth;
        int rem = num % 8;
        if (rem == 1 || rem == 4) berth = "LB (Lower)";
        else if (rem == 2 || rem == 5) berth = "MB (Middle)";
        else if (rem == 3 || rem == 6) berth = "UB (Upper)";
        else if (rem == 7) berth = "SL (Side Lower)";
        else berth = "SU (Side Upper)";

        return num + " " + berth;
    }

    // --- CUSTOM VECTOR COMPONENTS ---
    static class ModernCard extends JPanel {
        private int cornerRadius;

        ModernCard(int width, int height, int radius) {
            this.cornerRadius = radius;
            setOpaque(false);
            if (width > 0 && height > 0) {
                setPreferredSize(new Dimension(width, height));
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(15, 23, 42, 10));
            g2.fill(new RoundRectangle2D.Float(2, 4, w - 4, h - 4, cornerRadius, cornerRadius));

            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));

            g2.setColor(BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, cornerRadius, cornerRadius));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ModernButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private boolean isHovered = false;
        private boolean isPressed = false;

        ModernButton(String text, Color bg, Color fg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = bg.brighter();
            this.pressedBg = bg.darker();
            setFont(FONT_BTN);
            setForeground(fg);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; isPressed = false; repaint(); }
                public void mousePressed(MouseEvent e) { isPressed = true; repaint(); }
                public void mouseReleased(MouseEvent e) { isPressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!isEnabled()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(new Color(148, 163, 184));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color paintBg = isPressed ? pressedBg : (isHovered ? hoverBg : normalBg);
            g2.setColor(paintBg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class SidebarButton extends JButton {
        private boolean isActive = false;
        private boolean isHovered = false;

        SidebarButton(String text) {
            super(text);
            setFont(FONT_SUBHEAD);
            setForeground(SIDEBAR_TEXT);
            setHorizontalAlignment(SwingConstants.LEFT);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setBorder(new EmptyBorder(10, 16, 10, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setForeground(active ? Color.WHITE : (isHovered ? Color.WHITE : SIDEBAR_TEXT));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(SIDEBAR_ACTIVE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            } else if (isHovered) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class ModernTextField extends JTextField {
        private String placeholder;

        ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setFont(FONT_BODY);
            setPreferredSize(new Dimension(300, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(isEditable() ? CARD_BG : READONLY_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));

            if (hasFocus() && isEditable()) {
                g2.setColor(FOCUS_GLOW);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 10, 10));
                g2.setColor(PRIMARY_BLUE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));
            } else {
                g2.setColor(BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && placeholder != null && !hasFocus()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(TEXT_MUTED);
                g3.setFont(FONT_BODY);
                Insets insets = getInsets();
                g3.drawString(placeholder, insets.left, getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 2);
                g3.dispose();
            }
        }
    }

    static class ModernPasswordField extends JPasswordField {
        private String placeholder;

        ModernPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(FONT_BODY);
            setPreferredSize(new Dimension(300, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            setOpaque(false);
            setBorder(new EmptyBorder(8, 14, 8, 14));

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { repaint(); }
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(CARD_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));

            if (hasFocus()) {
                g2.setColor(FOCUS_GLOW);
                g2.setStroke(new BasicStroke(2.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 10, 10));
                g2.setColor(PRIMARY_BLUE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));
            } else {
                g2.setColor(BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 10, 10));
            }

            g2.dispose();
            super.paintComponent(g);

            if (getPassword().length == 0 && placeholder != null && !hasFocus()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(TEXT_MUTED);
                g3.setFont(FONT_BODY);
                Insets insets = getInsets();
                g3.drawString(placeholder, insets.left, getHeight() / 2 + g3.getFontMetrics().getAscent() / 2 - 2);
                g3.dispose();
            }
        }
    }

    // 6. REALISTIC IRCTC DIGITAL E-TICKET & BOARDING PASS
    static class ETicketCard extends JPanel {
        private String pnr = "8419204812";
        private String passenger = "John Doe (30/M)";
        private String trainNo = "20901";
        private String trainName = "Vande Bharat Express";
        private String classType = "Executive Chair Car (EC)";
        private String date = "05-09-2026";
        private String source = "Mumbai Central";
        private String dest = "Gandhinagar Capital";
        private String coach = "E1";
        private String seatNo = "24 LB";
        private String quota = "General (GN)";
        private double fare = 1850.0;
        private String status = "CONFIRMED";

        ETicketCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(440, 470));
        }

        public void updateTicketDetails(String pnr, String p, String tNo, String tName, String cl, String d, String src, String dst, String coach, String seat, String quota, double fare, String st) {
            this.pnr = pnr;
            this.passenger = p;
            this.trainNo = tNo;
            this.trainName = tName;
            this.classType = cl;
            this.date = d;
            this.source = src;
            this.dest = dst;
            this.coach = coach;
            this.seatNo = seat;
            this.quota = quota;
            this.fare = fare;
            this.status = st;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Color.WHITE);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

            g2.setColor(BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 16, 16));

            g2.setColor(SIDEBAR_BG);
            g2.fill(new RoundRectangle2D.Float(0, 0, w - 1, 55, 16, 16));
            g2.fillRect(0, 40, w - 1, 15);

            g2.setColor(Color.WHITE);
            g2.setFont(FONT_SUBHEAD);
            g2.drawString("GET IRCTC BOARDING PASS", 18, 33);

            Color statusBg = "CONFIRMED".equals(status) ? SUCCESS_GREEN : ("CANCELLED".equals(status) ? DANGER_RED : PRIMARY_BLUE);
            g2.setColor(statusBg);
            g2.fill(new RoundRectangle2D.Float(w - 125, 16, 105, 24, 12, 12));
            g2.setColor(Color.WHITE);
            g2.setFont(FONT_SMALL);
            FontMetrics fm = g2.getFontMetrics();
            int stX = w - 125 + (105 - fm.stringWidth(status)) / 2;
            g2.drawString(status, stX, 32);

            g2.setColor(READONLY_BG);
            g2.fillRect(0, 55, w - 1, 36);
            g2.setColor(TEXT_MUTED);
            g2.setFont(FONT_SMALL);
            g2.drawString("PNR NUMBER", 18, 78);
            g2.setColor(PRIMARY_BLUE);
            g2.setFont(FONT_MONO);
            g2.drawString(pnr, 110, 78);

            g2.setColor(TEXT_MUTED);
            g2.setFont(FONT_SMALL);
            g2.drawString("QUOTA: " + quota, w - 160, 78);

            int routeY = 120;
            g2.setColor(TEXT_DARK);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(source, 18, routeY);

            int dstWidth = g2.getFontMetrics().stringWidth(dest);
            g2.drawString(dest, w - 18 - dstWidth, routeY);

            g2.setColor(PRIMARY_BLUE);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(140, routeY - 6, w - 140, routeY - 6);
            g2.drawString("➔", (w / 2) - 8, routeY - 1);

            g2.setColor(BORDER_LIGHT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawLine(18, 140, w - 18, 140);

            int y = 165;
            drawInfoField(g2, "PASSENGER DETAILS", passenger, 18, y);
            drawInfoField(g2, "JOURNEY DATE", date, w / 2 + 10, y);

            y += 44;
            drawInfoField(g2, "TRAIN NO & NAME", trainNo + " - " + trainName, 18, y);

            y += 44;
            drawInfoField(g2, "COACH / SEAT NO", coach + " / " + seatNo, 18, y);
            drawInfoField(g2, "CLASS", classType, w / 2 + 10, y);

            y += 44;
            drawInfoField(g2, "TOTAL TICKET FARE", CURRENCY_FMT.format(fare), 18, y);
            drawInfoField(g2, "PAYMENT STATUS", "PAID (ONLINE)", w / 2 + 10, y);

            int barcodeY = h - 65;
            g2.setColor(BORDER_LIGHT);
            g2.drawLine(18, barcodeY - 12, w - 18, barcodeY - 12);

            g2.setColor(TEXT_DARK);
            Random barRnd = new Random(pnr.hashCode());
            int barX = 24;
            while (barX < w - 90) {
                int bw = 1 + barRnd.nextInt(3);
                g2.fillRect(barX, barcodeY, bw, 38);
                barX += bw + 2 + barRnd.nextInt(3);
            }

            int qrX = w - 70;
            int qrY = barcodeY - 2;
            g2.setColor(TEXT_DARK);
            g2.fillRect(qrX, qrY, 44, 44);
            g2.setColor(Color.WHITE);
            g2.fillRect(qrX + 6, qrY + 6, 12, 12);
            g2.fillRect(qrX + 26, qrY + 6, 12, 12);
            g2.fillRect(qrX + 6, qrY + 26, 12, 12);
            g2.setColor(TEXT_DARK);
            g2.fillRect(qrX + 9, qrY + 9, 6, 6);
            g2.fillRect(qrX + 29, qrY + 9, 6, 6);
            g2.fillRect(qrX + 9, qrY + 29, 6, 6);

            g2.dispose();
        }

        private void drawInfoField(Graphics2D g2, String label, String value, int x, int y) {
            g2.setColor(TEXT_MUTED);
            g2.setFont(FONT_SMALL);
            g2.drawString(label, x, y);

            g2.setColor(TEXT_DARK);
            g2.setFont(FONT_SUBHEAD);
            g2.drawString(value, x, y + 17);
        }
    }

    // 7. TABLE STATUS BADGE RENDERER
    static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean sel, boolean focus, int r, int c) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, val, sel, focus, r, c);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_SUBHEAD);

            String status = String.valueOf(val);
            if ("BOARDING".equals(status)) {
                label.setForeground(DANGER_RED);
            } else if ("DEPARTING SOON".equals(status)) {
                label.setForeground(WARNING_AMBER);
            } else {
                label.setForeground(SUCCESS_GREEN);
            }
            return label;
        }
    }

    // --- DB OPERATIONS ---

    static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().toLowerCase().contains("unique")) {
                message(mainFrame, "Username already exists. Please pick a different username.", "Registration Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                message(mainFrame, "Error creating user:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
    }

    static boolean loginUser(String username, String password) {
        String u = username != null ? username.trim() : "";
        String p = password != null ? password : "";
        String sql = "SELECT id, username FROM users WHERE LOWER(username)=LOWER(?) AND password=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loggedUser = rs.getString("username");
                return true;
            }
            return false;
        } catch (SQLException e) {
            message(mainFrame, "Login query failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    static int countUserReservations() {
        if (loggedUser == null) return 0;
        String sql = "SELECT COUNT(*) FROM reservations WHERE username=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loggedUser);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    static double countUserTotalSpent() {
        if (loggedUser == null) return 0.0;
        String sql = "SELECT SUM(fare) FROM reservations WHERE username=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loggedUser);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) { return 0.0; }
    }

    static boolean saveReservationFull(String pnr, String user, String pass, int age, String gender, String tNo, String tName, String cl, String d, String src, String dest, String coach, String seat, String quota, double fare) {
        String sql = "INSERT INTO reservations (pnr,username,passenger,age,gender,train_no,train_name,class_type,journey_date,source,destination,coach,seat_no,quota,fare,status) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr); ps.setString(2, user); ps.setString(3, pass);
            ps.setInt(4, age); ps.setString(5, gender); ps.setString(6, tNo);
            ps.setString(7, tName); ps.setString(8, cl); ps.setString(9, d);
            ps.setString(10, src); ps.setString(11, dest); ps.setString(12, coach);
            ps.setString(13, seat); ps.setString(14, quota); ps.setDouble(15, fare);
            ps.setString(16, "CONFIRMED");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            message(mainFrame, "Save failed:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    static boolean cancelReservation(String pnr) {
        String sql = "DELETE FROM reservations WHERE pnr=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            message(mainFrame, "Cancellation failed:\n" + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    static Reservation findReservation(String pnr) {
        String sql = "SELECT * FROM reservations WHERE pnr=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reservation r = new Reservation();
                r.pnr = rs.getString("pnr"); r.username = rs.getString("username");
                r.passenger = rs.getString("passenger"); r.age = rs.getInt("age");
                r.gender = rs.getString("gender"); r.trainNo = rs.getString("train_no");
                r.trainName = rs.getString("train_name"); r.classType = rs.getString("class_type");
                r.date = rs.getString("journey_date"); r.source = rs.getString("source");
                r.destination = rs.getString("destination"); r.coach = rs.getString("coach");
                r.seatNo = rs.getString("seat_no"); r.quota = rs.getString("quota");
                r.fare = rs.getDouble("fare"); r.status = rs.getString("status");
                return r;
            }
        } catch (SQLException ignored) {}
        return null;
    }

    static List<Reservation> getUserReservations(String username) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE username=? ORDER BY id DESC";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.pnr = rs.getString("pnr"); r.username = rs.getString("username");
                r.passenger = rs.getString("passenger"); r.age = rs.getInt("age");
                r.gender = rs.getString("gender"); r.trainNo = rs.getString("train_no");
                r.trainName = rs.getString("train_name"); r.classType = rs.getString("class_type");
                r.date = rs.getString("journey_date"); r.source = rs.getString("source");
                r.destination = rs.getString("destination"); r.coach = rs.getString("coach");
                r.seatNo = rs.getString("seat_no"); r.quota = rs.getString("quota");
                r.fare = rs.getDouble("fare"); r.status = rs.getString("status");
                list.add(r);
            }
        } catch (SQLException ignored) {}
        return list;
    }

    static String generatePNR() {
        Random rnd = new Random();
        while (true) {
            String pnr = String.valueOf(1000000000L + (long)(rnd.nextDouble() * 9000000000L));
            if (findReservation(pnr) == null) return pnr;
        }
    }

    static boolean validDate(String value) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            sdf.setLenient(false);
            sdf.parse(value);
            return value.matches("\\d{2}-\\d{2}-\\d{4}");
        } catch (Exception e) { return false; }
    }

    static String liveStatus(TrainInfo train) {
        try {
            String now = new SimpleDateFormat("HH:mm").format(new java.util.Date());
            int current = Integer.parseInt(now.substring(0, 2)) * 60 + Integer.parseInt(now.substring(3));
            int departure = Integer.parseInt(train.departure.substring(0, 2)) * 60 + Integer.parseInt(train.departure.substring(3));
            int diff = departure - current;

            if (Math.abs(diff) <= 5) return "BOARDING";
            if (diff > 0 && diff <= 120) return "DEPARTING SOON";
            return "SCHEDULED";
        } catch (Exception e) { return "SCHEDULED"; }
    }

    // --- COMPREHENSIVE MASTER TRAIN DATASET (50 REAL TRAINS ACROSS INDIA) ---
    static final Map<String, TrainInfo> TRAIN_DATA = createTrainData();

    static Map<String, TrainInfo> createTrainData() {
        Map<String, TrainInfo> m = new LinkedHashMap<>();

        // Vande Bharat Expresses ⚡
        addTrainRoute(m, "20901", "Vande Bharat Express", "Vande Bharat", "06:00", "12:25", "06h 25m", 1420,
            new StationStop("Mumbai Central", 0), new StationStop("Borivali", 30), new StationStop("Vapi", 168),
            new StationStop("Surat", 263), new StationStop("Vadodara", 392), new StationStop("Ahmedabad", 492), new StationStop("Gandhinagar Capital", 520));

        addTrainRoute(m, "22436", "Vande Bharat Express", "Vande Bharat", "06:00", "14:00", "08h 00m", 1750,
            new StationStop("New Delhi", 0), new StationStop("Kanpur Central", 440), new StationStop("Prayagraj Jn", 635), new StationStop("Varanasi Jn", 759));

        addTrainRoute(m, "20607", "Vande Bharat Express", "Vande Bharat", "05:50", "12:20", "06h 30m", 1360,
            new StationStop("MGR Chennai Central", 0), new StationStop("Katpadi Jn", 130), new StationStop("KSR Bengaluru", 362), new StationStop("Mysuru Jn", 500));

        addTrainRoute(m, "20833", "Vande Bharat Express", "Vande Bharat", "05:45", "14:15", "08h 30m", 1620,
            new StationStop("Visakhapatnam", 0), new StationStop("Rajahmundry", 201), new StationStop("Vijayawada", 350), new StationStop("Khammam", 449), new StationStop("Warangal", 556), new StationStop("Secunderabad", 698));

        addTrainRoute(m, "22448", "Vande Bharat Express", "Vande Bharat", "13:00", "18:25", "05h 25m", 1280,
            new StationStop("Amb Andaura", 0), new StationStop("Una Himachal", 27), new StationStop("Anandpur Sahib", 71), new StationStop("Chandigarh", 172), new StationStop("Ambala Cantt", 217), new StationStop("New Delhi", 412));

        addTrainRoute(m, "22895", "Howrah - Puri Vande Bharat", "Vande Bharat", "06:10", "12:35", "06h 25m", 1480,
            new StationStop("Howrah", 0), new StationStop("Kharagpur", 115), new StationStop("Balasore", 231), new StationStop("Cuttack", 409), new StationStop("Bhubaneswar", 437), new StationStop("Puri", 500));

        // Rajdhani Expresses 🚄
        addTrainRoute(m, "12951", "Mumbai Rajdhani Express", "Rajdhani", "17:00", "08:32", "15h 32m", 2450,
            new StationStop("Mumbai Central", 0), new StationStop("Borivali", 30), new StationStop("Surat", 263), new StationStop("Vadodara", 392), new StationStop("Ratlam", 653), new StationStop("Kota", 920), new StationStop("New Delhi", 1386));

        addTrainRoute(m, "12301", "Howrah Rajdhani Express", "Rajdhani", "16:55", "10:00", "17h 05m", 2600,
            new StationStop("Howrah", 0), new StationStop("Asansol", 200), new StationStop("Dhanbad", 259), new StationStop("Gaya", 459), new StationStop("Pt. DD Upadhyaya", 668), new StationStop("Prayagraj", 821), new StationStop("Kanpur Central", 1015), new StationStop("New Delhi", 1447));

        addTrainRoute(m, "12431", "Trivandrum Rajdhani Express", "Rajdhani", "19:15", "12:40", "41h 25m", 4200,
            new StationStop("Thiruvananthapuram", 0), new StationStop("Kollam", 65), new StationStop("Ernakulam", 220), new StationStop("Kozhikode", 413), new StationStop("Mangaluru", 634), new StationStop("Madgaon", 950), new StationStop("Panvel", 1540), new StationStop("Vadodara", 1960), new StationStop("Kota", 2380), new StationStop("Hazrat Nizamuddin", 2848));

        addTrainRoute(m, "12423", "Dibrugarh Rajdhani Express", "Rajdhani", "20:35", "10:55", "38h 20m", 3800,
            new StationStop("Dibrugarh", 0), new StationStop("Guwahati", 560), new StationStop("New Jalpaiguri", 985), new StationStop("Katihar", 1170), new StationStop("Barauni", 1350), new StationStop("Pt. DD Upadhyaya", 1810), new StationStop("New Delhi", 2435));

        addTrainRoute(m, "12437", "Secunderabad Rajdhani Express", "Rajdhani", "12:45", "06:00", "17h 15m", 2550,
            new StationStop("Secunderabad", 0), new StationStop("Kazipet", 132), new StationStop("Balharshah", 367), new StationStop("Nagpur", 575), new StationStop("Bhopal", 965), new StationStop("Hazrat Nizamuddin", 1660));

        // Shatabdi Expresses 🚆
        addTrainRoute(m, "12001", "Bhopal Shatabdi Express", "Shatabdi", "06:00", "14:05", "08h 05m", 1680,
            new StationStop("New Delhi", 0), new StationStop("Mathura Jn", 141), new StationStop("Agra Cantt", 195), new StationStop("Gwalior", 313), new StationStop("VGL Jhansi", 411), new StationStop("Bhopal", 701), new StationStop("Rani Kamlapati", 707));

        addTrainRoute(m, "12004", "Lucknow Shatabdi Express", "Shatabdi", "06:10", "12:40", "06h 30m", 1320,
            new StationStop("New Delhi", 0), new StationStop("Ghaziabad", 25), new StationStop("Aligarh", 131), new StationStop("Tundla", 209), new StationStop("Etawah", 301), new StationStop("Kanpur Central", 440), new StationStop("Lucknow NR", 512));

        addTrainRoute(m, "12007", "Chennai Shatabdi Express", "Shatabdi", "06:00", "13:00", "07h 00m", 1250,
            new StationStop("MGR Chennai Central", 0), new StationStop("Katpadi Jn", 130), new StationStop("KSR Bengaluru", 362), new StationStop("Mysuru Jn", 500));

        addTrainRoute(m, "12011", "Kalka Shatabdi Express", "Shatabdi", "07:40", "11:45", "04h 05m", 980,
            new StationStop("New Delhi", 0), new StationStop("Panipat", 89), new StationStop("Kurukshetra", 156), new StationStop("Ambala Cantt", 199), new StationStop("Chandigarh", 244), new StationStop("Kalka", 269));

        // Superfast & Duronto Expresses 🚈
        addTrainRoute(m, "12723", "Telangana Express", "Superfast", "06:00", "07:40", "25h 40m", 680,
            new StationStop("Hyderabad Deccan", 0), new StationStop("Secunderabad", 9), new StationStop("Kazipet", 141), new StationStop("Ramagundam", 234), new StationStop("Sirpur Kaghaznagar", 306), new StationStop("Balharshah", 376), new StationStop("Nagpur", 585), new StationStop("Bhopal", 975), new StationStop("VGL Jhansi", 1267), new StationStop("Gwalior", 1365), new StationStop("Agra Cantt", 1483), new StationStop("Mathura", 1537), new StationStop("New Delhi", 1677));

        addTrainRoute(m, "12759", "Charminar Express", "Superfast", "18:25", "08:00", "13h 35m", 450,
            new StationStop("Hyderabad Deccan", 0), new StationStop("Secunderabad", 9), new StationStop("Nalgonda", 119), new StationStop("Miryalaguda", 157), new StationStop("Piduguralla", 217), new StationStop("Guntur", 281), new StationStop("Tenali", 306), new StationStop("Ongole", 413), new StationStop("Nellore", 530), new StationStop("Gudur", 568), new StationStop("MGR Chennai Central", 789));

        addTrainRoute(m, "12841", "Coromandel Express", "Superfast", "15:20", "16:50", "25h 30m", 690,
            new StationStop("Howrah", 0), new StationStop("Kharagpur", 115), new StationStop("Balasore", 231), new StationStop("Bhadrak", 294), new StationStop("Cuttack", 409), new StationStop("Bhubaneswar", 437), new StationStop("Khurda Road", 456), new StationStop("Visakhapatnam", 880), new StationStop("Rajahmundry", 1080), new StationStop("Vijayawada", 1229), new StationStop("MGR Chennai Central", 1659));

        addTrainRoute(m, "12626", "Kerala Express", "Superfast", "20:10", "18:00", "45h 50m", 950,
            new StationStop("New Delhi", 0), new StationStop("Mathura", 141), new StationStop("Agra Cantt", 195), new StationStop("Gwalior", 313), new StationStop("VGL Jhansi", 411), new StationStop("Bhopal", 701), new StationStop("Nagpur", 1090), new StationStop("Vijayawada", 1755), new StationStop("Katpadi", 2190), new StationStop("Jolarpettai", 2274), new StationStop("Salem", 2394), new StationStop("Erode", 2454), new StationStop("Coimbatore", 2555), new StationStop("Palakkad", 2611), new StationStop("Thrisur", 2686), new StationStop("Ernakulam", 2760), new StationStop("Kottayam", 2820), new StationStop("Kollam", 2916), new StationStop("Thiruvananthapuram", 3031));

        addTrainRoute(m, "12628", "Karnataka Express", "Superfast", "21:15", "12:00", "38h 45m", 850,
            new StationStop("New Delhi", 0), new StationStop("Mathura", 141), new StationStop("Agra Cantt", 195), new StationStop("Gwalior", 313), new StationStop("VGL Jhansi", 411), new StationStop("Bhopal", 701), new StationStop("Khandwa", 884), new StationStop("Manmad", 1190), new StationStop("Ahmednagar", 1344), new StationStop("Daund", 1428), new StationStop("Solapur", 1615), new StationStop("Kalaburagi", 1728), new StationStop("Raichur", 1872), new StationStop("Guntakal", 1994), new StationStop("Dharmavaram", 2205), new StationStop("KSR Bengaluru", 2407));

        addTrainRoute(m, "12860", "Gitanjali Express", "Superfast", "14:05", "21:20", "31h 15m", 720,
            new StationStop("Howrah", 0), new StationStop("Kharagpur", 115), new StationStop("Tatanagar", 249), new StationStop("Rourkela", 413), new StationStop("Bilaspur", 717), new StationStop("Raipur", 828), new StationStop("Nagpur", 1130), new StationStop("Wardha", 1209), new StationStop("Badnera", 1304), new StationStop("Akola", 1383), new StationStop("Bhusaval", 1523), new StationStop("Nashik Road", 1780), new StationStop("Kalyan", 1912), new StationStop("Mumbai CSMT", 1968));

        addTrainRoute(m, "12393", "Sampoorna Kranti Express", "Superfast", "19:25", "07:55", "12h 30m", 510,
            new StationStop("Rajendra Nagar T", 0), new StationStop("Patna Jn", 3), new StationStop("Pt. DD Upadhyaya", 215), new StationStop("Kanpur Central", 562), new StationStop("New Delhi", 1001));

        addTrainRoute(m, "12259", "Sealdah Duronto Express", "Duronto", "17:00", "18:20", "25h 20m", 2100,
            new StationStop("Sealdah", 0), new StationStop("Dhanbad", 266), new StationStop("Pt. DD Upadhyaya", 675), new StationStop("Kanpur Central", 1020), new StationStop("New Delhi", 1450), new StationStop("Bikaner Jn", 1923));

        addTrainRoute(m, "12123", "Deccan Queen Express", "Superfast", "17:10", "20:25", "03h 15m", 350,
            new StationStop("Mumbai CSMT", 0), new StationStop("Karjat", 100), new StationStop("Lonavala", 128), new StationStop("Shivajinagar", 189), new StationStop("Pune Jn", 192));

        addTrainRoute(m, "12616", "Grand Trunk Express", "Superfast", "18:40", "04:30", "33h 50m", 820,
            new StationStop("New Delhi", 0), new StationStop("Agra Cantt", 195), new StationStop("Gwalior", 313), new StationStop("VGL Jhansi", 411), new StationStop("Bhopal", 701), new StationStop("Nagpur", 1090), new StationStop("Warangal", 1665), new StationStop("Vijayawada", 1872), new StationStop("MGR Chennai Central", 2182));

        return m;
    }

    static void addTrainRoute(Map<String, TrainInfo> m, String no, String name, String type, String dep, String arr, String duration, double baseFare, StationStop... stops) {
        List<StationStop> routeStops = Arrays.asList(stops);
        String from = routeStops.get(0).stationName;
        String to = routeStops.get(routeStops.size() - 1).stationName;
        int distance = routeStops.get(routeStops.size() - 1).distance;
        m.put(no, new TrainInfo(no, name, type, from, to, dep, arr, duration, distance, baseFare, routeStops));
    }

    static TrainInfo getTrain(String number) {
        return TRAIN_DATA.get(number);
    }

    static class StationStop {
        String stationName;
        int distance;
        StationStop(String stationName, int distance) {
            this.stationName = stationName;
            this.distance = distance;
        }
    }

    static class TrainInfo {
        String number, name, type, from, to, departure, arrival, duration;
        int distance;
        double baseFare;
        List<StationStop> routeStops;

        TrainInfo(String number, String name, String type, String from, String to, String departure, String arrival, String duration, int distance, double baseFare, List<StationStop> routeStops) {
            this.number = number; this.name = name; this.type = type;
            this.from = from; this.to = to; this.departure = departure;
            this.arrival = arrival; this.duration = duration;
            this.distance = distance; this.baseFare = baseFare;
            this.routeStops = routeStops;
        }
    }

    static class Reservation {
        String pnr, username, passenger, gender, trainNo, trainName, classType, date, source, destination, coach, seatNo, quota, status;
        int age;
        double fare;
    }

    static class SimpleDocumentListener implements DocumentListener {
        Runnable action;
        SimpleDocumentListener(Runnable action) { this.action = action; }
        public void insertUpdate(DocumentEvent e) { action.run(); }
        public void removeUpdate(DocumentEvent e) { action.run(); }
        public void changedUpdate(DocumentEvent e) { action.run(); }
    }
}