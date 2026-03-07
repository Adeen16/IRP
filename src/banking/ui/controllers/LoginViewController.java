package banking.ui.controllers;

import banking.application.NavigationManager;
import banking.model.Customer;
import banking.model.User;
import banking.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Enter both fields to continue.");
            return;
        }

        setLoading(true, "Authenticating...");
        lblError.setVisible(false);

        UiSupport.runAsync("login-ui", () -> authenticate(username, password), user -> {
            setLoading(false, "Access Granted");
            NavigationManager.showDashboard(user);
        }, error -> {
            setLoading(false, "Enter Secure Bank");
            showError(error.getMessage());
        });
    }

    private User authenticate(String username, String password) throws Exception {
        try {
            Customer customer = authService.authenticateByNameAndAccountNumber(username, password);
            if (customer != null) {
                User mockUser = new User(customer.getName(), "", User.UserRole.USER);
                mockUser.setUserId(customer.getUserId() > 0 ? customer.getUserId() : -customer.getCustomerId());
                return mockUser;
            }
        } catch (Exception ignored) {
        }
        return authService.login(username, password);
    }

    private void setLoading(boolean loading, String buttonText) {
        btnLogin.setDisable(loading);
        btnLogin.setText(buttonText);
    }

    private void showError(String message) {
        lblError.setText(message == null ? "Authentication failed." : message);
        lblError.setVisible(true);
    }
}
