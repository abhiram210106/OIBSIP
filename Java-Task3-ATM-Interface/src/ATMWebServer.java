import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ATMWebServer embeds a lightweight HTTP server using standard Java.
 * Bridges the Indian Commercial Bank core (Bank, Account, Transaction) with
 * the official NetBanking & ATM Web Portal.
 */
public class ATMWebServer {
    private static final int PORT = 8080;
    private static Bank bank;
    private static Account currentAccount = null;

    public static void main(String[] args) throws IOException {
        bank = new Bank("Oasis National Bank of India", "ONBI", "ONBI0001089");
        seedDemoAccounts(bank);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/logout", new LogoutHandler());
        server.createContext("/api/withdraw", new WithdrawHandler());
        server.createContext("/api/deposit", new DepositHandler());
        server.createContext("/api/transfer", new TransferHandler());
        server.createContext("/api/history", new HistoryHandler());
        server.createContext("/api/status", new StatusHandler());

        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("==================================================================");
        System.out.println("   OASIS NATIONAL BANK OF INDIA - NETBANKING & ATM WEB SERVER");
        System.out.println("==================================================================");
        System.out.println(" [URL] http://localhost:" + PORT);
        System.out.println(" [STATUS] Server active on port " + PORT);
        System.out.println(" Opening NetBanking portal in your browser...");
        System.out.println(" Press Ctrl+C in this console window to stop server.");
        System.out.println("==================================================================");

        openBrowser("http://localhost:" + PORT);
    }

