package banking.util;

import java.sql.Connection;
import java.sql.Statement;

public class InitSchemaAdditions {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Add cibil_score to customer
            try {
                stmt.execute("ALTER TABLE customer ADD COLUMN cibil_score INTEGER DEFAULT 700");
                System.out.println("Added cibil_score to customer table.");
            } catch (Exception e) {
                System.out.println("cibil_score might already exist: " + e.getMessage());
            }

            // Create loan table
            String loanSql = "CREATE TABLE IF NOT EXISTS loan (" +
                             "loan_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                             "customer_id INTEGER NOT NULL, " +
                             "loan_amount REAL NOT NULL, " +
                             "interest_rate REAL NOT NULL DEFAULT 0, " +
                             "loan_duration INTEGER NOT NULL, " +
                             "emi REAL NOT NULL DEFAULT 0, " +
                             "loan_type TEXT NOT NULL DEFAULT 'PERSONAL', " +
                             "status TEXT NOT NULL DEFAULT 'PENDING', " +
                             "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                             "FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE" +
                             ")";
            stmt.execute(loanSql);
            System.out.println("Created loan table.");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
