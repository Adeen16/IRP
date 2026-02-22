package banking;

import banking.ui.LoginForm;
import banking.util.DatabaseConnection;

import javax.swing.*;

/**
 * Antigravity Banking System
 * Secure, multi-user, database-backed management system.
 */
public class MainApp {
    public static void main(String[] args) {
        // Set Look and Feel (FlatLaf)
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Check Database Connection
        System.out.println("Initializing Secure Banking System...");
        
        // Use a background thread for initialization
        SwingUtilities.invokeLater(() -> {
            boolean connected = DatabaseConnection.testConnection();
            if (!connected) {
                int option = JOptionPane.showConfirmDialog(null, 
                    "Could not connect to MySQL database.\n" +
                    "Please ensure MySQL is running and the 'banking_system' database is created.\n\n" +
                    "Would you like to try connecting with default settings anyway?", 
                    "Database Connection Error", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.ERROR_MESSAGE);
                
                if (option != JOptionPane.YES_OPTION) {
                    System.exit(1);
                }
            }

            // Launch Login Form
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
            
            System.out.println("System Ready.");
        });

        // Add Shutdown Hook for DB Cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down system...");
            DatabaseConnection.closeAllConnections();
        }));
    }
}
