package banking.ui.panels;

import banking.model.Account;
import banking.model.Customer;
import banking.service.BankingService;
import banking.ui.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Modal dialog for creating a new bank account linked to an existing customer.
 */
public class AccountFormDialog extends JDialog {
    private final BankingService bankingService;
    private boolean success = false;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtInitialDeposit;
    private JComboBox<String> cmbAccountType;
    private JPasswordField txtTransactionPin;

    public AccountFormDialog(Window parent, BankingService bankingService) {
        super(parent, "Create New Account", ModalityType.APPLICATION_MODAL);
        this.bankingService = bankingService;
        initializeUI();
    }

    private void initializeUI() {
        setSize(480, 700);
        setLocationRelativeTo(getOwner());
        setResizable(true); // Allow resizing in case of smaller screens
        setLayout(new BorderLayout());

        // --- Main Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Title
        gbc.gridy = 0;
        JLabel lblTitle = new JLabel("New Account & Customer");
        lblTitle.setFont(UIStyle.HEADER_FONT);
        lblTitle.setForeground(UIStyle.TEXT_COLOR);
        formPanel.add(lblTitle, gbc);

        // Customer Name
        gbc.gridy++;
        formPanel.add(createLabel("Customer Name *"), gbc);
        gbc.gridy++;
        txtName = new JTextField();
        UIStyle.styleTextField(txtName);
        formPanel.add(txtName, gbc);

        // Phone
        gbc.gridy++;
        formPanel.add(createLabel("Phone Number *"), gbc);
        gbc.gridy++;
        txtPhone = new JTextField();
        UIStyle.styleTextField(txtPhone);
        formPanel.add(txtPhone, gbc);

        // Email
        gbc.gridy++;
        formPanel.add(createLabel("Email Address *"), gbc);
        gbc.gridy++;
        txtEmail = new JTextField();
        UIStyle.styleTextField(txtEmail);
        formPanel.add(txtEmail, gbc);

        // Account Type
        gbc.gridy++;
        formPanel.add(createLabel("Account Type *"), gbc);
        gbc.gridy++;
        cmbAccountType = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
        cmbAccountType.setFont(UIStyle.SMALL_FONT);
        formPanel.add(cmbAccountType, gbc);

        // Transaction PIN
        gbc.gridy++;
        formPanel.add(createLabel("Transaction PIN (4-6 digits) *"), gbc);
        gbc.gridy++;
        txtTransactionPin = new JPasswordField();
        UIStyle.styleTextField(txtTransactionPin);
        formPanel.add(txtTransactionPin, gbc);

        // Initial deposit
        gbc.gridy++;
        formPanel.add(createLabel("Initial Deposit ($)"), gbc);
        gbc.gridy++;
        txtInitialDeposit = new JTextField("0.00");
        UIStyle.styleTextField(txtInitialDeposit);
        formPanel.add(txtInitialDeposit, gbc);
        
        // Spacer at the bottom of GridBag to push everything up
        gbc.gridy++;
        gbc.weighty = 1.0;
        formPanel.add(Box.createVerticalGlue(), gbc);

        // Wrap form in ScrollPane
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // --- Button Panel ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnPanel.setBackground(new Color(248, 250, 252));
        btnPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JButton btnCancel = new JButton("Cancel");
        UIStyle.styleButton(btnCancel, UIStyle.SECONDARY_COLOR);
        btnCancel.addActionListener(e -> dispose());

        JButton btnCreate = new JButton("Create Account");
        UIStyle.styleButton(btnCreate, UIStyle.ACCENT_COLOR);
        btnCreate.addActionListener(e -> handleCreate());

        btnPanel.add(btnCancel);
        btnPanel.add(btnCreate);

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIStyle.SMALL_FONT);
        lbl.setForeground(UIStyle.TEXT_LIGHT);
        return lbl;
    }

    private void handleCreate() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String pin = new String(txtTransactionPin.getPassword()).trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            UIStyle.showError(this, "Please fill in all customer details.");
            return;
        }

        // Validate transaction PIN
        if (!pin.matches("\\d{4,6}")) {
            UIStyle.showError(this, "Transaction PIN must be 4 to 6 digits.");
            return;
        }

        Account.AccountType accountType = cmbAccountType.getSelectedIndex() == 0
                ? Account.AccountType.SAVINGS : Account.AccountType.CURRENT;

        BigDecimal initialDeposit;
        try {
            initialDeposit = new BigDecimal(txtInitialDeposit.getText().trim());
            if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
                UIStyle.showError(this, "Initial deposit cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            UIStyle.showError(this, "Please enter a valid numeric amount.");
            return;
        }

        final BigDecimal deposit = initialDeposit;
        final String transactionPin = pin;
        final Account.AccountType acctType = accountType;

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Use BankingService to ensure User record and credentials are created
                Customer customer = bankingService.createCustomer(name, phone, email, "");
                
                if (customer == null || customer.getCustomerId() <= 0) {
                    throw new Exception("Failed to create customer and user login.");
                }

                Account account = bankingService.createAccount(customer.getCustomerId(), acctType, transactionPin);
                if (deposit.compareTo(BigDecimal.ZERO) > 0) {
                    // userId 0 indicates admin/system deposit during account creation
                    bankingService.deposit(account.getAccountNumber(), deposit, 0);
                }
                return account.getAccountNumber();
            }

            @Override
            protected void done() {
                try {
                    String accNum = get();
                    success = true;
                    JOptionPane.showMessageDialog(AccountFormDialog.this,
                            "Account & Customer created successfully!\n\nUser Access Credential (Account Number):\n" + accNum,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    UIStyle.showError(AccountFormDialog.this, e.getMessage());
                }
            }
        }.execute();
    }

    public boolean isSuccess() {
        return success;
    }
}
