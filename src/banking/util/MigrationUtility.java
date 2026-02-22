package banking.util;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility to run database migrations from within Java.
 */
public class MigrationUtility {
    public static void main(String[] args) {
        System.out.println("Running database migration...");
        String sql = "ALTER TABLE users ADD COLUMN customer_id INT NULL, " +
                     "ADD FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL;";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            System.out.println("Migration successful!");
        } catch (Exception e) {
            // Check if column already exists
            if (e.getMessage().contains("Duplicate column name")) {
                System.out.println("Column already exists. Skipping.");
            } else {
                System.err.println("Migration failed: " + e.getMessage());
                System.exit(1);
            }
        }
    }
}
