package banking.ui.panels;

import banking.model.Account;
import banking.model.User;
import banking.service.BankingService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;
import banking.ui.user.TransactionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern dashboard panel for standard users.
 * Shows account selection, balance, and quick action buttons.
 */
public class UserOverviewPanel extends JPanel implements Refreshable {
    private final User currentUser;
    private final BankingService bankingService;
    private final Frame parentFrame;
    
    private JComboBox<String> accountSelector;
    private JLabel lblBalance;
    private JLabel lblWelcome;
    private JLabel lblCibil;
    private JLabel lblAccountType;

    public UserOverviewPanel(User user, BankingService service, Frame parent) {
        this.currentUser = user;
        this.bankingService = service;
        this.parentFrame = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(25, 25));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Account Overview Card ---
        ModernUIComponents.RoundedPanel accountCard = 
            new ModernUIComponents.RoundedPanel(20, Color.WHITE);
        accountCard.setLayout(new BorderLayout(20, 20));
        accountCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        userInfoPanel.setBackground(Color.WHITE);
        
        lblWelcome = new JLabel("Welcome back, " + currentUser.getUsername());
        lblWelcome.setFont(UIStyle.HEADER_FONT);
        lblWelcome.setForeground(UIStyle.TEXT_COLOR);
        userInfoPanel.add(lblWelcome);
        
        lblCibil = new JLabel("CIBIL Score: Loading...");
        lblCibil.setFont(UIStyle.LABEL_FONT);
        lblCibil.setForeground(UIStyle.TEXT_LIGHT);
        userInfoPanel.add(lblCibil);
        
        topRow.add(userInfoPanel, BorderLayout.WEST);
        
        accountSelector = new JComboBox<>();
        UIStyle.styleComboBox(accountSelector);
        accountSelector.setPreferredSize(new Dimension(200, 35));
        accountSelector.addActionListener(e -> {
            if (accountSelector.getSelectedItem() != null) {
                updateBalanceDisplay((String) accountSelector.getSelectedItem());
            }
        });
        topRow.add(accountSelector, BorderLayout.EAST);
        
        accountCard.add(topRow, BorderLayout.NORTH);
        
