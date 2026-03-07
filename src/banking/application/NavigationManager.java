package banking.application;

import banking.model.User;
import banking.security.AuthSession;
import banking.ui.controllers.SessionAwareController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class NavigationManager {
    private static final String THEME_PATH = "/banking/resources/styles/theme.css";
    private static Stage primaryStage;

    private NavigationManager() {
    }

    public static void start(Stage stage) {
        primaryStage = stage;
        showLogin();
        primaryStage.show();
    }

    public static void showLogin() {
        AuthSession.clear();
        setScene("/banking/resources/ui/views/LoginView.fxml", "Secure Bank", 1280, 820, null);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static void navigateToLogin(Stage stage) {
        primaryStage = stage;
        showLogin();
    }

    public static void showDashboard(User user) {
        AuthSession.start(user);
        String fxml = user.isAdmin()
            ? "/banking/resources/ui/views/AdminDashboardView.fxml"
            : "/banking/resources/ui/views/UserDashboardView.fxml";
        String title = user.isAdmin() ? "Secure Bank Admin" : "Secure Bank Dashboard";
        setScene(fxml, title, 1440, 920, controller -> {
            if (controller instanceof SessionAwareController awareController) {
                awareController.setUser(user);
            }
        });
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(760);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
    }

    public static void navigateToDashboard(Stage stage, User user) {
        primaryStage = stage;
        showDashboard(user);
    }

    private static void setScene(String resourcePath, String title, double width, double height,
                                 ControllerConfigurer controllerConfigurer) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(resourcePath));
            Parent root = loader.load();
            if (controllerConfigurer != null) {
                controllerConfigurer.configure(loader.getController());
            }

            Scene scene = new Scene(root, width, height);
            String theme = NavigationManager.class.getResource(THEME_PATH).toExternalForm();
            scene.getStylesheets().add(theme);

            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
        } catch (IOException exception) {
            exception.printStackTrace();
            throw new IllegalStateException("Unable to load view: " + resourcePath, exception);
        }
    }

    @FunctionalInterface
    private interface ControllerConfigurer {
        void configure(Object controller);
    }
}
