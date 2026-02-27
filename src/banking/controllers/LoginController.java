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
    @FXML private Button btnCreateAccount;
    @FXML private Label lblError;

    private AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Entrance slide animation for the card
        TranslateTransition tt = new TranslateTransition(Duration.millis(900), loginCard);
        tt.setFromY(50);
        tt.setToY(0);
        tt.play();
        
        // Button Hover Animations
        addScaleHover(btnLogin);
        addScaleHover(btnCreateAccount);
    }
    
    private void addScaleHover(Button button) {
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
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        lblError.setVisible(false);
        btnLogin.setDisable(true);
        btnLogin.setText("Authenticating...");

        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // Background thread authentication
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                // Temporarily just print success until dashboards are connected
                System.out.println("Login Success for: " + user.getUsername());
                btnLogin.setText("Success!");
                btnLogin.setStyle("-fx-background-color: #10B981; -fx-text-fill: white;"); // Emerald 500
                
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
                pause.setOnFinished(ev -> {
                    javafx.stage.Stage stage = (javafx.stage.Stage) btnLogin.getScene().getWindow();
                    banking.application.NavigationManager.navigateToDashboard(stage, user);
                });
                pause.play();
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            showError("Authentication failed: " + ex.getMessage());
            btnLogin.setDisable(false);
            btnLogin.setText("Sign In");
        });

        new Thread(loginTask).start();
    }
    
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
