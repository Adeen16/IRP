package banking.application;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainAppFX extends Application {

    @Override
    public void start(Stage primaryStage) {
        NavigationManager.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
