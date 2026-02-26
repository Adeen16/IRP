package banking.ui.panels;

import banking.model.Customer;
import banking.service.BankingService;
import banking.ui.UIStyle;
import banking.util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for adding or editing a customer.
 */
public class CustomerFormDialog extends JDialog {
    private final BankingService bankingService;
    private final Customer existing; // null = add mode
    private boolean success = false;

    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;


    public CustomerFormDialog(Window parent, BankingService bankingService, Customer existing) {
        super(parent, existing == null ? "Add Customer" : "Edit Customer", ModalityType.APPLICATION_MODAL);
        this.bankingService = bankingService;
        this.existing = existing;
        initializeUI();
    }

    private void initializeUI() {
        setSize(450, 400);
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
        JLabel lblTitle = new JLabel(existing == null ? "New Customer" : "Edit Customer");
        lblTitle.setFont(UIStyle.HEADER_FONT);
        lblTitle.setForeground(UIStyle.TEXT_COLOR);
        panel.add(lblTitle, gbc);

        // Name
        gbc.gridy++;
        panel.add(createLabel("Full Name *"), gbc);
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



        // Pre-fill for edit mode
        if (existing != null) {
            txtName.setText(existing.getName());
            txtPhone.setText(existing.getPhone());
            txtEmail.setText(existing.getEmail());
        }

        // Buttons
        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Cancel");
        UIStyle.styleButton(btnCancel, UIStyle.SECONDARY_COLOR);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(existing == null ? "Add Customer" : "Save Changes");
        UIStyle.styleButton(btnSave, UIStyle.ACCENT_COLOR);
        btnSave.addActionListener(e -> handleSave());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        panel.add(btnPanel, gbc);

        add(panel);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIStyle.SMALL_FONT);
        lbl.setForeground(UIStyle.TEXT_LIGHT);
        return lbl;
    }

    private void handleSave() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        // Validate
        Validator.ValidationResult result = Validator.validateCustomer(name, phone, email);
        if (!result.isValid()) {
            UIStyle.showError(this, result.getMessage());
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (existing == null) {
                    bankingService.createCustomer(name, phone, email, "");
                } else {
                    existing.setName(name);
                    existing.setPhone(phone);
                    existing.setEmail(email);
                    bankingService.updateCustomer(existing);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    success = true;
                    UIStyle.showSuccess(CustomerFormDialog.this,
                            existing == null ? "Customer added successfully!" : "Customer updated!");
                    dispose();
                } catch (Exception e) {
                    UIStyle.showError(CustomerFormDialog.this, e.getMessage());
                }
            }
        }.execute();
    }

    public boolean isSuccess() {
        return success;
    }
}
