package com.library;

import com.library.database.DatabaseManager;
import com.library.server.ApiHandler;
import com.library.server.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("===============================================================");
        System.out.println("   DIGITAL LIBRARY MANAGEMENT SYSTEM (TASK 5 - OASIS/AICTE)    ");
        System.out.println("===============================================================");

        // 1. Initialize SQLite Database and Seed Data
        System.out.println("[1/3] Initializing Database & Verifying Schema...");
        DatabaseManager.initializeDatabase();

        // 2. Resolve web directory path
        File webDir = new File("web");
        if (!webDir.exists()) {
            webDir = new File("Java-Task5-DigitalLibraryManagementSystem/web");
        }
        System.out.println("[2/3] Web Assets Directory: " + webDir.getAbsolutePath());

        // 3. Start HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api", new ApiHandler());
            server.createContext("/", new StaticFileHandler(webDir.getAbsolutePath()));
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            server.start();

            System.out.println("[3/3] Server started successfully!");
            System.out.println("---------------------------------------------------------------");
            System.out.println(">> Web Application URL: http://localhost:" + port);
            System.out.println(">> Admin Login:         admin     / admin123");
            System.out.println(">> Student Demo Login:  student1  / user123");
            System.out.println("---------------------------------------------------------------");
            System.out.println("Press Ctrl+C to stop the server.");
        } catch (Exception e) {
            System.err.println("Fatal: Could not start server on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
