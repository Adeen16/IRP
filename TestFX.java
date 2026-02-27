import javafx.application.Application;
import javafx.stage.Stage;

public class TestFX extends Application {
    @Override
    public void start(Stage primaryStage) {
        System.out.println("JavaFX is available!");
        System.exit(0);
    }
    public static void main(String[] args) {
        launch(args);
    }
}
