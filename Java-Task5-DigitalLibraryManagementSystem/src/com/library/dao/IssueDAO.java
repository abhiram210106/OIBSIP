package com.library.dao;

import com.library.database.DatabaseManager;
import com.library.models.BookIssue;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class IssueDAO {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final double FINE_RATE_PER_DAY = 5.0; // ₹5 per day

    public IssueDAO() {
        refreshOverdueStatus();
    }

    /**
     * Refresh overdue statuses and recalculate fines for all active issues.
     */
    public synchronized void refreshOverdueStatus() {
        LocalDate today = LocalDate.now();
        String sql = "SELECT id, due_date FROM issues WHERE status = 'ISSUED' OR status = 'OVERDUE'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<int[]> updates = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String dueDateStr = rs.getString("due_date");
                if (dueDateStr != null) {
                    try {
                        LocalDate dueDate = LocalDate.parse(dueDateStr, DTF);
                        if (today.isAfter(dueDate)) {
                            long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                            double fine = overdueDays * FINE_RATE_PER_DAY;
                            updates.add(new int[]{id, (int) overdueDays, (int) fine});
                        }
                    } catch (Exception ignored) {}
                }
            }

            if (!updates.isEmpty()) {
                String updateSql = "UPDATE issues SET status = 'OVERDUE', fine_amount = ? WHERE id = ? AND fine_paid = 0";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    for (int[] up : updates) {
                        ps.setDouble(1, (double) up[2]);
                        ps.setInt(2, up[0]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized Map<String, Object> issueBook(int userId, int bookId, int borrowDays) {
        Map<String, Object> result = new HashMap<>();
        if (borrowDays <= 0) borrowDays = 14; // standard 2 weeks

        // 1. Check book availability
        BookDAO bookDAO = new BookDAO();
        var book = bookDAO.getBookById(bookId);
        if (book == null) {
            result.put("success", false);
            result.put("message", "Book not found.");
            return result;
        }

        if (book.getAvailableQuantity() <= 0) {
            result.put("success", false);
            result.put("message", "No copies currently available. You can place an Advance Reservation.");
            return result;
        }

        // 2. Check if user already has an active issue of this book
        String checkSql = "SELECT COUNT(*) FROM issues WHERE user_id = ? AND book_id = ? AND status IN ('ISSUED', 'OVERDUE')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    result.put("success", false);
                    result.put("message", "You already have an active borrowed copy of this book.");
                    return result;
                }
            }
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
            return result;
        }

        // 3. Issue the book
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(borrowDays);

        String insertSql = "INSERT INTO issues (book_id, user_id, issue_date, due_date, return_date, fine_amount, fine_paid, status) VALUES (?, ?, ?, ?, NULL, 0.0, 1, 'ISSUED')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setString(3, issueDate.format(DTF));
            ps.setString(4, dueDate.format(DTF));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                // Decrement book available quantity
                bookDAO.decrementAvailable(bookId);

                int issueId = 0;
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) issueId = gk.getInt(1);
                }

                result.put("success", true);
                result.put("issueId", issueId);
                result.put("issueDate", issueDate.format(DTF));
                result.put("dueDate", dueDate.format(DTF));
                result.put("message", "Book issued successfully! Due date is " + dueDate.format(DTF) + ".");
                return result;
            }
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Failed to issue book: " + e.getMessage());
            return result;
        }

        result.put("success", false);
        result.put("message", "Could not complete transaction.");
        return result;
    }

    public synchronized Map<String, Object> returnBook(int issueId) {
        Map<String, Object> result = new HashMap<>();
        String query = "SELECT i.*, b.id as b_id, b.title as b_title FROM issues i JOIN books b ON i.book_id = b.id WHERE i.id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    result.put("success", false);
                    result.put("message", "Issue record not found.");
                    return result;
                }

                String currentStatus = rs.getString("status");
                if ("RETURNED".equalsIgnoreCase(currentStatus)) {
                    result.put("success", false);
                    result.put("message", "This book was already marked as returned.");
                    return result;
                }

                int bookId = rs.getInt("b_id");
                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = LocalDate.parse(dueDateStr, DTF);
                LocalDate returnDate = LocalDate.now();

                long overdueDays = 0;
                double fine = 0.0;
                if (returnDate.isAfter(dueDate)) {
                    overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                    fine = overdueDays * FINE_RATE_PER_DAY;
                }

                // Check if existing fine was already paid
                int finePaid = rs.getInt("fine_paid");
                if (fine == 0.0) {
                    finePaid = 1;
                }

                // Update issue record
                String updateSql = "UPDATE issues SET return_date = ?, fine_amount = ?, fine_paid = ?, status = 'RETURNED' WHERE id = ?";
                try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                    updatePs.setString(1, returnDate.format(DTF));
                    updatePs.setDouble(2, fine);
                    updatePs.setInt(3, finePaid);
                    updatePs.setInt(4, issueId);
                    updatePs.executeUpdate();
                }

                // Increment book available quantity
                BookDAO bookDAO = new BookDAO();
                bookDAO.incrementAvailable(bookId);

                // Check if any reservation is pending for this book
                ReservationDAO resDAO = new ReservationDAO();
                var pendingReservations = resDAO.getPendingForBook(bookId);
                boolean reservationNotified = false;
                if (!pendingReservations.isEmpty()) {
                    reservationNotified = true;
                }

                result.put("success", true);
                result.put("returnDate", returnDate.format(DTF));
                result.put("overdueDays", overdueDays);
                result.put("fineAmount", fine);
                result.put("finePaid", finePaid == 1);
                result.put("reservationAlert", reservationNotified ? "Book is now reserved for the next waiting member." : null);
                result.put("message", overdueDays > 0 
                    ? ("Book returned with " + overdueDays + " overdue day(s). Generated fine: ₹" + fine + ".")
                    : "Book returned on time! No overdue fine.");
                return result;
            }
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Error returning book: " + e.getMessage());
            return result;
        }
    }

    public boolean markFinePaid(int issueId) {
        String sql = "UPDATE issues SET fine_paid = 1 WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, issueId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BookIssue> getIssuesByUser(int userId) {
        refreshOverdueStatus();
        List<BookIssue> list = new ArrayList<>();
        String sql = """
            SELECT i.*, b.title as book_title, b.author as book_author, b.isbn as book_isbn, b.cover_url as book_cover,
                   u.full_name as user_name, u.email as user_email
            FROM issues i
            JOIN books b ON i.book_id = b.id
            JOIN users u ON i.user_id = u.id
            WHERE i.user_id = ?
            ORDER BY i.id DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIssue(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookIssue> getAllIssues(String statusFilter) {
        refreshOverdueStatus();
        List<BookIssue> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT i.*, b.title as book_title, b.author as book_author, b.isbn as book_isbn, b.cover_url as book_cover,
                   u.full_name as user_name, u.email as user_email
            FROM issues i
            JOIN books b ON i.book_id = b.id
            JOIN users u ON i.user_id = u.id
        """);

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
            sql.append(" WHERE i.status = ? ");
        }
        sql.append(" ORDER BY i.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("ALL")) {
                ps.setString(1, statusFilter.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIssue(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Object> getSystemStats() {
        refreshOverdueStatus();
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Total books & total copies
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*), SUM(total_quantity), SUM(available_quantity) FROM books")) {
                if (rs.next()) {
                    stats.put("totalBookTitles", rs.getInt(1));
                    stats.put("totalPhysicalCopies", rs.getInt(2));
                    stats.put("availableCopies", rs.getInt(3));
                }
            }

            // Total members
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role = 'USER'")) {
                if (rs.next()) {
                    stats.put("totalMembers", rs.getInt(1));
                }
            }

            // Active issues
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM issues WHERE status IN ('ISSUED', 'OVERDUE')")) {
                if (rs.next()) {
                    stats.put("activeIssues", rs.getInt(1));
                }
            }

            // Overdue issues
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM issues WHERE status = 'OVERDUE'")) {
                if (rs.next()) {
                    stats.put("overdueIssues", rs.getInt(1));
                }
            }

            // Fines: pending and collected
            try (ResultSet rs = stmt.executeQuery("SELECT SUM(fine_amount) FROM issues WHERE fine_paid = 1 AND fine_amount > 0")) {
                if (rs.next()) {
                    stats.put("finesCollected", rs.getDouble(1));
                }
            }
            try (ResultSet rs = stmt.executeQuery("SELECT SUM(fine_amount) FROM issues WHERE fine_paid = 0 AND fine_amount > 0")) {
                if (rs.next()) {
                    stats.put("finesPending", rs.getDouble(1));
                }
            }

            // Total reservations
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM reservations WHERE status = 'PENDING'")) {
                if (rs.next()) {
                    stats.put("pendingReservations", rs.getInt(1));
                }
            }

            // Total queries
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM queries WHERE status = 'PENDING'")) {
                if (rs.next()) {
                    stats.put("pendingQueries", rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    private BookIssue mapIssue(ResultSet rs) throws SQLException {
        BookIssue issue = new BookIssue(
            rs.getInt("id"),
            rs.getInt("book_id"),
            rs.getInt("user_id"),
            rs.getString("issue_date"),
            rs.getString("due_date"),
            rs.getString("return_date"),
            rs.getDouble("fine_amount"),
            rs.getInt("fine_paid") == 1,
            rs.getString("status")
        );
        issue.setBookTitle(rs.getString("book_title"));
        issue.setBookAuthor(rs.getString("book_author"));
        issue.setBookIsbn(rs.getString("book_isbn"));
        issue.setBookCover(rs.getString("book_cover"));
        issue.setUserName(rs.getString("user_name"));
        issue.setUserEmail(rs.getString("user_email"));
        return issue;
    }
}
