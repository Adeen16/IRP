package banking.ui.panels;

import banking.model.Customer;
import banking.model.User;
import banking.service.AuthService;
import banking.service.BankingService;
import banking.ui.UIStyle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for creating a new system user.
 * Allows linking to a Customer profile if the role is USER.
 */
public class UserFormDialog extends JDialog {
    private final AuthService authService;
    private final BankingService bankingService;
    private boolean success = false;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<User.UserRole> roleCombo;
    private JComboBox<Customer> customerCombo;
    private JLabel lblCustomer;

    public UserFormDialog(Window parent, AuthService authService, BankingService bankingService) {
        super(parent, "Create New User", ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        this.bankingService = bankingService;
        initializeUI();
        loadCustomers();
    }

    private void initializeUI() {
        setSize(420, 500);
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
        JLabel lblTitle = new JLabel("New System User");
        lblTitle.setFont(UIStyle.HEADER_FONT);
        lblTitle.setForeground(UIStyle.TEXT_COLOR);
        panel.add(lblTitle, gbc);

        // Username
        gbc.gridy++;
        panel.add(createLabel("Username *"), gbc);
        gbc.gridy++;
        txtUsername = new JTextField();
        UIStyle.styleTextField(txtUsername);
        panel.add(txtUsername, gbc);

        // Password
        gbc.gridy++;
        panel.add(createLabel("Password * (min 6 characters)"), gbc);
        gbc.gridy++;
        txtPassword = new JPasswordField();
        UIStyle.stylePasswordField(txtPassword);
        panel.add(txtPassword, gbc);

        // Confirm Password
        gbc.gridy++;
        panel.add(createLabel("Confirm Password *"), gbc);
        gbc.gridy++;
        txtConfirmPassword = new JPasswordField();
        UIStyle.stylePasswordField(txtConfirmPassword);
        panel.add(txtConfirmPassword, gbc);

        // Role
        gbc.gridy++;
        panel.add(createLabel("Role *"), gbc);
        gbc.gridy++;
        roleCombo = new JComboBox<>(User.UserRole.values());
        UIStyle.styleComboBox(roleCombo);
        roleCombo.addActionListener(e -> toggleCustomerSelection());
        panel.add(roleCombo, gbc);

        // Customer Selection (only for USER role)
        gbc.gridy++;
        lblCustomer = createLabel("Link to Customer Profile");
        panel.add(lblCustomer, gbc);
        gbc.gridy++;
        customerCombo = new JComboBox<>();
        UIStyle.styleComboBox(customerCombo);
        panel.add(customerCombo, gbc);

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        UIStyle.styleButton(btnCancel, UIStyle.SECONDARY_COLOR);
        btnCancel.addActionListener(e -> dispose());

        JButton btnCreate = new JButton("Create User");
        UIStyle.styleButton(btnCreate, UIStyle.ACCENT_COLOR);
        btnCreate.addActionListener(e -> handleCreate());

        btnPanel.add(btnCancel);
        btnPanel.add(btnCreate);
        panel.add(btnPanel, gbc);

        add(panel);
        toggleCustomerSelection();
    }

    private void toggleCustomerSelection() {
        boolean isUser = roleCombo.getSelectedItem() == User.UserRole.USER;
        lblCustomer.setVisible(isUser);
        customerCombo.setVisible(isUser);
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
                        customerCombo.addItem(c);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIStyle.SMALL_FONT);
        lbl.setForeground(UIStyle.TEXT_LIGHT);
        return lbl;
    }

    private void handleCreate() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());
        User.UserRole role = (User.UserRole) roleCombo.getSelectedItem();
        Customer customer = (Customer) customerCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            UIStyle.showError(this, "Username and Password are required.");
            return;
        }
        if (!password.equals(confirm)) {
            UIStyle.showError(this, "Passwords do not match.");
            return;
        }

        Integer customerId = (role == User.UserRole.USER && customer != null) ? customer.getCustomerId() : null;

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.createUser(username, password, role, customerId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    success = true;
                    UIStyle.showSuccess(UserFormDialog.this, "User created successfully!");
                    dispose();
                } catch (Exception e) {
                    UIStyle.showError(UserFormDialog.this, e.getMessage());
                }
            }
        }.execute();
    }

    public boolean isSuccess() {
        return success;
    }
}
