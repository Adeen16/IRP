package banking.controllers;

import banking.model.User;
import banking.service.AuthService;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private VBox leftPanel;
    @FXML private VBox loginCard;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Entrance slide animation for the card (if present)
        if (loginCard != null) {
            TranslateTransition tt = new TranslateTransition(Duration.millis(600), loginCard);
            tt.setFromY(30);
            tt.setToY(0);
            tt.play();
        }
        
        // Button Hover Animation (if button exists)
        if (btnLogin != null) {
            addScaleHover(btnLogin);
        }
    }
    
    private void addScaleHover(Button button) {
        if (button == null) return;
        
        ScaleTransition stIn = new ScaleTransition(Duration.millis(150), button);
        stIn.setToX(1.02);
        stIn.setToY(1.02);
        
        ScaleTransition stOut = new ScaleTransition(Duration.millis(150), button);
        stOut.setToX(1.0);
        stOut.setToY(1.0);
        
        button.setOnMouseEntered(e -> {
            stOut.stop();
            stIn.play();
        });
        
        button.setOnMouseExited(e -> {
            stIn.stop();
            stOut.play();
        });
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername != null ? txtUsername.getText().trim() : "";
        String password = txtPassword != null ? txtPassword.getText() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        if (lblError != null) lblError.setVisible(false);
        if (btnLogin != null) {
            btnLogin.setDisable(true);
            btnLogin.setText("Authenticating...");
        }

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                System.out.println("Login Success for: " + user.getUsername());
                if (btnLogin != null) {
                    btnLogin.setText("Success!");
                    btnLogin.setStyle("-fx-background-color: #10B981; -fx-text-fill: white;");
                }
                
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
                pause.setOnFinished(ev -> {
                    javafx.stage.Stage stage = (javafx.stage.Stage) (btnLogin != null ? btnLogin.getScene().getWindow() : null);
                    if (stage != null) {
                        banking.application.NavigationManager.navigateToDashboard(stage, user);
                    }
                });
                pause.play();
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            showError("Authentication failed: " + ex.getMessage());
            if (btnLogin != null) {
                btnLogin.setDisable(false);
                btnLogin.setText("Sign In");
            }
        });

        new Thread(loginTask).start();
    }
    
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        }
    }
}