    private static void seedDemoAccounts(Bank bank) {
        Account rajesh = new Account("1001", "1234", "Rajesh Kumar Sharma", "501004928172", "ONBI0001089", "Privilege Savings", 125000.00);
        Account priya = new Account("1002", "4321", "Priya Ramesh Patel", "501008392103", "ONBI0001089", "Classic Savings", 65500.00);
        Account amit = new Account("1003", "9999", "Amit Vikram Verma", "501001192847", "ONBI0001089", "Corporate Salary", 250000.00);
        Account ananya = new Account("1004", "1111", "Ananya Sundaram Iyer", "501006543219", "ONBI0001089", "Student Savings", 42000.00);

        bank.registerAccount(rajesh);
        bank.registerAccount(priya);
        bank.registerAccount(amit);
        bank.registerAccount(ananya);
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
            }
        } catch (Exception e) {
            System.out.println("Please open your browser manually and visit: " + url);
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}", "application/json");
                return;
            }

            Map<String, String> body = parseJsonMap(readRequestBody(exchange));
            String userId = body.getOrDefault("userId", "").trim();
            String pin = body.getOrDefault("pin", "").trim();

            Bank.AuthResult auth = bank.authenticateUser(userId, pin);
            if (auth.isAuthenticated()) {
                currentAccount = auth.getAccount();
                String json = String.format("{\"success\":true,\"message\":\"Login successful\",\"userId\":\"%s\",\"name\":\"%s\",\"accountNo\":\"%s\",\"ifsc\":\"%s\",\"accType\":\"%s\",\"balance\":%.2f}",
                        currentAccount.getUserId(), escapeJson(currentAccount.getAccountHolderName()),
                        currentAccount.getMaskedAccountNumber(), currentAccount.getIfscCode(), currentAccount.getAccountType(), currentAccount.getBalance());
                sendResponse(exchange, 200, json, "application/json");
            } else {
                String json = String.format("{\"success\":false,\"message\":\"%s\",\"remainingAttempts\":%d}",
                        escapeJson(auth.getMessage()), auth.getRemainingAttempts());
                sendResponse(exchange, 401, json, "application/json");
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            currentAccount = null;
            sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Session terminated safely\"}", "application/json");
        }
    }

    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentAccount == null) {
                sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}", "application/json");
                return;
            }

            Map<String, String> body = parseJsonMap(readRequestBody(exchange));
            double amount = 0;
            try {
                amount = Double.parseDouble(body.getOrDefault("amount", "0"));
            } catch (NumberFormatException ignored) {}

            if (amount <= 0) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Amount must be > 0\"}", "application/json");
                return;
            }

            if (amount > currentAccount.getBalance()) {
                String json = String.format("{\"success\":false,\"message\":\"Insufficient Funds: Balance is Rs. %.2f\",\"balance\":%.2f}",
                        currentAccount.getBalance(), currentAccount.getBalance());
                sendResponse(exchange, 400, json, "application/json");
                return;
            }

            Transaction tx = currentAccount.withdraw(amount, "ATM Cash Dispense");
            if (tx != null) {
                String json = String.format("{\"success\":true,\"message\":\"Cash withdrawal of Rs. %.2f successful\",\"balance\":%.2f,\"txnId\":\"%s\"}",
                        amount, currentAccount.getBalance(), tx.getTransactionId());
                sendResponse(exchange, 200, json, "application/json");
            } else {
                sendResponse(exchange, 500, "{\"success\":false,\"message\":\"Withdrawal failed\"}", "application/json");
            }
        }
    }

    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentAccount == null) {
                sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}", "application/json");
                return;
            }

            Map<String, String> body = parseJsonMap(readRequestBody(exchange));
            double amount = 0;
            try {
                amount = Double.parseDouble(body.getOrDefault("amount", "0"));
            } catch (NumberFormatException ignored) {}

            if (amount <= 0) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Deposit amount must be > 0\"}", "application/json");
                return;
            }

            Transaction tx = currentAccount.deposit(amount, "CDM Cash Deposit");
            if (tx != null) {
                String json = String.format("{\"success\":true,\"message\":\"Deposit of Rs. %.2f credited successfully\",\"balance\":%.2f,\"txnId\":\"%s\"}",
                        amount, currentAccount.getBalance(), tx.getTransactionId());
                sendResponse(exchange, 200, json, "application/json");
            } else {
                sendResponse(exchange, 500, "{\"success\":false,\"message\":\"Deposit failed\"}", "application/json");
            }
        }
    }

    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentAccount == null) {
                sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}", "application/json");
                return;
            }

            Map<String, String> body = parseJsonMap(readRequestBody(exchange));
            String recipientId = body.getOrDefault("recipientId", "").trim();
            double amount = 0;
            try {
                amount = Double.parseDouble(body.getOrDefault("amount", "0"));
            } catch (NumberFormatException ignored) {}

            Bank.TransferResult res = bank.transferFunds(currentAccount.getUserId(), recipientId, amount);
            if (res.isSuccess()) {
                String json = String.format("{\"success\":true,\"message\":\"%s\",\"balance\":%.2f,\"txnId\":\"%s\"}",
                        escapeJson(res.getMessage()), currentAccount.getBalance(), res.getSenderTransaction().getTransactionId());
                sendResponse(exchange, 200, json, "application/json");
            } else {
                String json = String.format("{\"success\":false,\"message\":\"%s\",\"balance\":%.2f}",
                        escapeJson(res.getMessage()), currentAccount.getBalance());
                sendResponse(exchange, 400, json, "application/json");
            }
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentAccount == null) {
                sendResponse(exchange, 401, "{\"error\":\"Not authenticated\"}", "application/json");
                return;
            }

            List<Transaction> list = currentAccount.getTransactionHistory();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"transactions\":[");
            for (int i = 0; i < list.size(); i++) {
                Transaction tx = list.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format("{\"id\":\"%s\",\"type\":\"%s\",\"amount\":%.2f,\"postBalance\":%.2f,\"timestamp\":\"%s\",\"remarks\":\"%s\"}",
                        tx.getTransactionId(), tx.getType().getDisplayName(), tx.getAmount(), tx.getBalanceAfter(),
                        tx.getFormattedTimestamp(), escapeJson(tx.getRemarks())));
            }
            sb.append("],\"balance\":").append(String.format("%.2f", currentAccount.getBalance())).append("}");

            sendResponse(exchange, 200, sb.toString(), "application/json");
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (currentAccount == null) {
                sendResponse(exchange, 200, "{\"authenticated\":false}", "application/json");
            } else {
                String json = String.format("{\"authenticated\":true,\"userId\":\"%s\",\"name\":\"%s\",\"accountNo\":\"%s\",\"ifsc\":\"%s\",\"balance\":%.2f}",
                        currentAccount.getUserId(), escapeJson(currentAccount.getAccountHolderName()),
                        currentAccount.getMaskedAccountNumber(), currentAccount.getIfscCode(), currentAccount.getBalance());
                sendResponse(exchange, 200, json, "application/json");
            }
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String pathStr = exchange.getRequestURI().getPath();
            if (pathStr == null || pathStr.equals("/") || pathStr.isEmpty()) {
                pathStr = "/index.html";
            }

            Path filePath = Paths.get("web-showcase", pathStr.substring(1)).normalize();
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                filePath = Paths.get(pathStr.substring(1)).normalize();
                file = filePath.toFile();
            }

            if (!file.exists()) {
                sendResponse(exchange, 404, "404 Not Found", "text/plain");
                return;
            }

            String contentType = "application/octet-stream";
            if (file.getName().endsWith(".html")) contentType = "text/html; charset=UTF-8";
            else if (file.getName().endsWith(".css")) contentType = "text/css; charset=UTF-8";
            else if (file.getName().endsWith(".js")) contentType = "application/javascript; charset=UTF-8";
            else if (file.getName().endsWith(".png")) contentType = "image/png";
            else if (file.getName().endsWith(".svg")) contentType = "image/svg+xml";

            byte[] bytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseJsonMap(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
            String[] pairs = trimmed.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String k = kv[0].trim().replace("\"", "");
                    String v = kv[1].trim().replace("\"", "");
                    map.put(k, v);
                }
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
