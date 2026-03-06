package banking.application;

import banking.ui.ThemeManager;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainAppFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/banking/resources/fxml/LandingPage.fxml"));
        Parent root = loader.load();

        // Scene setup
        Scene scene = new Scene(root, 900, 600);
        
        ThemeManager.apply(scene);

        primaryStage.setTitle("Secure Banking System");
        primaryStage.setScene(scene);
        ThemeManager.apply(primaryStage);
        primaryStage.setResizable(false);
        
        // Entrance Fade Animation
        root.setOpacity(0);
        primaryStage.show();
        
        FadeTransition ft = new FadeTransition(Duration.millis(800), root);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
