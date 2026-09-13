package com.library.database;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:library.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Enable WAL mode for better concurrency
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    phone TEXT,
                    created_at TEXT NOT NULL
                );
            """);

            // 2. Books table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS books (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    author TEXT NOT NULL,
                    isbn TEXT UNIQUE NOT NULL,
                    category TEXT NOT NULL,
                    total_quantity INTEGER NOT NULL DEFAULT 1,
                    available_quantity INTEGER NOT NULL DEFAULT 1,
                    cover_url TEXT,
                    description TEXT,
                    published_year TEXT,
                    rating REAL DEFAULT 4.5
                );
            """);

            // 3. Issues table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS issues (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    issue_date TEXT NOT NULL,
                    due_date TEXT NOT NULL,
                    return_date TEXT,
                    fine_amount REAL DEFAULT 0.0,
                    fine_paid INTEGER DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'ISSUED',
                    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 4. Reservations table (advance booking)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    book_id INTEGER NOT NULL,
                    user_id INTEGER NOT NULL,
                    reservation_date TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 5. Contact Queries table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS queries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    message TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'PENDING',
                    admin_reply TEXT,
                    created_at TEXT NOT NULL
                );
            """);

            // Seed data if empty
            seedInitialData(conn);

            System.out.println("Database tables checked and initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialData(Connection conn) throws SQLException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        // Check if users exist
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding default users...");
                String insertUser = "INSERT INTO users (username, password, full_name, email, role, phone, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                    // Admin
                    ps.setString(1, "admin");
                    ps.setString(2, "admin123");
                    ps.setString(3, "Central Library Administrator");
                    ps.setString(4, "admin@digitallibrary.org");
                    ps.setString(5, "ADMIN");
                    ps.setString(6, "+91 98765 00000");
                    ps.setString(7, now.minusMonths(2).format(dtf));
                    ps.executeUpdate();

                    // Student 1
                    ps.setString(1, "student1");
                    ps.setString(2, "user123");
                    ps.setString(3, "Aarav Sharma");
                    ps.setString(4, "aarav.sharma@campus.edu");
                    ps.setString(5, "USER");
                    ps.setString(6, "+91 98765 43210");
                    ps.setString(7, now.minusMonths(1).format(dtf));
                    ps.executeUpdate();

                    // Student 2
                    ps.setString(1, "student2");
                    ps.setString(2, "user123");
                    ps.setString(3, "Priya Patel");
                    ps.setString(4, "priya.patel@campus.edu");
                    ps.setString(5, "USER");
                    ps.setString(6, "+91 98765 43211");
                    ps.setString(7, now.minusDays(20).format(dtf));
                    ps.executeUpdate();

                    // Student 3
                    ps.setString(1, "student3");
                    ps.setString(2, "user123");
                    ps.setString(3, "Rahul Verma");
                    ps.setString(4, "rahul.verma@campus.edu");
                    ps.setString(5, "USER");
                    ps.setString(6, "+91 98765 43212");
                    ps.setString(7, now.minusDays(10).format(dtf));
                    ps.executeUpdate();
                }
            }
        }

        // Check if books exist
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM books")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding library books catalogue...");
                String insertBook = "INSERT INTO books (title, author, isbn, category, total_quantity, available_quantity, cover_url, description, published_year, rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertBook)) {
                    Object[][] bookList = {
                        {"Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", "978-0132350884", "Computer Science", 4, 3, "https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?w=400&q=80", "Even bad code can function. But if code isn't clean, it can bring a development organization to its knees.", "2008", 4.8},
                        {"Introduction to Algorithms (CLRS 4th Ed)", "Thomas H. Cormen et al.", "978-0262046305", "Algorithms & DS", 3, 2, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80", "Comprehensive textbook covering modern algorithms, graph theory, dynamic programming and NP-completeness.", "2022", 4.9},
                        {"Designing Data-Intensive Applications", "Martin Kleppmann", "978-1449373320", "Distributed Systems", 5, 4, "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=400&q=80", "The definitive guide to the principles and architectures of scalable, reliable, and maintainable systems.", "2017", 4.9},
                        {"Artificial Intelligence: A Modern Approach", "Stuart Russell, Peter Norvig", "978-0136042594", "Artificial Intelligence", 2, 0, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80", "The leading textbook in AI worldwide, exploring search, knowledge representation, reasoning, and machine learning.", "2020", 4.7},
                        {"Python Crash Course (3rd Edition)", "Eric Matthes", "978-1593279288", "Programming", 6, 5, "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=400&q=80", "A fast-paced, thorough introduction to programming with Python that will have you writing programs in no time.", "2023", 4.7},
                        {"Deep Learning", "Ian Goodfellow, Yoshua Bengio", "978-0262035613", "Artificial Intelligence", 3, 2, "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=400&q=80", "An introduction to a broad range of topics in deep learning including neural nets, backprop, CNNs, and generative models.", "2016", 4.6},
                        {"Database System Concepts", "Abraham Silberschatz", "978-0078022159", "Database Systems", 4, 4, "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400&q=80", "Presents the fundamental concepts of database management, SQL, relational calculus, indexing, and recovery.", "2019", 4.5},
                        {"The Pragmatic Programmer: Your Journey to Mastery", "David Thomas, Andrew Hunt", "978-0135957059", "Software Engineering", 4, 3, "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&q=80", "One of the most significant books on programming philosophy, pragmatic thinking, career development and craft.", "2019", 4.9},
                        {"Atomic Habits", "James Clear", "978-0735211292", "Self Development", 5, 4, "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&q=80", "An easy & proven way to build good habits and break bad ones through tiny behavioral compound changes.", "2018", 4.8},
                        {"Operating System Concepts", "Silberschatz, Galvin, Gagne", "978-1118063330", "Operating Systems", 3, 3, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&q=80", "Provides a clear description of the concepts that underlie modern operating systems, processes, memory and I/O.", "2018", 4.6},
                        {"Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "978-0062316097", "History & Science", 4, 4, "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=400&q=80", "Explores how an insignificant ape became the ruler of planet Earth, covering cognition, agriculture, and science.", "2015", 4.7},
                        {"Zero to One: Notes on Startups", "Peter Thiel", "978-0804139298", "Business & Startups", 5, 5, "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=400&q=80", "How to build companies that create new things, moving from zero to one rather than copying from 1 to n.", "2014", 4.6}
                    };

                    for (Object[] b : bookList) {
                        ps.setString(1, (String) b[0]);
                        ps.setString(2, (String) b[1]);
                        ps.setString(3, (String) b[2]);
                        ps.setString(4, (String) b[3]);
                        ps.setInt(5, (Integer) b[4]);
                        ps.setInt(6, (Integer) b[5]);
                        ps.setString(7, (String) b[6]);
                        ps.setString(8, (String) b[7]);
                        ps.setString(9, (String) b[8]);
                        ps.setDouble(10, (Double) b[9]);
                        ps.executeUpdate();
                    }
                }
            }
        }

        // Check if issues exist
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM issues")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding sample issue & fine records...");
                String insertIssue = "INSERT INTO issues (book_id, user_id, issue_date, due_date, return_date, fine_amount, fine_paid, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertIssue)) {
                    // Overdue issue for student1 (6 days overdue -> ₹30 fine)
                    ps.setInt(1, 1); // Clean Code
                    ps.setInt(2, 2); // Aarav (student1)
                    ps.setString(3, now.minusDays(20).format(dtf));
                    ps.setString(4, now.minusDays(6).format(dtf)); // Due 6 days ago!
                    ps.setString(5, null);
                    ps.setDouble(6, 30.0); // 6 days * ₹5 = ₹30 fine
                    ps.setInt(7, 0); // Not paid
                    ps.setString(8, "OVERDUE");
                    ps.executeUpdate();

                    // Normal active issue for student1
                    ps.setInt(1, 2); // CLRS
                    ps.setInt(2, 2); // Aarav (student1)
                    ps.setString(3, now.minusDays(4).format(dtf));
                    ps.setString(4, now.plusDays(10).format(dtf));
                    ps.setString(5, null);
                    ps.setDouble(6, 0.0);
                    ps.setInt(7, 1);
                    ps.setString(8, "ISSUED");
                    ps.executeUpdate();

                    // Issued issue for student2 that made AI book have 0 copies
                    ps.setInt(1, 4); // AI: A Modern Approach (all 2 copies out)
                    ps.setInt(2, 3); // Priya (student2)
                    ps.setString(3, now.minusDays(2).format(dtf));
                    ps.setString(4, now.plusDays(12).format(dtf));
                    ps.setString(5, null);
                    ps.setDouble(6, 0.0);
                    ps.setInt(7, 1);
                    ps.setString(8, "ISSUED");
                    ps.executeUpdate();

                    // Past returned issue with fine paid
                    ps.setInt(1, 5); // Python Crash Course
                    ps.setInt(2, 4); // Rahul (student3)
                    ps.setString(3, now.minusDays(30).format(dtf));
                    ps.setString(4, now.minusDays(16).format(dtf));
                    ps.setString(5, now.minusDays(14).format(dtf)); // returned 2 days late
                    ps.setDouble(6, 10.0); // 2 * ₹5 = ₹10
                    ps.setInt(7, 1); // Paid
                    ps.setString(8, "RETURNED");
                    ps.executeUpdate();
                }
            }
        }

        // Check if reservations exist
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM reservations")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding advance booking reservation...");
                String insertRes = "INSERT INTO reservations (book_id, user_id, reservation_date, status) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertRes)) {
                    ps.setInt(1, 4); // AI book (0 available)
                    ps.setInt(2, 4); // Rahul (student3) reserved it
                    ps.setString(3, now.minusDays(1).format(dtf));
                    ps.setString(4, "PENDING");
                    ps.executeUpdate();
                }
            }
        }

        // Check if queries exist
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM queries")) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding sample contact query...");
                String insertQ = "INSERT INTO queries (name, email, subject, message, status, admin_reply, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertQ)) {
                    ps.setString(1, "Priya Patel");
                    ps.setString(2, "priya.patel@campus.edu");
                    ps.setString(3, "Request for IEEE Xplore digital journals access");
                    ps.setString(4, "Hello, can the library provide institutional login credentials or VPN access for IEEE digital library transactions for our AI seminar?");
                    ps.setString(5, "PENDING");
                    ps.setString(6, null);
                    ps.setString(7, now.minusDays(2).format(dtf));
                    ps.executeUpdate();
                }
            }
        }
    }
}
