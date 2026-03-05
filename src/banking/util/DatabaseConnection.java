package banking.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Properties;

public class DatabaseConnection {
    private static String url;
    private static String username;
    private static String password;
    private static boolean schemaInitialized = false;

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("database.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                url = "jdbc:sqlite:bank.db";
                username = "";
                password = ""; 
            } else {
                prop.load(input);
                url = prop.getProperty("db.url");
                username = prop.getProperty("db.username", "");
                password = prop.getProperty("db.password", "");
            }
            if (url == null || url.isEmpty()) {
                url = "jdbc:sqlite:bank.db";
            }
        } catch (Exception e) {
            System.err.println("Failed to load database properties: " + e.getMessage());
            url = "jdbc:sqlite:bank.db";
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url, username, password);
        
        // Enforce SQLite Foreign Keys
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        
        if (!schemaInitialized) {
            initializeSchemaIfNeeded(conn);
        }
        
        return conn;
    }
    
    private static synchronized void initializeSchemaIfNeeded(Connection conn) {
        if (schemaInitialized) return;
        
        boolean needsInit = true;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'")) {
            if (rs.next()) {
                needsInit = false; // Schema already exists
            }
        } catch (SQLException e) {
            System.err.println("Error checking schema existence: " + e.getMessage());
        }
        
        if (needsInit) {
            System.out.println("Initializing SQLite database schema from database/schema.sql...");
            try {
                String schemaSql = new String(Files.readAllBytes(Paths.get("database/schema.sql")));
                String[] statements = schemaSql.split(";");
                
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        if (sql.trim().length() > 0) {
                            stmt.execute(sql);
                        }
                    }
                }
                System.out.println("Schema initialized successfully.");
            } catch (Exception e) {
                System.err.println("Failed to initialize database schema.");
                e.printStackTrace();
            }
        }
        schemaInitialized = true;
    }
    
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error releasing connection: " + e.getMessage());
            }
        }
    }
    
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public static void closeAllConnections() {
        // No-op for direct connections without pool
    }
}