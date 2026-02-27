package banking.application;

import banking.model.User;
import banking.controllers.DashboardController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NavigationManager {

    public static void navigateToLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource("/banking/resources/fxml/LandingPage.fxml"));
            Parent root = loader.load();
            
            String cssPath = NavigationManager.class.getResource("/banking/resources/css/global.css").toExternalForm();
            root.getStylesheets().add(cssPath);

            Scene scene = new Scene(root, 900, 600);
            root.setOpacity(0);
            stage.setTitle("Secure Banking System");
            stage.setScene(scene);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void navigateToDashboard(Stage stage, User user) {
        try {
            String fxmlPath = user.isAdmin() ? "/banking/resources/fxml/AdminDashboard.fxml" : "/banking/resources/fxml/UserDashboard.fxml";
            String title = user.isAdmin() ? "Admin Dashboard" : "User Dashboard";
            
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).setUser(user);
            }
            
            String cssPath = NavigationManager.class.getResource("/banking/resources/css/global.css").toExternalForm();
            root.getStylesheets().add(cssPath);

            Scene scene = new Scene(root, 900, 600);
            root.setOpacity(0);
            stage.setTitle(title);
            stage.setScene(scene);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
