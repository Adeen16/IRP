package banking.application;

import banking.model.User;
import banking.security.AuthSession;
import banking.ui.admin.AdminDashboard;
import banking.ui.user.UserDashboard;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;

public class NavigationManager {

    public static void navigateToLogin(Stage stage) {
        try {
            AuthSession.clear();
            String fxmlPath = "banking/resources/fxml/LandingPage.fxml";
            
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getClassLoader().getResource(fxmlPath));
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 900, 600);
            applyTheme(scene);
            root.setOpacity(0);
            stage.setTitle("Secure Banking System");
            stage.setScene(scene);
            applyTheme(stage);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to load login page:");
            e.printStackTrace();
        }
    }

    public static void navigateToDashboard(Stage stage, User user) {
        AuthSession.start(user);
        
        // Close the JavaFX stage and launch Swing dashboard directly
        stage.close();
        
        // Launch the appropriate Swing dashboard based on role
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (user.isAdmin()) {
                AdminDashboard adminDashboard = new AdminDashboard(user);
                adminDashboard.setVisible(true);
            } else {
                UserDashboard userDashboard = new UserDashboard(user);
                userDashboard.setVisible(true);
            }
        });
    }
    
    private static void applyTheme(Scene scene) {
        try {
            String cssPath = "banking/resources/css/global.css";
            URL cssUrl = NavigationManager.class.getClassLoader().getResource(cssPath);
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("[WARN] Could not apply theme: " + e.getMessage());
        }
    }
    
    private static void applyTheme(Stage stage) {
        if (stage != null && stage.getScene() != null) {
            applyTheme(stage.getScene());
        }
    }
}
