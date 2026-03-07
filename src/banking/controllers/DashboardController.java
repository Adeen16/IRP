package banking.controllers;

import banking.application.NavigationManager;
import banking.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.Node;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label lblWelcome;
    @FXML private Label lblBrand;
    @FXML private Label lblBalance;
    @FXML private Label lblLoanStatus;
    @FXML private Label lblTotalAssets;
    @FXML private Label lblTotalAccounts;
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblPendingLoans;
    
    @FXML private ComboBox<?> cmbAccount;
    @FXML private TextField txtSearch;
    @FXML private TableView<?> tblTransactions;
    @FXML private PieChart chartSpending;
    @FXML private PieChart chartAccounts;
    @FXML private PieChart chartAccountTypes;
    @FXML private BarChart<?, ?> chartTransactions;
    
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        if (lblWelcome != null) {
            lblWelcome.setText("Welcome, " + user.getUsername() + "!");
        }
        if (lblBrand != null && user.isAdmin()) {
            lblBrand.setText("SECURE BANK - ADMIN");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        NavigationManager.navigateToLogin(stage);
    }
    
    @FXML
    private void handleDeposit(ActionEvent event) {
        // Launch legacy Swing dialog for deposit
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleWithdraw(ActionEvent event) {
        // Launch legacy Swing dialog for withdraw
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleTransfer(ActionEvent event) {
        // Launch legacy Swing dialog for transfer
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleApplyLoan(ActionEvent event) {
        // Launch legacy Swing panel for loan
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        // Handle search in admin view
    }
    
    @FXML
    private void handleManageCustomers(ActionEvent event) {
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleManageAccounts(ActionEvent event) {
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleManageLoans(ActionEvent event) {
        launchLegacyDashboard(event);
    }
    
    @FXML
    private void handleTransactionReports(ActionEvent event) {
        launchLegacyDashboard(event);
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
