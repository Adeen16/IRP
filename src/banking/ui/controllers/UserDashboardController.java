package banking.ui.controllers;

import banking.application.NavigationManager;
import banking.model.Account;
import banking.model.Customer;
import banking.model.Loan;
import banking.model.LoanDecision;
import banking.model.Transaction;
import banking.model.User;
import banking.security.AuthSession;
import banking.service.AuthService;
import banking.service.BankingService;
import banking.service.ChatController;
import banking.service.LoanService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDashboardController implements SessionAwareController {
    @FXML private Label lblWelcome;
    @FXML private Label lblHeaderContext;
    @FXML private Label lblSessionState;
    @FXML private Button btnNavOverview;
    @FXML private Button btnNavStatement;
    @FXML private Button btnNavLoan;
    @FXML private Button btnNavSecurity;
    @FXML private Button btnNavAssistant;

    @FXML private VBox paneOverview;
    @FXML private VBox paneStatement;
    @FXML private VBox paneLoan;
    @FXML private VBox paneSecurity;
    @FXML private VBox paneAssistant;

    @FXML private ComboBox<String> cmbDashboardAccount;
    @FXML private Label lblSummaryAccount;
    @FXML private Label lblSummaryType;
    @FXML private Label lblSummaryBalance;
    @FXML private Label lblSummaryCibil;
    @FXML private Label lblFraudHeadline;
    @FXML private Label lblFraudText;
    @FXML private TableView<Transaction> tblRecentTransactions;
    @FXML private TableColumn<Transaction, String> colRecentDate;
    @FXML private TableColumn<Transaction, String> colRecentType;
    @FXML private TableColumn<Transaction, String> colRecentAmount;
    @FXML private BarChart<String, Number> chartActivity;
    @FXML private PieChart chartBreakdown;

    @FXML private ComboBox<String> cmbStatementAccount;
    @FXML private TableView<Transaction> tblStatement;
    @FXML private TableColumn<Transaction, String> colStatementDate;
    @FXML private TableColumn<Transaction, String> colStatementType;
    @FXML private TableColumn<Transaction, String> colStatementAmount;

    @FXML private ComboBox<String> cmbLoanAccount;
    @FXML private TextField txtLoanAmount;
    @FXML private TextField txtLoanIncome;
    @FXML private ComboBox<String> cmbLoanType;
    @FXML private ComboBox<Integer> cmbLoanDuration;
    @FXML private TextArea txtLoanDecision;
    @FXML private TableView<Loan> tblLoanHistory;
    @FXML private TableColumn<Loan, String> colLoanId;
    @FXML private TableColumn<Loan, String> colLoanType;
    @FXML private TableColumn<Loan, String> colLoanAmount;
    @FXML private TableColumn<Loan, String> colLoanStatus;
    @FXML private TableColumn<Loan, String> colLoanEmi;

    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblSecurityMessage;

    @FXML private Label lblAssistantStatus;
    @FXML private ComboBox<String> cmbAssistantModel;
    @FXML private VBox chatMessages;
    @FXML private ScrollPane scrollAssistant;
    @FXML private TextField txtAssistantInput;
    @FXML private Button btnSendAssistant;

    private final BankingService bankingService = new BankingService();
    private final AuthService authService = new AuthService();
    private final LoanService loanService = new LoanService();

    private User currentUser;
    private Customer currentCustomer;
    private ChatController chatController;
    private final Map<String, Account> accountByNumber = new HashMap<>();
    private List<Account> currentAccounts = new ArrayList<>();
    private List<Transaction> currentTransactions = new ArrayList<>();

    @FXML
    private void initialize() {
        configureTables();
        cmbLoanType.setItems(FXCollections.observableArrayList("PERSONAL", "STUDENT", "HOME", "AUTO"));
        cmbLoanDuration.setItems(FXCollections.observableArrayList(12, 24, 36, 48, 60));
        cmbAssistantModel.setItems(FXCollections.observableArrayList("phi3", "llama3:8b"));

        cmbDashboardAccount.valueProperty().addListener((obs, oldValue, newValue) -> updateSelectedAccountSummary(newValue));
        cmbStatementAccount.valueProperty().addListener((obs, oldValue, newValue) -> loadStatement(newValue));
        cmbAssistantModel.valueProperty().addListener((obs, oldValue, newValue) -> switchAssistantModel(newValue));
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        this.chatController = new ChatController(user, bankingService);
        lblWelcome.setText(user.isAdmin() ? "Administrator" : user.getUsername());
        lblHeaderContext.setText("Customer workspace with secure transfers, statements, loans, and AI support.");
        lblSessionState.setText("Signed in as " + user.getRole().name());
        cmbAssistantModel.setValue(chatController.getModelName());
        clearAssistant();
        showOverview();
        refreshAllUserData();
        refreshAssistantStatus();
    }

    @FXML
    private void showOverview() {
        showPane(paneOverview, btnNavOverview);
    }

    @FXML
    private void showStatement() {
        showPane(paneStatement, btnNavStatement);
        loadStatement(cmbStatementAccount.getValue());
    }

    @FXML
    private void showLoan() {
        showPane(paneLoan, btnNavLoan);
    }

    @FXML
    private void showSecurity() {
        showPane(paneSecurity, btnNavSecurity);
        clearSecurityForm();
    }

    @FXML
    private void showAssistant() {
        showPane(paneAssistant, btnNavAssistant);
        txtAssistantInput.requestFocus();
    }

    @FXML
    private void handleLogout() {
        AuthSession.clear();
        NavigationManager.showLogin();
    }

    @FXML
    private void handleRefreshOverview() {
        refreshAllUserData();
    }

    @FXML
    private void handleDeposit() {
        openTransactionDialog("DEPOSIT");
    }

    @FXML
    private void handleWithdraw() {
        openTransactionDialog("WITHDRAW");
    }

    @FXML
    private void handleTransfer() {
        openTransactionDialog("TRANSFER");
    }

    @FXML
    private void handleSetPin() {
        String accountNumber = cmbDashboardAccount.getValue();
        if (accountNumber == null || accountNumber.isBlank()) {
            UiSupport.showWarning("No Account Selected", "Choose an account before setting a transaction PIN.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Set Transaction PIN");
        dialog.setHeaderText("Update PIN for account " + accountNumber);
        dialog.setContentText("Enter a 4-6 digit PIN:");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/banking/resources/styles/theme.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("dialog-root");
        var result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String newPin = result.get().trim();
        if (!newPin.matches("\\d{4,6}")) {
            UiSupport.showWarning("Invalid PIN", "Transaction PIN must be 4-6 digits.");
            return;
        }
        UiSupport.runAsync("set-pin", () -> bankingService.setTransactionPassword(accountNumber, newPin, currentUser.getUserId()),
            ignored -> UiSupport.showInfo("PIN Updated", "Transaction PIN saved for account " + accountNumber + "."),
            error -> UiSupport.showError("PIN Update Failed", error.getMessage()));
    }

    @FXML
    private void handleRefreshStatement() {
        loadStatement(cmbStatementAccount.getValue());
    }

    @FXML
    private void handleSubmitLoan() {
        String accountNumber = cmbLoanAccount.getValue();
        if (accountNumber == null || accountNumber.isBlank()) {
            UiSupport.showWarning("Missing Account", "Select an account for this loan request.");
            return;
        }

        BigDecimal amount;
        BigDecimal income;
        try {
            amount = new BigDecimal(txtLoanAmount.getText().trim());
            income = new BigDecimal(txtLoanIncome.getText().trim());
        } catch (Exception exception) {
            UiSupport.showError("Invalid Values", "Enter valid numeric loan amount and monthly income.");
            return;
        }

        String loanType = cmbLoanType.getValue();
        Integer duration = cmbLoanDuration.getValue();
        if (loanType == null || duration == null || currentCustomer == null) {
            UiSupport.showWarning("Missing Details", "Complete every loan request field first.");
            return;
        }

        txtLoanDecision.setText("Submitting loan application...");
        UiSupport.runAsync("loan-submit", () -> loanService.submitLoanRequest(
            currentCustomer.getCustomerId(),
            accountNumber,
            currentCustomer.getCibilScore(),
            income,
            amount,
            loanType,
            duration
        ), decision -> {
            renderLoanDecision(decision);
            refreshLoanHistory();
        }, error -> {
            txtLoanDecision.setText("Loan request failed: " + error.getMessage());
        });
    }

    @FXML
    private void handleUpdatePassword() {
        String currentPassword = txtCurrentPassword.getText();
        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            setSecurityMessage("All password fields are required.", true);
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            setSecurityMessage("New passwords do not match.", true);
            return;
        }

        setSecurityMessage("Updating password...", false);
        UiSupport.runAsync("password-update", () -> authService.changePassword(currentUser.getUserId(), currentPassword, newPassword),
            ignored -> {
                clearSecurityForm();
                setSecurityMessage("Password updated successfully.", false);
            },
            error -> setSecurityMessage(error.getMessage(), true));
    }

    @FXML
    private void handleSendAssistant() {
        String prompt = txtAssistantInput.getText().trim();
        if (prompt.isEmpty()) {
            return;
        }

        appendChatBubble("You", prompt, true);
        txtAssistantInput.clear();
        setAssistantBusy(true, "Thinking...");

        UiSupport.runAsync("assistant-message", () -> chatController.processMessage(prompt), response -> {
            appendChatBubble("Assistant", response, false);
            setAssistantBusy(false, "Model: " + chatController.getModelName() + " | Ready");
        }, error -> {
            appendChatBubble("Assistant", "[Error] " + error.getMessage(), false);
            setAssistantBusy(false, "Assistant unavailable");
        });
    }

    @FXML
    private void handleClearAssistant() {
        clearAssistant();
    }

    private void refreshAllUserData() {
        UiSupport.runAsync("user-dashboard-load", this::loadUserState, state -> {
            currentCustomer = state.customer;
            currentAccounts = state.accounts;
            currentTransactions = state.transactions;

            accountByNumber.clear();
            for (Account account : state.accounts) {
                accountByNumber.put(account.getAccountNumber(), account);
            }

            List<String> accountNumbers = state.accounts.stream().map(Account::getAccountNumber).toList();
            cmbDashboardAccount.setItems(FXCollections.observableArrayList(accountNumbers));
            cmbStatementAccount.setItems(FXCollections.observableArrayList(accountNumbers));
            cmbLoanAccount.setItems(FXCollections.observableArrayList(accountNumbers));

            selectDefaultValue(cmbDashboardAccount, accountNumbers);
            selectDefaultValue(cmbStatementAccount, accountNumbers);
            selectDefaultValue(cmbLoanAccount, accountNumbers);

            updateSelectedAccountSummary(cmbDashboardAccount.getValue());
            tblRecentTransactions.setItems(FXCollections.observableArrayList(state.transactions.stream().limit(8).toList()));
            tblLoanHistory.setItems(FXCollections.observableArrayList(state.loans));
            renderOverviewCharts(state.transactions);
            renderFraudAlert(state.transactions);
            if (cmbStatementAccount.getValue() != null) {
                loadStatement(cmbStatementAccount.getValue());
            }
        }, error -> UiSupport.showError("Dashboard Load Failed", error.getMessage()));
    }

    private UserState loadUserState() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        List<Account> accounts = customer == null
            ? new ArrayList<>()
            : bankingService.getAccountsByCustomer(customer.getCustomerId());

        List<Transaction> transactions = new ArrayList<>();
        for (Account account : accounts) {
            transactions.addAll(bankingService.getTransactionHistory(account.getAccountNumber()));
        }
        transactions.sort(Comparator.comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Loan> loans = customer == null ? new ArrayList<>() : loanService.getCustomerLoans(customer.getCustomerId());
        loans.sort(Comparator.comparing(Loan::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return new UserState(customer, accounts, transactions, loans);
    }

    private void configureTables() {
        colRecentDate.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatTimestamp(data.getValue().getCreatedAt())));
        colRecentType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colRecentAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getAmount())));

        colStatementDate.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatTimestamp(data.getValue().getCreatedAt())));
        colStatementType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colStatementAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getAmount())));

        colLoanId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getLoanId())));
        colLoanType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanType()));
        colLoanAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getLoanAmount())));
        colLoanStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        colLoanEmi.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getEmi())));
    }

    private void updateSelectedAccountSummary(String accountNumber) {
        Account selectedAccount = accountNumber == null ? null : accountByNumber.get(accountNumber);
        if (selectedAccount == null) {
            lblSummaryAccount.setText("No account selected");
            lblSummaryType.setText("Type: --");
            lblSummaryBalance.setText(UiSupport.formatCurrency(BigDecimal.ZERO));
            lblSummaryCibil.setText("CIBIL Score: --");
            return;
        }

        lblSummaryAccount.setText(selectedAccount.getAccountNumber());
        lblSummaryType.setText("Type: " + selectedAccount.getAccountType().name());
        lblSummaryBalance.setText(UiSupport.formatCurrency(selectedAccount.getBalance()));
        lblSummaryCibil.setText("CIBIL Score: " + (currentCustomer == null ? "--" : currentCustomer.getCibilScore()));
    }

    private void loadStatement(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            tblStatement.getItems().clear();
            return;
        }
        UiSupport.runAsync("statement-load", () -> bankingService.getTransactionHistory(accountNumber),
            transactions -> tblStatement.setItems(FXCollections.observableArrayList(transactions)),
            error -> UiSupport.showError("Statement Load Failed", error.getMessage()));
    }

    private void refreshLoanHistory() {
        if (currentCustomer == null) {
            tblLoanHistory.getItems().clear();
            return;
        }
        UiSupport.runAsync("loan-history", () -> loanService.getCustomerLoans(currentCustomer.getCustomerId()),
            loans -> tblLoanHistory.setItems(FXCollections.observableArrayList(loans)),
            error -> UiSupport.showError("Loan History Failed", error.getMessage()));
    }

    private void renderLoanDecision(LoanDecision decision) {
        txtLoanDecision.setText(
            "Status: " + decision.getStatus() + "\n" +
            "Interest Rate: " + decision.getInterestRate() + "%\n" +
            "Duration: " + decision.getLoanDuration() + " months\n" +
            "Monthly EMI: " + UiSupport.formatCurrency(decision.getEmi()) + "\n\n" +
            decision.getReason()
        );
    }

    private void renderOverviewCharts(List<Transaction> transactions) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        Map<String, Integer> counts = new HashMap<>();
        Map<String, BigDecimal> totals = new HashMap<>();

        for (Transaction transaction : transactions) {
            String type = transaction.getType().name();
            counts.put(type, counts.getOrDefault(type, 0) + 1);
            totals.put(type, totals.getOrDefault(type, BigDecimal.ZERO).add(transaction.getAmount()));
        }

        for (String type : List.of("DEPOSIT", "WITHDRAW", "TRANSFER", "LOAN_CREDIT")) {
            series.getData().add(new XYChart.Data<>(type, counts.getOrDefault(type, 0)));
        }
        chartActivity.getData().setAll(series);

        List<PieChart.Data> breakdown = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            breakdown.add(new PieChart.Data(entry.getKey(), entry.getValue().doubleValue()));
        }
        if (breakdown.isEmpty()) {
            breakdown.add(new PieChart.Data("No activity", 1));
        }
        chartBreakdown.setData(FXCollections.observableArrayList(breakdown));
    }

    private void renderFraudAlert(List<Transaction> transactions) {
        long largeTransfers = transactions.stream()
            .filter(transaction -> transaction.getType() == Transaction.TransactionType.TRANSFER || transaction.getType() == Transaction.TransactionType.WITHDRAW)
            .filter(transaction -> transaction.getAmount() != null && transaction.getAmount().compareTo(new BigDecimal("10000")) >= 0)
            .count();

        if (largeTransfers > 0) {
            lblFraudHeadline.setText("High-value movement detected");
            lblFraudText.setText("Review recent transfer and withdrawal activity. Update your PIN if anything looks unfamiliar.");
            return;
        }

        long repeatedTransfers = transactions.stream()
            .filter(transaction -> transaction.getType() == Transaction.TransactionType.TRANSFER)
            .limit(5)
            .count();

        if (repeatedTransfers >= 3) {
            lblFraudHeadline.setText("Watch for repeated transfers");
            lblFraudText.setText("Several recent transfer events were recorded. Confirm every destination account before sending funds.");
        } else {
            lblFraudHeadline.setText("No immediate fraud signals");
            lblFraudText.setText("Your recent activity looks normal. Keep transaction PINs private and monitor statement updates daily.");
        }
    }

    private void switchAssistantModel(String model) {
        if (chatController == null || model == null || model.isBlank()) {
            return;
        }
        chatController.setModel(model);
        refreshAssistantStatus();
    }

    private void refreshAssistantStatus() {
        if (chatController == null) {
            return;
        }
        lblAssistantStatus.setText("Checking local AI connection...");
        UiSupport.runAsync("assistant-status", chatController::isLLMAvailable, available -> {
            if (available) {
                lblAssistantStatus.setText("Model: " + chatController.getModelName() + " | Connected");
            } else {
                lblAssistantStatus.setText("Ollama offline - start `ollama serve` to use the assistant");
            }
        }, error -> lblAssistantStatus.setText("Assistant connection check failed"));
    }

    private void setAssistantBusy(boolean busy, String message) {
        btnSendAssistant.setDisable(busy);
        txtAssistantInput.setDisable(busy);
        lblAssistantStatus.setText(message);
        if (!busy) {
            txtAssistantInput.requestFocus();
        }
    }

    private void clearAssistant() {
        chatMessages.getChildren().clear();
        appendSystemBubble("Welcome to Secure Bank AI. Ask about balances, transfers, statements, or spending patterns.");
    }

    private void appendSystemBubble(String text) {
        HBox row = new HBox();
        Label label = new Label(text);
        label.getStyleClass().addAll("chat-bubble", "chat-system");
        label.setWrapText(true);
        row.getChildren().add(label);
        chatMessages.getChildren().add(row);
        scrollAssistantToBottom();
    }

    private void appendChatBubble(String sender, String text, boolean userBubble) {
        HBox row = new HBox();
        row.getStyleClass().add(userBubble ? "chat-row-user" : "chat-row-assistant");

        Label label = new Label(sender + "\n" + text);
        label.setWrapText(true);
        label.setMaxWidth(520);
        label.getStyleClass().addAll("chat-bubble", userBubble ? "chat-user" : "chat-assistant");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        if (userBubble) {
            row.getChildren().addAll(spacer, label);
        } else {
            row.getChildren().addAll(label, spacer);
        }
        chatMessages.getChildren().add(row);
        scrollAssistantToBottom();
    }

    private void scrollAssistantToBottom() {
        Platform.runLater(() -> scrollAssistant.setVvalue(1.0));
    }

    private void openTransactionDialog(String type) {
        String accountNumber = cmbDashboardAccount.getValue();
        if (accountNumber == null || accountNumber.isBlank()) {
            UiSupport.showWarning("No Account Selected", "Choose an account before performing a transaction.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/banking/resources/ui/views/TransactionDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNavOverview.getScene().getWindow());
            stage.setTitle(type + " Funds");
            Scene scene = new Scene(root, 460, "TRANSFER".equals(type) ? 520 : 460);
            scene.getStylesheets().add(getClass().getResource("/banking/resources/styles/theme.css").toExternalForm());
            stage.setScene(scene);

            TransactionDialogController controller = loader.getController();
            controller.configure(stage, currentUser, accountNumber, type, this::refreshAllUserData);
            stage.showAndWait();
        } catch (Exception exception) {
            UiSupport.showError("Unable to Open Transaction Window", exception.getMessage());
        }
    }

    private void setSecurityMessage(String message, boolean error) {
        lblSecurityMessage.setText(message);
        lblSecurityMessage.getStyleClass().removeAll("error-text", "success-text");
        lblSecurityMessage.getStyleClass().add(error ? "error-text" : "success-text");
    }

    private void clearSecurityForm() {
        txtCurrentPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        lblSecurityMessage.setText("Update your Secure Bank password with strong credentials.");
        lblSecurityMessage.getStyleClass().removeAll("error-text", "success-text");
        lblSecurityMessage.getStyleClass().add("muted-text");
    }

    private void showPane(VBox targetPane, Button activeButton) {
        List<VBox> panes = List.of(paneOverview, paneStatement, paneLoan, paneSecurity, paneAssistant);
        for (VBox pane : panes) {
            boolean active = pane == targetPane;
            pane.setVisible(active);
            pane.setManaged(active);
        }
        UiSupport.markActive(activeButton, btnNavOverview, btnNavStatement, btnNavLoan, btnNavSecurity, btnNavAssistant);
    }

    private void selectDefaultValue(ComboBox<String> comboBox, List<String> values) {
        String currentValue = comboBox.getValue();
        if (currentValue != null && values.contains(currentValue)) {
            comboBox.setValue(currentValue);
        } else if (!values.isEmpty()) {
            comboBox.setValue(values.get(0));
        } else {
            comboBox.setValue(null);
        }
    }

    private static final class UserState {
        private final Customer customer;
        private final List<Account> accounts;
        private final List<Transaction> transactions;
        private final List<Loan> loans;

        private UserState(Customer customer, List<Account> accounts, List<Transaction> transactions, List<Loan> loans) {
            this.customer = customer;
            this.accounts = accounts;
            this.transactions = transactions;
            this.loans = loans;
        }
    }
}
