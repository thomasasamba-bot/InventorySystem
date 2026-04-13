package com.inventory.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the PostgreSQL database connection using the Singleton pattern.
 * Ensures only ONE connection instance exists throughout the application lifecycle.
 * This prevents resource wastage from multiple redundant connections.
 */
public class DatabaseConnection {

    // -------------------------
    // Connection Configuration
    // -------------------------
    private static final String HOST     = "localhost";
    private static final String PORT     = "5432";
    private static final String DATABASE = "inventory_db";
    private static final String USERNAME = "inventory";       // Change to your username
    private static final String PASSWORD = "password";  // Change to your password

    // Full JDBC connection URL for PostgreSQL
    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    // Singleton instance — only one connection object exists at a time
    private static Connection instance = null;

    // -------------------------
    // Private Constructor (Singleton Pattern)
    // Prevents direct instantiation from outside this class
    // -------------------------
    private DatabaseConnection() {}

    // -------------------------
    // Public Method to Get Connection
    // -------------------------
    /**
     * Returns the single shared database connection.
     * Creates a new connection if one does not exist or if the existing one is closed.
     *
     * @return active PostgreSQL Connection object
     * @throws SQLException if connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Check if connection is null or has been closed
            if (instance == null || instance.isClosed()) {

                // Explicitly load the PostgreSQL JDBC driver
                Class.forName("org.postgresql.Driver");

                // Establish the connection
                instance = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            // Driver JAR not found on classpath
            throw new SQLException("PostgreSQL JDBC Driver not found. " +
                    "Ensure postgresql-xx.jar is in your project libraries.\n" + e.getMessage());
        }
        return instance;
    }

    // -------------------------
    // Close Connection (Call on Application Exit)
    // -------------------------
    /**
     * Safely closes the database connection.
     * Should be called when the JavaFX application is shutting down.
     */
    public static void closeConnection() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("[DB] Connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }

    // -------------------------
    // Test Connection Utility
    // -------------------------
    /**
     * Validates that the connection is alive and reachable.
     * Useful during startup to confirm database availability.
     *
     * @return true if connection is valid, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && conn.isValid(3); // 3-second timeout
        } catch (SQLException e) {
            System.err.println("[DB] Connection test failed: " + e.getMessage());
            return false;
        }
    }
}