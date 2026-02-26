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
    private JComboBox<CustomerItem> customerCombo;

    private JTextField txtInitialDeposit;

    public AccountFormDialog(Window parent, BankingService bankingService) {
        super(parent, "Create New Account", ModalityType.APPLICATION_MODAL);
        this.bankingService = bankingService;
        initializeUI();
        loadCustomers();
    }

    private void initializeUI() {
        setSize(450, 380);
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
        JLabel lblTitle = new JLabel("New Account");
        lblTitle.setFont(UIStyle.HEADER_FONT);
        lblTitle.setForeground(UIStyle.TEXT_COLOR);
        panel.add(lblTitle, gbc);

        // Customer selection
        gbc.gridy++;
        panel.add(createLabel("Customer *"), gbc);
        gbc.gridy++;
        customerCombo = new JComboBox<>();
        UIStyle.styleComboBox(customerCombo);
        panel.add(customerCombo, gbc);



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

    private void loadCustomers() {
        new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                return bankingService.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    List<Customer> customers = get();
                    customerCombo.removeAllItems();
                    for (Customer c : customers) {
                        customerCombo.addItem(new CustomerItem(c.getCustomerId(), c.getName()));
                    }
                } catch (Exception e) {
                    UIStyle.showError(AccountFormDialog.this, "Failed to load customers: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void handleCreate() {
        CustomerItem selected = (CustomerItem) customerCombo.getSelectedItem();
        if (selected == null) {
            UIStyle.showError(this, "Please select a customer.");
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
                Account account = bankingService.createAccount(selected.id);
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
                    UIStyle.showSuccess(AccountFormDialog.this,
                            "Account created: " + accNum);
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

    /** Helper class for customer combo box items. */
    private static class CustomerItem {
        final int id;
        final String name;

        CustomerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }
}
