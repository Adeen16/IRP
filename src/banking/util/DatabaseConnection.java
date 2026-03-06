package banking.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
        applySchemaUpgrades(conn);
        schemaInitialized = true;
    }

    private static void applySchemaUpgrades(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS loan (" +
                "loan_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER NOT NULL, " +
                "loan_amount REAL NOT NULL, " +
                "interest_rate REAL NOT NULL DEFAULT 0, " +
                "loan_duration INTEGER NOT NULL, " +
                "emi REAL NOT NULL DEFAULT 0, " +
                "loan_type TEXT NOT NULL DEFAULT 'PERSONAL', " +
                "status TEXT CHECK(status IN ('APPROVED', 'REJECTED', 'PENDING')) NOT NULL DEFAULT 'PENDING', " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE)");

            if (tableExists(conn, "loan_requests")) {
                stmt.execute("INSERT INTO loan (customer_id, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at) " +
                    "SELECT lr.customer_id, lr.amount, 0, 12, 0, 'PERSONAL', lr.status, lr.timestamp " +
                    "FROM loan_requests lr WHERE NOT EXISTS (SELECT 1 FROM loan l WHERE l.customer_id = lr.customer_id AND l.created_at = lr.timestamp)");
            }
        } catch (SQLException e) {
            System.err.println("Failed to apply schema upgrades: " + e.getMessage());
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
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
