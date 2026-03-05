package banking.ui.panels;

import banking.model.Account;
import banking.model.Customer;
import banking.service.BankingService;
import banking.ui.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

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

    public AccountFormDialog(Window parent, BankingService bankingService) {
        super(parent, "Create New Account", ModalityType.APPLICATION_MODAL);
        this.bankingService = bankingService;
        initializeUI();
    }

    private void initializeUI() {
        setSize(450, 450);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

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
        panel.add(lblTitle, gbc);

        // Customer Name
        gbc.gridy++;
        panel.add(createLabel("Customer Name *"), gbc);
        gbc.gridy++;
        txtName = new JTextField();
        UIStyle.styleTextField(txtName);
        panel.add(txtName, gbc);

        // Phone
        gbc.gridy++;
        panel.add(createLabel("Phone Number *"), gbc);
        gbc.gridy++;
        txtPhone = new JTextField();
        UIStyle.styleTextField(txtPhone);
        panel.add(txtPhone, gbc);

        // Email
        gbc.gridy++;
        panel.add(createLabel("Email Address *"), gbc);
        gbc.gridy++;
        txtEmail = new JTextField();
        UIStyle.styleTextField(txtEmail);
        panel.add(txtEmail, gbc);

        // Initial deposit
        gbc.gridy++;
        panel.add(createLabel("Initial Deposit ($)"), gbc);
        gbc.gridy++;
        txtInitialDeposit = new JTextField("0.00");
        UIStyle.styleTextField(txtInitialDeposit);
        panel.add(txtInitialDeposit, gbc);

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        UIStyle.styleButton(btnCancel, UIStyle.SECONDARY_COLOR);
        btnCancel.addActionListener(e -> dispose());

        JButton btnCreate = new JButton("Create Account");
        UIStyle.styleButton(btnCreate, UIStyle.ACCENT_COLOR);
        btnCreate.addActionListener(e -> handleCreate());

        btnPanel.add(btnCancel);
        btnPanel.add(btnCreate);
        panel.add(btnPanel, gbc);

        add(panel);
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

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            UIStyle.showError(this, "Please fill in all customer details.");
            return;
        }

        BigDecimal initialDeposit;
        try {
            initialDeposit = new java.math.BigDecimal(txtInitialDeposit.getText().trim());
            if (initialDeposit.compareTo(java.math.BigDecimal.ZERO) < 0) {
                UIStyle.showError(this, "Initial deposit cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            UIStyle.showError(this, "Please enter a valid numeric amount.");
            return;
        }

        final BigDecimal deposit = initialDeposit;
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                banking.dao.CustomerDAO customerDAO = new banking.dao.CustomerDAOImpl();
                Customer customer = new Customer(name, phone, email);
                int customerId = customerDAO.create(customer);
                
                if (customerId <= 0) {
                    throw new Exception("Failed to create customer.");
                }

                Account account = bankingService.createAccount(customerId);
                if (deposit.compareTo(java.math.BigDecimal.ZERO) > 0) {
                    bankingService.deposit(account.getAccountNumber(), deposit);
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
