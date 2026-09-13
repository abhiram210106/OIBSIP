package com.library.dao;

import com.library.database.DatabaseManager;
import com.library.models.Reservation;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationDAO {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Map<String, Object> createReservation(int bookId, int userId) {
        Map<String, Object> result = new HashMap<>();

        // Check if user already has a pending reservation for this book
        String checkSql = "SELECT COUNT(*) FROM reservations WHERE book_id = ? AND user_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    result.put("success", false);
                    result.put("message", "You already have an active advance reservation for this title.");
                    return result;
                }
            }
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Database error: " + e.getMessage());
            return result;
        }

        // Insert reservation
        String insertSql = "INSERT INTO reservations (book_id, user_id, reservation_date, status) VALUES (?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookId);
            ps.setInt(2, userId);
            ps.setString(3, LocalDate.now().format(DTF));
            int affected = ps.executeUpdate();
            if (affected > 0) {
                int resId = 0;
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) resId = gk.getInt(1);
                }
                result.put("success", true);
                result.put("reservationId", resId);
                result.put("message", "Advance booking confirmed! You will be notified once a copy is returned.");
                return result;
            }
        } catch (SQLException e) {
            result.put("success", false);
            result.put("message", "Failed to reserve book: " + e.getMessage());
            return result;
        }

        result.put("success", false);
        result.put("message", "Could not create reservation.");
        return result;
    }

    public List<Reservation> getReservationsByUser(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.title as book_title, b.author as book_author, b.cover_url as book_cover,
                   u.full_name as user_name, u.email as user_email
            FROM reservations r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.user_id = u.id
            WHERE r.user_id = ?
            ORDER BY r.id DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = """
            SELECT r.*, b.title as book_title, b.author as book_author, b.cover_url as book_cover,
                   u.full_name as user_name, u.email as user_email
            FROM reservations r
            JOIN books b ON r.book_id = b.id
            JOIN users u ON r.user_id = u.id
            ORDER BY r.id DESC
        """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reservation> getPendingForBook(int bookId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE book_id = ? AND status = 'PENDING' ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reservation r = new Reservation();
                    r.setId(rs.getInt("id"));
                    r.setBookId(rs.getInt("book_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setReservationDate(rs.getString("reservation_date"));
                    r.setStatus(rs.getString("status"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelReservation(int id) {
        String sql = "UPDATE reservations SET status = 'CANCELLED' WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean fulfillReservation(int id) {
        String sql = "UPDATE reservations SET status = 'FULFILLED' WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation(
            rs.getInt("id"),
            rs.getInt("book_id"),
            rs.getInt("user_id"),
            rs.getString("reservation_date"),
            rs.getString("status")
        );
        r.setBookTitle(rs.getString("book_title"));
        r.setBookAuthor(rs.getString("book_author"));
        r.setBookCover(rs.getString("book_cover"));
        r.setUserName(rs.getString("user_name"));
        r.setUserEmail(rs.getString("user_email"));
        return r;
    }
}