        lblBalance = new JLabel("$0.00", SwingConstants.CENTER);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 64));
        lblBalance.setForeground(UIStyle.ACCENT_COLOR);
        accountCard.add(lblBalance, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        southPanel.setBackground(Color.WHITE);
        JLabel lblStatus = new JLabel("Available Balance", SwingConstants.CENTER);
        lblStatus.setFont(UIStyle.LABEL_FONT);
        lblStatus.setForeground(UIStyle.TEXT_LIGHT);
        southPanel.add(lblStatus);
        
        lblAccountType = new JLabel("Account Type: --", SwingConstants.CENTER);
        lblAccountType.setFont(UIStyle.SMALL_FONT);
        lblAccountType.setForeground(UIStyle.TEXT_LIGHT);
        southPanel.add(lblAccountType);
        accountCard.add(southPanel, BorderLayout.SOUTH);

        add(accountCard, BorderLayout.NORTH);

        // --- Quick Actions ---
        JPanel actionsWrapper = new JPanel(new BorderLayout(0, 25));
        actionsWrapper.setBackground(UIStyle.BACKGROUND_COLOR);

        JPanel actionsGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        actionsGrid.setBackground(UIStyle.BACKGROUND_COLOR);
        
        JButton btnDeposit = new JButton("\u2193  QUICK DEPOSIT");
        UIStyle.styleSuccessButton(btnDeposit);
        btnDeposit.addActionListener(e -> openTransaction("DEPOSIT"));
        actionsGrid.add(btnDeposit);
        
        JButton btnWithdraw = new JButton("\u2191  QUICK WITHDRAW");
        UIStyle.stylePrimaryButton(btnWithdraw);
        btnWithdraw.addActionListener(e -> openTransaction("WITHDRAW"));
        actionsGrid.add(btnWithdraw);

        JButton btnTransfer = new JButton("\u21C4  SEND MONEY (TRANSFER)");
        UIStyle.styleButton(btnTransfer, UIStyle.SECONDARY_COLOR);
        btnTransfer.setPreferredSize(new Dimension(0, 60));
        btnTransfer.addActionListener(e -> openTransaction("TRANSFER"));

        JButton btnLoan = new JButton("\uD83D\uDCB5  REQUEST LOAN");
        UIStyle.styleButton(btnLoan, UIStyle.PRIMARY_COLOR);
        btnLoan.setPreferredSize(new Dimension(0, 60));
        btnLoan.addActionListener(e -> requestLoan());

        JButton btnSetPin = new JButton("\uD83D\uDD12  SET / CHANGE TRANSACTION PIN");
        UIStyle.styleButton(btnSetPin, new Color(100, 100, 100));
        btnSetPin.setPreferredSize(new Dimension(0, 60));
        btnSetPin.addActionListener(e -> setTransactionPin());

        JPanel bottomActions = new JPanel(new GridLayout(3, 1, 0, 15));
        bottomActions.setBackground(UIStyle.BACKGROUND_COLOR);
        bottomActions.add(btnTransfer);
        bottomActions.add(btnLoan);
        bottomActions.add(btnSetPin);
        
        actionsWrapper.add(actionsGrid, BorderLayout.NORTH);
        actionsWrapper.add(bottomActions, BorderLayout.CENTER);
        
        add(actionsWrapper, BorderLayout.CENTER);
    }

    private void openTransaction(String type) {
        String acc = (String) accountSelector.getSelectedItem();
        if (acc == null || acc.isEmpty()) {
            UIStyle.showWarning(this, "Please select an account first.");
            return;
        }
        TransactionDialog dialog = new TransactionDialog(parentFrame, acc, type, currentUser.getUserId());
        dialog.setVisible(true);
        onActivated(); // Refresh after transaction
    }

    private void setTransactionPin() {
        String acc = (String) accountSelector.getSelectedItem();
        if (acc == null || acc.isEmpty()) {
            UIStyle.showWarning(this, "Please select an account first.");
            return;
        }
        JPasswordField pinField = new JPasswordField();
        int result = JOptionPane.showConfirmDialog(parentFrame, pinField,
                "Enter new Transaction PIN (4-6 digits):", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String pin = new String(pinField.getPassword()).trim();
        if (!pin.matches("\\d{4,6}")) {
            UIStyle.showError(this, "Transaction PIN must be 4 to 6 digits.");
            return;
        }

        try {
            bankingService.setTransactionPassword(acc, pin, currentUser.getUserId());
            UIStyle.showSuccess(this, "Transaction PIN set successfully for account " + acc);
        } catch (Exception ex) {
            UIStyle.showError(this, "Failed to set PIN: " + ex.getMessage());
        }
    }

    private void requestLoan() {
        JTextField amountField = new JTextField();
        JTextField incomeField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"PERSONAL", "STUDENT", "HOME", "AUTO"});
        JComboBox<String> durationBox = new JComboBox<>(new String[]{"12", "24", "36", "48", "60"});

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Loan Amount ($):"));
        panel.add(amountField);
        panel.add(new JLabel("Monthly Income ($):"));
        panel.add(incomeField);
        panel.add(new JLabel("Loan Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Duration (months):"));
        panel.add(durationBox);

        int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Request Loan", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(amountField.getText().trim());
            java.math.BigDecimal monthlyIncome = new java.math.BigDecimal(incomeField.getText().trim());
            int duration = Integer.parseInt((String) durationBox.getSelectedItem());
            String loanType = (String) typeBox.getSelectedItem();

            banking.model.Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
            if (customer == null) {
                throw new Exception("Customer not found");
            }

            banking.service.LoanService loanService = new banking.service.LoanService();
            banking.model.LoanDecision decision = loanService.submitLoanRequest(
                customer.getCustomerId(),
                customer.getCibilScore(),
                monthlyIncome,
                amount,
                loanType,
                duration
            );

            JOptionPane.showMessageDialog(parentFrame,
                "Loan Status: " + decision.getStatus() + "\n" +
                    "Interest Rate: " + decision.getInterestRate() + "%\n" +
                    "Loan Duration: " + decision.getLoanDuration() + " months\n" +
                    "Monthly EMI: $" + decision.getEmi() + "\n" +
                    "Reason: " + decision.getReason(),
                "Loan Decision",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parentFrame, "Error: " + ex.getMessage(), "Loan Request Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBalanceDisplay(String accNum) {
        new SwingWorker<Account, Void>() {
            @Override
            protected Account doInBackground() throws Exception {
                return bankingService.getAccount(accNum);
            }
            @Override
            protected void done() {
                try {
                    Account acc = get();
                    lblBalance.setText(banking.util.Validator.formatCurrency(acc.getBalance()));
                    String typeStr = acc.getAccountType() != null ? acc.getAccountType().name() : "SAVINGS";
                    lblAccountType.setText("Account Type: " + typeStr);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    @Override
    public void onActivated() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                banking.model.Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
                if (customer != null) {
                    return bankingService.getAccountsByCustomer(customer.getCustomerId());
                }
                return new java.util.ArrayList<>();
            }
            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    banking.model.Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
                    if (customer != null) {
                        lblCibil.setText("CIBIL Score: " + customer.getCibilScore());
                    }
                    String selected = (String) accountSelector.getSelectedItem();
                    accountSelector.removeAllItems();
                    for (Account acc : accounts) {
                        accountSelector.addItem(acc.getAccountNumber());
                    }
                    if (selected != null) {
                        accountSelector.setSelectedItem(selected);
                    } else if (!accounts.isEmpty()) {
                        accountSelector.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    accountSelector.addItem("Error loading accounts");
                }
            }
        }.execute();
    }
}
