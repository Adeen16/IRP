package banking.controllers;

import banking.application.NavigationManager;
import banking.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label lblWelcome;
    
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        if (lblWelcome != null) {
            lblWelcome.setText("Welcome, " + user.getUsername() + "!");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationManager.navigateToLogin(stage);
    }
    
    @FXML
    private void launchLegacyDashboard(ActionEvent event) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (currentUser != null && currentUser.isAdmin()) {
                new banking.ui.admin.AdminDashboard(currentUser).setVisible(true);
            } else {
                new banking.ui.user.UserDashboard(currentUser).setVisible(true);
            }
        });
    }
}
