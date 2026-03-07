package banking.ui.controllers;

import banking.application.NavigationManager;
import banking.model.Account;
import banking.model.Customer;
import banking.model.Loan;
import banking.model.Transaction;
import banking.model.User;
import banking.security.AuthSession;
import banking.service.AnalyticsService;
import banking.service.AuthService;
import banking.service.BankingService;
import banking.service.LoanService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardController implements SessionAwareController {
    @FXML private Label lblWelcome;
    @FXML private Label lblHeaderContext;
    @FXML private Label lblSessionState;

    @FXML private Button btnNavOverview;
    @FXML private Button btnNavCustomers;
    @FXML private Button btnNavAccounts;
    @FXML private Button btnNavLoans;
    @FXML private Button btnNavTransactions;
    @FXML private Button btnNavUsers;

    @FXML private VBox paneOverview;
    @FXML private VBox paneCustomers;
    @FXML private VBox paneAccounts;
    @FXML private VBox paneLoans;
    @FXML private VBox paneTransactions;
    @FXML private VBox paneUsers;

    @FXML private Label lblTotalAssets;
    @FXML private Label lblTotalAccounts;
    @FXML private Label lblPendingLoans;
    @FXML private Label lblTotalCustomers;
    @FXML private BarChart<String, Number> chartAdminTransactions;
    @FXML private PieChart chartAccountTypes;
    @FXML private TableView<Transaction> tblAdminRecentTransactions;
    @FXML private TableColumn<Transaction, String> colAdminRecentDate;
    @FXML private TableColumn<Transaction, String> colAdminRecentType;
    @FXML private TableColumn<Transaction, String> colAdminRecentAmount;

    @FXML private TextField txtCustomerSearch;
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, String> colCustomerId;
    @FXML private TableColumn<Customer, String> colCustomerName;
    @FXML private TableColumn<Customer, String> colCustomerPhone;
    @FXML private TableColumn<Customer, String> colCustomerEmail;
    @FXML private TableColumn<Customer, String> colCustomerCibil;
    @FXML private TextField txtCustomerName;
    @FXML private TextField txtCustomerPhone;
    @FXML private TextField txtCustomerEmail;

    @FXML private TableView<AccountRow> tblAccounts;
    @FXML private TableColumn<AccountRow, String> colAccountNumber;
    @FXML private TableColumn<AccountRow, String> colAccountCustomer;
    @FXML private TableColumn<AccountRow, String> colAccountType;
    @FXML private TableColumn<AccountRow, String> colAccountBalance;
    @FXML private TextField txtAccountCustomerName;
    @FXML private TextField txtAccountPhone;
    @FXML private TextField txtAccountEmail;
    @FXML private TextField txtAccountInitialDeposit;
    @FXML private ComboBox<String> cmbAccountType;
    @FXML private PasswordField txtAccountPin;

    @FXML private TableView<Loan> tblLoans;
    @FXML private TableColumn<Loan, String> colLoanId;
    @FXML private TableColumn<Loan, String> colLoanCustomerId;
    @FXML private TableColumn<Loan, String> colLoanAccount;
    @FXML private TableColumn<Loan, String> colLoanType;
    @FXML private TableColumn<Loan, String> colLoanAmount;
    @FXML private TableColumn<Loan, String> colLoanStatus;

    @FXML private ComboBox<String> cmbTransactionAccount;
    @FXML private TextField txtTransactionFrom;
    @FXML private TextField txtTransactionTo;
    @FXML private TableView<Transaction> tblTransactions;
    @FXML private TableColumn<Transaction, String> colTransactionId;
    @FXML private TableColumn<Transaction, String> colTransactionAccount;
    @FXML private TableColumn<Transaction, String> colTransactionType;
    @FXML private TableColumn<Transaction, String> colTransactionAmount;
    @FXML private TableColumn<Transaction, String> colTransactionDate;

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, String> colUserId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colUserRole;
    @FXML private TableColumn<User, String> colUserCreated;
    @FXML private TextField txtSystemUsername;
    @FXML private PasswordField txtSystemPassword;
    @FXML private PasswordField txtSystemConfirm;
    @FXML private ComboBox<User.UserRole> cmbSystemRole;
    @FXML private ComboBox<Customer> cmbSystemCustomer;

    private final BankingService bankingService = new BankingService();
    private final AuthService authService = new AuthService();
    private final LoanService loanService = new LoanService();
    private final AnalyticsService analyticsService = new AnalyticsService();

    private User currentUser;
    private Customer selectedCustomer;
    private Map<Integer, Customer> customersById = new HashMap<>();

    @FXML
    private void initialize() {
        configureTables();
        cmbAccountType.setItems(FXCollections.observableArrayList("SAVINGS", "CURRENT"));
        cmbSystemRole.setItems(FXCollections.observableArrayList(User.UserRole.values()));
        cmbSystemRole.valueProperty().addListener((obs, oldValue, newValue) -> updateUserCustomerVisibility());
        cmbSystemCustomer.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getName() + " (#" + customer.getCustomerId() + ")";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateCustomerForm(newValue));
    }

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        lblWelcome.setText(user.getUsername());
        lblHeaderContext.setText("Admin control center for customers, accounts, credit approvals, reporting, and users.");
        lblSessionState.setText("Role: " + user.getRole().name());
        cmbSystemRole.setValue(User.UserRole.USER);
        showOverview();
        refreshAllAdminData();
    }

    @FXML
    private void showOverview() {
        showPane(paneOverview, btnNavOverview);
    }

    @FXML
    private void showCustomers() {
        showPane(paneCustomers, btnNavCustomers);
    }

    @FXML
    private void showAccounts() {
        showPane(paneAccounts, btnNavAccounts);
    }

    @FXML
    private void showLoans() {
        showPane(paneLoans, btnNavLoans);
    }

    @FXML
    private void showTransactions() {
        showPane(paneTransactions, btnNavTransactions);
    }

    @FXML
    private void showUsers() {
        showPane(paneUsers, btnNavUsers);
    }

    @FXML
    private void handleLogout() {
        AuthSession.clear();
        NavigationManager.showLogin();
    }

    @FXML
    private void handleRefreshAll() {
        refreshAllAdminData();
    }

    @FXML
    private void handleSearchCustomers() {
        String query = txtCustomerSearch.getText().trim();
        UiSupport.runAsync("customer-search", () -> query.isEmpty() ? bankingService.getAllCustomers() : bankingService.searchCustomersByName(query),
            customers -> tblCustomers.setItems(FXCollections.observableArrayList(customers)),
            error -> UiSupport.showError("Customer Search Failed", error.getMessage()));
    }

    @FXML
    private void handleNewCustomer() {
        selectedCustomer = null;
        txtCustomerName.clear();
        txtCustomerPhone.clear();
        txtCustomerEmail.clear();
    }

    @FXML
    private void handleSaveCustomer() {
        String name = txtCustomerName.getText().trim();
        String phone = txtCustomerPhone.getText().trim();
        String email = txtCustomerEmail.getText().trim();

        if (name.isBlank() || phone.isBlank() || email.isBlank()) {
            UiSupport.showWarning("Missing Customer Details", "Name, phone, and email are required.");
            return;
        }

        if (selectedCustomer == null) {
            UiSupport.runAsync("customer-create", () -> bankingService.createCustomer(name, phone, email, ""), customer -> {
                String[] parts = customer.getPhone() == null ? new String[0] : customer.getPhone().split("\\|\\|");
                String generatedUsername = parts.length > 1 ? parts[1] : "N/A";
                String generatedPassword = parts.length > 2 ? parts[2] : "N/A";
                UiSupport.showInfo("Customer Created", "Generated access\nUsername: " + generatedUsername + "\nPassword: " + generatedPassword);
                refreshAllAdminData();
            }, error -> UiSupport.showError("Create Customer Failed", error.getMessage()));
        } else {
            selectedCustomer.setName(name);
            selectedCustomer.setPhone(phone);
            selectedCustomer.setEmail(email);
            UiSupport.runAsync("customer-update", () -> bankingService.updateCustomer(selectedCustomer),
                ignored -> refreshAllAdminData(),
                error -> UiSupport.showError("Update Customer Failed", error.getMessage()));
        }
    }

    @FXML
    private void handleDeleteCustomer() {
        Customer customer = tblCustomers.getSelectionModel().getSelectedItem();
        if (customer == null) {
            UiSupport.showWarning("No Customer Selected", "Choose a customer to delete.");
            return;
        }
        if (!UiSupport.confirm("Delete Customer", "Delete " + customer.getName() + " and their linked data?")) {
            return;
        }
        UiSupport.runAsync("customer-delete", () -> bankingService.deleteCustomer(customer.getCustomerId()),
            ignored -> refreshAllAdminData(),
            error -> UiSupport.showError("Delete Customer Failed", error.getMessage()));
    }

    @FXML
    private void handleCreateAccount() {
        String name = txtAccountCustomerName.getText().trim();
        String phone = txtAccountPhone.getText().trim();
        String email = txtAccountEmail.getText().trim();
        String pin = txtAccountPin.getText().trim();
        String accountType = cmbAccountType.getValue();

        if (name.isBlank() || phone.isBlank() || email.isBlank() || accountType == null || !pin.matches("\\d{4,6}")) {
            UiSupport.showWarning("Missing Account Details", "Enter customer details, account type, and a 4-6 digit PIN.");
            return;
        }

        BigDecimal initialDeposit;
        try {
            initialDeposit = new BigDecimal(txtAccountInitialDeposit.getText().trim().isEmpty() ? "0" : txtAccountInitialDeposit.getText().trim());
        } catch (Exception exception) {
            UiSupport.showError("Invalid Deposit", "Enter a valid initial deposit amount.");
            return;
        }

        UiSupport.runAsync("account-create", () -> {
            Customer matchedCustomer = findMatchingCustomer(phone, email);
            Customer customer = matchedCustomer != null ? matchedCustomer : bankingService.createCustomer(name, phone, email, "");
            Account account = bankingService.createAccount(customer.getCustomerId(), Account.AccountType.valueOf(accountType), pin);
            if (initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
                bankingService.deposit(account.getAccountNumber(), initialDeposit, currentUser.getUserId());
            }
            return account;
        }, account -> {
            UiSupport.showInfo("Account Created", "New account number: " + account.getAccountNumber());
            clearAccountForm();
            refreshAllAdminData();
        }, error -> UiSupport.showError("Create Account Failed", error.getMessage()));
    }

    @FXML
    private void handleCloseSelectedAccount() {
        AccountRow row = tblAccounts.getSelectionModel().getSelectedItem();
        if (row == null) {
            UiSupport.showWarning("No Account Selected", "Choose an account to close.");
            return;
        }
        if (!UiSupport.confirm("Close Account", "Close account " + row.accountNumber + "? The account balance must already be zero.")) {
            return;
        }
        UiSupport.runAsync("account-close", () -> bankingService.closeAccount(row.accountNumber),
            ignored -> refreshAllAdminData(),
            error -> UiSupport.showError("Close Account Failed", error.getMessage()));
    }

    @FXML
    private void handleRefreshLoans() {
        refreshLoans();
    }

    @FXML
    private void handleApproveLoan() {
        Loan loan = tblLoans.getSelectionModel().getSelectedItem();
        if (loan == null) {
            UiSupport.showWarning("No Loan Selected", "Choose a pending loan to approve.");
            return;
        }
        UiSupport.runAsync("loan-approve", () -> loanService.approveLoanWithCredit(loan.getLoanId()),
            ignored -> refreshAllAdminData(),
            error -> UiSupport.showError("Loan Approval Failed", error.getMessage()));
    }

    @FXML
    private void handleRejectLoan() {
        Loan loan = tblLoans.getSelectionModel().getSelectedItem();
        if (loan == null) {
            UiSupport.showWarning("No Loan Selected", "Choose a pending loan to reject.");
            return;
        }
        UiSupport.runAsync("loan-reject", () -> loanService.rejectLoan(loan.getLoanId()),
            ignored -> refreshAllAdminData(),
            error -> UiSupport.showError("Loan Rejection Failed", error.getMessage()));
    }

    @FXML
    private void handleSearchTransactions() {
        String account = cmbTransactionAccount.getValue();
        String fromText = txtTransactionFrom.getText().trim();
        String toText = txtTransactionTo.getText().trim();

        UiSupport.runAsync("transaction-search", () -> {
            boolean hasAccount = account != null && !"ALL".equals(account);
            boolean hasRange = !fromText.isEmpty() && !toText.isEmpty();
            if (hasAccount && hasRange) {
                return bankingService.getTransactionsByDateRange(account, LocalDate.parse(fromText), LocalDate.parse(toText));
            }
            if (hasAccount) {
                return bankingService.getTransactionHistory(account);
            }
            if (hasRange) {
                return bankingService.getAllTransactionsByDateRange(LocalDate.parse(fromText), LocalDate.parse(toText));
            }
            return bankingService.getAllTransactions();
        }, transactions -> tblTransactions.setItems(FXCollections.observableArrayList(transactions)),
            error -> UiSupport.showError("Transaction Search Failed", error.getMessage()));
    }

    @FXML
    private void handleLastFiveTransactions() {
        String account = cmbTransactionAccount.getValue();
        if (account == null || "ALL".equals(account)) {
            UiSupport.showWarning("Specific Account Required", "Choose a single account to load the last five transactions.");
            return;
        }
        UiSupport.runAsync("transaction-last-five", () -> bankingService.getMiniStatement(account),
            transactions -> tblTransactions.setItems(FXCollections.observableArrayList(transactions)),
            error -> UiSupport.showError("Mini Statement Failed", error.getMessage()));
    }

    @FXML
    private void handleShowAllTransactions() {
        txtTransactionFrom.clear();
        txtTransactionTo.clear();
        cmbTransactionAccount.setValue("ALL");
        handleSearchTransactions();
    }

    @FXML
    private void handleCreateUser() {
        String username = txtSystemUsername.getText().trim();
        String password = txtSystemPassword.getText();
        String confirm = txtSystemConfirm.getText();
        User.UserRole role = cmbSystemRole.getValue();
        Customer customer = cmbSystemCustomer.getValue();

        if (username.isBlank() || password.isBlank() || role == null) {
            UiSupport.showWarning("Missing User Details", "Username, password, and role are required.");
            return;
        }
        if (!password.equals(confirm)) {
            UiSupport.showWarning("Password Mismatch", "Password confirmation does not match.");
            return;
        }

        Integer customerId = role == User.UserRole.USER && customer != null ? customer.getCustomerId() : null;
        UiSupport.runAsync("user-create", () -> authService.createUser(username, password, role, customerId),
            ignored -> {
                clearUserForm();
                refreshAllAdminData();
            },
            error -> UiSupport.showError("Create User Failed", error.getMessage()));
    }

    @FXML
    private void handleDeleteUser() {
        User user = tblUsers.getSelectionModel().getSelectedItem();
        if (user == null) {
            UiSupport.showWarning("No User Selected", "Choose a user to delete.");
            return;
        }
        if (currentUser != null && currentUser.getUserId() == user.getUserId()) {
            UiSupport.showWarning("Action Blocked", "You cannot delete your own admin account.");
            return;
        }
        if (!UiSupport.confirm("Delete User", "Delete system user " + user.getUsername() + "?")) {
            return;
        }
        UiSupport.runAsync("user-delete", () -> authService.deleteUser(user.getUserId()),
            ignored -> refreshAllAdminData(),
            error -> UiSupport.showError("Delete User Failed", error.getMessage()));
    }

    private void refreshAllAdminData() {
        UiSupport.runAsync("admin-dashboard-load", this::loadAdminState, state -> {
            customersById = new HashMap<>();
            for (Customer customer : state.customers) {
                customersById.put(customer.getCustomerId(), customer);
            }

            renderOverview(state);
            tblCustomers.setItems(FXCollections.observableArrayList(state.customers));
            tblAccounts.setItems(FXCollections.observableArrayList(buildAccountRows(state.accounts, state.customers)));
            tblLoans.setItems(FXCollections.observableArrayList(state.pendingLoans));
            tblTransactions.setItems(FXCollections.observableArrayList(state.transactions));
            tblUsers.setItems(FXCollections.observableArrayList(state.users));

            List<String> accountNumbers = new ArrayList<>();
            accountNumbers.add("ALL");
            for (Account account : state.accounts) {
                accountNumbers.add(account.getAccountNumber());
            }
            cmbTransactionAccount.setItems(FXCollections.observableArrayList(accountNumbers));
            if (cmbTransactionAccount.getValue() == null) {
                cmbTransactionAccount.setValue("ALL");
            }

            cmbSystemCustomer.setItems(FXCollections.observableArrayList(state.customers));
            refreshLoans();
            updateUserCustomerVisibility();
        }, error -> UiSupport.showError("Admin Dashboard Failed", error.getMessage()));
    }

    private AdminState loadAdminState() throws Exception {
        Map<String, Object> dashboardSummary = analyticsService.getDashboardSummary();
        List<Customer> customers = bankingService.getAllCustomers();
        List<Account> accounts = bankingService.getAllAccounts();
        List<Loan> pendingLoans = loanService.getPendingLoans();
        List<Transaction> transactions = bankingService.getAllTransactions();
        transactions.sort(Comparator.comparing(Transaction::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        List<User> users = authService.getAllUsers();
        return new AdminState(dashboardSummary, customers, accounts, pendingLoans, transactions, users);
    }

    private void renderOverview(AdminState state) {
        BigDecimal totalBalance = state.dashboardSummary.get("totalBalance") instanceof BigDecimal value ? value : BigDecimal.ZERO;
        int totalAccounts = state.dashboardSummary.get("totalAccounts") instanceof Number value ? value.intValue() : 0;

        lblTotalAssets.setText(UiSupport.formatCurrency(totalBalance));
        lblTotalAccounts.setText(String.valueOf(totalAccounts));
        lblPendingLoans.setText(String.valueOf(state.pendingLoans.size()));
        lblTotalCustomers.setText(String.valueOf(state.customers.size()));

        tblAdminRecentTransactions.setItems(FXCollections.observableArrayList(state.transactions.stream().limit(8).toList()));
        renderTransactionChart(state.transactions);
        renderAccountTypeChart(state.accounts);
    }

    private void renderTransactionChart(List<Transaction> transactions) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String type = transaction.getType().name();
            totals.put(type, totals.getOrDefault(type, BigDecimal.ZERO).add(transaction.getAmount()));
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String type : List.of("DEPOSIT", "WITHDRAW", "TRANSFER", "LOAN_CREDIT")) {
            series.getData().add(new XYChart.Data<>(type, totals.getOrDefault(type, BigDecimal.ZERO).doubleValue()));
        }
        chartAdminTransactions.getData().setAll(series);
    }

    private void renderAccountTypeChart(List<Account> accounts) {
        long savings = accounts.stream().filter(account -> account.getAccountType() == Account.AccountType.SAVINGS).count();
        long current = accounts.stream().filter(account -> account.getAccountType() == Account.AccountType.CURRENT).count();
        chartAccountTypes.setData(FXCollections.observableArrayList(
            new PieChart.Data("Savings", savings),
            new PieChart.Data("Current", current)
        ));
    }

    private void refreshLoans() {
        UiSupport.runAsync("loan-refresh", loanService::getPendingLoans,
            loans -> tblLoans.setItems(FXCollections.observableArrayList(loans)),
            error -> UiSupport.showError("Loan Refresh Failed", error.getMessage()));
    }

    private List<AccountRow> buildAccountRows(List<Account> accounts, List<Customer> customers) {
        Map<Integer, String> customerNames = new HashMap<>();
        for (Customer customer : customers) {
            customerNames.put(customer.getCustomerId(), customer.getName());
        }
        List<AccountRow> rows = new ArrayList<>();
        for (Account account : accounts) {
            rows.add(new AccountRow(
                account.getAccountNumber(),
                customerNames.getOrDefault(account.getCustomerId(), "Customer #" + account.getCustomerId()),
                account.getAccountType() == null ? "SAVINGS" : account.getAccountType().name(),
                UiSupport.formatCurrency(account.getBalance())
            ));
        }
        return rows;
    }

    private void configureTables() {
        colAdminRecentDate.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatTimestamp(data.getValue().getCreatedAt())));
        colAdminRecentType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colAdminRecentAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getAmount())));

        colCustomerId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCustomerId())));
        colCustomerName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colCustomerPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colCustomerEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colCustomerCibil.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCibilScore())));

        colAccountNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().accountNumber));
        colAccountCustomer.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().customerName));
        colAccountType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().accountType));
        colAccountBalance.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().balance));

        colLoanId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getLoanId())));
        colLoanCustomerId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getCustomerId())));
        colLoanAccount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNumber()));
        colLoanType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLoanType()));
        colLoanAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getLoanAmount())));
        colLoanStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        colTransactionId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTransactionId())));
        colTransactionAccount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccountNumber()));
        colTransactionType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType().name()));
        colTransactionAmount.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatCurrency(data.getValue().getAmount())));
        colTransactionDate.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatTimestamp(data.getValue().getCreatedAt())));

        colUserId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getUserId())));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colUserRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole().name()));
        colUserCreated.setCellValueFactory(data -> new SimpleStringProperty(UiSupport.formatDate(data.getValue().getCreatedAt())));
    }

    private void populateCustomerForm(Customer customer) {
        selectedCustomer = customer;
        if (customer == null) {
            return;
        }
        txtCustomerName.setText(customer.getName());
        txtCustomerPhone.setText(customer.getPhone());
        txtCustomerEmail.setText(customer.getEmail());
    }

    private Customer findMatchingCustomer(String phone, String email) {
        for (Customer customer : customersById.values()) {
            if ((phone != null && !phone.isBlank() && phone.equalsIgnoreCase(customer.getPhone()))
                || (email != null && !email.isBlank() && email.equalsIgnoreCase(customer.getEmail()))) {
                return customer;
            }
        }
        return null;
    }

    private void clearAccountForm() {
        txtAccountCustomerName.clear();
        txtAccountPhone.clear();
        txtAccountEmail.clear();
        txtAccountInitialDeposit.clear();
        txtAccountPin.clear();
        cmbAccountType.setValue("SAVINGS");
    }

    private void clearUserForm() {
        txtSystemUsername.clear();
        txtSystemPassword.clear();
        txtSystemConfirm.clear();
        cmbSystemRole.setValue(User.UserRole.USER);
        if (!cmbSystemCustomer.getItems().isEmpty()) {
            cmbSystemCustomer.getSelectionModel().selectFirst();
        }
    }

    private void updateUserCustomerVisibility() {
        boolean isUserRole = cmbSystemRole.getValue() == User.UserRole.USER;
        cmbSystemCustomer.setDisable(!isUserRole);
    }

    private void showPane(VBox targetPane, Button activeButton) {
        List<VBox> panes = List.of(paneOverview, paneCustomers, paneAccounts, paneLoans, paneTransactions, paneUsers);
        for (VBox pane : panes) {
            boolean active = pane == targetPane;
            pane.setManaged(active);
            pane.setVisible(active);
        }
        UiSupport.markActive(activeButton,
            btnNavOverview, btnNavCustomers, btnNavAccounts, btnNavLoans, btnNavTransactions, btnNavUsers);
    }

    private static final class AdminState {
        private final Map<String, Object> dashboardSummary;
        private final List<Customer> customers;
        private final List<Account> accounts;
        private final List<Loan> pendingLoans;
        private final List<Transaction> transactions;
        private final List<User> users;

        private AdminState(Map<String, Object> dashboardSummary, List<Customer> customers, List<Account> accounts,
                           List<Loan> pendingLoans, List<Transaction> transactions, List<User> users) {
            this.dashboardSummary = dashboardSummary;
            this.customers = customers;
            this.accounts = accounts;
            this.pendingLoans = pendingLoans;
            this.transactions = transactions;
            this.users = users;
        }
    }

    public static final class AccountRow {
        private final String accountNumber;
        private final String customerName;
        private final String accountType;
        private final String balance;

        private AccountRow(String accountNumber, String customerName, String accountType, String balance) {
            this.accountNumber = accountNumber;
            this.customerName = customerName;
            this.accountType = accountType;
            this.balance = balance;
        }
    }
}
