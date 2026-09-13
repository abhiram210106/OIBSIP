package com.library.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.library.dao.*;
import com.library.models.Book;
import com.library.models.User;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final IssueDAO issueDAO = new IssueDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final QueryDAO queryDAO = new QueryDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            HttpHelper.sendCorsHeaders(exchange);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = HttpHelper.parseQueryParams(query);

        try {
            if (path.startsWith("/api/auth/")) {
                handleAuth(exchange, path, method);
            } else if (path.equals("/api/books/categories")) {
                HttpHelper.sendJsonResponse(exchange, 200, bookDAO.getCategories());
            } else if (path.startsWith("/api/books")) {
                handleBooks(exchange, path, method, queryParams);
            } else if (path.startsWith("/api/issues")) {
                handleIssues(exchange, path, method, queryParams);
            } else if (path.startsWith("/api/reservations")) {
                handleReservations(exchange, path, method, queryParams);
            } else if (path.startsWith("/api/queries")) {
                handleQueries(exchange, path, method, queryParams);
            } else if (path.startsWith("/api/users")) {
                handleUsers(exchange, path, method, queryParams);
            } else if (path.equals("/api/stats")) {
                HttpHelper.sendJsonResponse(exchange, 200, issueDAO.getSystemStats());
            } else {
                Map<String, String> err = new HashMap<>();
                err.put("error", "Endpoint not found: " + path);
                HttpHelper.sendJsonResponse(exchange, 404, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> err = new HashMap<>();
            err.put("error", "Internal server error: " + e.getMessage());
            HttpHelper.sendJsonResponse(exchange, 500, err);
        }
    }

    private void handleAuth(HttpExchange exchange, String path, String method) throws IOException {
        if ("/api/auth/login".equals(path) && "POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String username = json.has("username") ? json.get("username").getAsString() : "";
            String password = json.has("password") ? json.get("password").getAsString() : "";

            User user = userDAO.authenticate(username, password);
            Map<String, Object> resp = new HashMap<>();
            if (user != null) {
                resp.put("success", true);
                resp.put("user", user);
                resp.put("message", "Welcome back, " + user.getFullName() + "!");
                HttpHelper.sendJsonResponse(exchange, 200, resp);
            } else {
                resp.put("success", false);
                resp.put("message", "Invalid username or password.");
                HttpHelper.sendJsonResponse(exchange, 401, resp);
            }
        } else if ("/api/auth/register".equals(path) && "POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            User user = new User();
            user.setUsername(json.has("username") ? json.get("username").getAsString() : "");
            user.setPassword(json.has("password") ? json.get("password").getAsString() : "");
            user.setFullName(json.has("fullName") ? json.get("fullName").getAsString() : "");
            user.setEmail(json.has("email") ? json.get("email").getAsString() : "");
            user.setPhone(json.has("phone") ? json.get("phone").getAsString() : "");
            user.setRole("USER");

            boolean ok = userDAO.register(user);
            Map<String, Object> resp = new HashMap<>();
            if (ok) {
                User saved = userDAO.authenticate(user.getUsername(), user.getPassword());
                resp.put("success", true);
                resp.put("user", saved);
                resp.put("message", "Registration successful! You can now browse and issue books.");
                HttpHelper.sendJsonResponse(exchange, 201, resp);
            } else {
                resp.put("success", false);
                resp.put("message", "Username or email already exists.");
                HttpHelper.sendJsonResponse(exchange, 400, resp);
            }
        }
    }

    private void handleBooks(HttpExchange exchange, String path, String method, Map<String, String> queryParams) throws IOException {
        if ("/api/books".equals(path)) {
            if ("GET".equals(method)) {
                String category = queryParams.get("category");
                String search = queryParams.get("search");
                HttpHelper.sendJsonResponse(exchange, 200, bookDAO.getAllBooks(category, search));
            } else if ("POST".equals(method)) {
                // Add new book (Admin)
                Book book = HttpHelper.parseJsonBody(exchange, Book.class);
                if (book != null && bookDAO.addBook(book)) {
                    Map<String, Object> resp = Map.of("success", true, "message", "Book added to catalogue successfully!");
                    HttpHelper.sendJsonResponse(exchange, 201, resp);
                } else {
                    Map<String, Object> resp = Map.of("success", false, "message", "Failed to add book. ISBN may already exist.");
                    HttpHelper.sendJsonResponse(exchange, 400, resp);
                }
            } else if ("PUT".equals(method)) {
                // Edit book (Admin)
                Book book = HttpHelper.parseJsonBody(exchange, Book.class);
                if (book != null && bookDAO.updateBook(book)) {
                    Map<String, Object> resp = Map.of("success", true, "message", "Book updated successfully!");
                    HttpHelper.sendJsonResponse(exchange, 200, resp);
                } else {
                    Map<String, Object> resp = Map.of("success", false, "message", "Failed to update book.");
                    HttpHelper.sendJsonResponse(exchange, 400, resp);
                }
            }
        } else if (path.startsWith("/api/books/")) {
            // Specific book ID
            String idStr = path.substring("/api/books/".length());
            try {
                int id = Integer.parseInt(idStr);
                if ("GET".equals(method)) {
                    Book book = bookDAO.getBookById(id);
                    if (book != null) {
                        HttpHelper.sendJsonResponse(exchange, 200, book);
                    } else {
                        HttpHelper.sendJsonResponse(exchange, 404, Map.of("error", "Book not found"));
                    }
                } else if ("DELETE".equals(method)) {
                    // Delete book record (Admin)
                    boolean ok = bookDAO.deleteBook(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok, "message", ok ? "Book deleted." : "Failed to delete."));
                }
            } catch (NumberFormatException e) {
                HttpHelper.sendJsonResponse(exchange, 400, Map.of("error", "Invalid book ID"));
            }
        }
    }

    private void handleIssues(HttpExchange exchange, String path, String method, Map<String, String> queryParams) throws IOException {
        if ("/api/issues".equals(path) && "GET".equals(method)) {
            String userIdStr = queryParams.get("userId");
            String status = queryParams.get("status");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                int userId = Integer.parseInt(userIdStr);
                HttpHelper.sendJsonResponse(exchange, 200, issueDAO.getIssuesByUser(userId));
            } else {
                HttpHelper.sendJsonResponse(exchange, 200, issueDAO.getAllIssues(status));
            }
        } else if ("/api/issues/issue".equals(path) && "POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            int userId = json.get("userId").getAsInt();
            int bookId = json.get("bookId").getAsInt();
            int days = json.has("days") ? json.get("days").getAsInt() : 14;

            Map<String, Object> res = issueDAO.issueBook(userId, bookId, days);
            int code = (Boolean) res.get("success") ? 200 : 400;
            HttpHelper.sendJsonResponse(exchange, code, res);
        } else if ("/api/issues/return".equals(path) && "POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            int issueId = json.get("issueId").getAsInt();

            Map<String, Object> res = issueDAO.returnBook(issueId);
            int code = (Boolean) res.get("success") ? 200 : 400;
            HttpHelper.sendJsonResponse(exchange, code, res);
        } else if ("/api/issues/pay-fine".equals(path) && "POST".equals(method)) {
            String body = HttpHelper.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            int issueId = json.get("issueId").getAsInt();

            boolean ok = issueDAO.markFinePaid(issueId);
            HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok, "message", ok ? "Fine marked as paid." : "Failed to update fine."));
        }
    }

    private void handleReservations(HttpExchange exchange, String path, String method, Map<String, String> queryParams) throws IOException {
        if ("/api/reservations".equals(path)) {
            if ("GET".equals(method)) {
                String userIdStr = queryParams.get("userId");
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    int userId = Integer.parseInt(userIdStr);
                    HttpHelper.sendJsonResponse(exchange, 200, reservationDAO.getReservationsByUser(userId));
                } else {
                    HttpHelper.sendJsonResponse(exchange, 200, reservationDAO.getAllReservations());
                }
            } else if ("POST".equals(method)) {
                String body = HttpHelper.readRequestBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                int bookId = json.get("bookId").getAsInt();
                int userId = json.get("userId").getAsInt();

                Map<String, Object> res = reservationDAO.createReservation(bookId, userId);
                int code = (Boolean) res.get("success") ? 200 : 400;
                HttpHelper.sendJsonResponse(exchange, code, res);
            }
        } else if (path.startsWith("/api/reservations/")) {
            String idStr = path.substring("/api/reservations/".length());
            try {
                int id = Integer.parseInt(idStr);
                if ("DELETE".equals(method)) {
                    boolean ok = reservationDAO.cancelReservation(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                } else if ("PUT".equals(method)) {
                    boolean ok = reservationDAO.fulfillReservation(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                }
            } catch (NumberFormatException e) {
                HttpHelper.sendJsonResponse(exchange, 400, Map.of("error", "Invalid reservation ID"));
            }
        }
    }

    private void handleQueries(HttpExchange exchange, String path, String method, Map<String, String> queryParams) throws IOException {
        if ("/api/queries".equals(path)) {
            if ("GET".equals(method)) {
                HttpHelper.sendJsonResponse(exchange, 200, queryDAO.getAllQueries());
            } else if ("POST".equals(method)) {
                String body = HttpHelper.readRequestBody(exchange);
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String name = json.get("name").getAsString();
                String email = json.get("email").getAsString();
                String subject = json.get("subject").getAsString();
                String message = json.get("message").getAsString();

                boolean ok = queryDAO.createQuery(name, email, subject, message);
                HttpHelper.sendJsonResponse(exchange, ok ? 201 : 400, Map.of("success", ok, "message", ok ? "Your query has been submitted to the library administration." : "Failed to submit."));
            }
        } else if (path.startsWith("/api/queries/")) {
            String idStr = path.substring("/api/queries/".length());
            try {
                int id = Integer.parseInt(idStr);
                if ("PUT".equals(method)) {
                    String body = HttpHelper.readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String reply = json.has("reply") ? json.get("reply").getAsString() : "";
                    boolean ok = queryDAO.replyQuery(id, reply);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                } else if ("DELETE".equals(method)) {
                    boolean ok = queryDAO.deleteQuery(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                }
            } catch (NumberFormatException e) {
                HttpHelper.sendJsonResponse(exchange, 400, Map.of("error", "Invalid query ID"));
            }
        }
    }

    private void handleUsers(HttpExchange exchange, String path, String method, Map<String, String> queryParams) throws IOException {
        if ("/api/users".equals(path) && "GET".equals(method)) {
            HttpHelper.sendJsonResponse(exchange, 200, userDAO.getAllUsers());
        } else if (path.startsWith("/api/users/")) {
            String idStr = path.substring("/api/users/".length());
            try {
                int id = Integer.parseInt(idStr);
                if ("DELETE".equals(method)) {
                    boolean ok = userDAO.deleteUser(id);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                } else if ("PUT".equals(method)) {
                    String body = HttpHelper.readRequestBody(exchange);
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String role = json.get("role").getAsString();
                    boolean ok = userDAO.updateUserRole(id, role);
                    HttpHelper.sendJsonResponse(exchange, 200, Map.of("success", ok));
                }
            } catch (NumberFormatException e) {
                HttpHelper.sendJsonResponse(exchange, 400, Map.of("error", "Invalid user ID"));
            }
        }
    }
}
