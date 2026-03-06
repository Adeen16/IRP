package banking.ui.user;

import banking.service.BankingService;
import banking.ui.UIStyle;
import banking.exception.BankingException;

import javax.swing.*;
import java.awt.*;

public class TransactionDialog extends JDialog {
    private String accountNumber;
    private BankingService bankingService;
    private JTextField txtAmount;
    private JTextField txtTargetAccount;
    private JPasswordField txtTransactionPin;
    private String type;
    private boolean success = false;
    private int userId;

    public TransactionDialog(Frame parent, String accountNumber, String type, int userId) {
        super(parent, type, true);
        this.accountNumber = accountNumber;
        this.type = type;
        this.userId = userId;
        this.bankingService = new BankingService();
        initializeUI();
    }

    private void initializeUI() {
        boolean needsPin = type.equals("WITHDRAW") || type.equals("TRANSFER");
        int height = type.equals("TRANSFER") ? (needsPin ? 420 : 350) : (needsPin ? 320 : 250);
        setSize(400, height);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 10, 5);

        // Account Info
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblAcc = new JLabel("Account: " + accountNumber);
        lblAcc.setFont(UIStyle.SMALL_FONT);
        panel.add(lblAcc, gbc);

        // Target Account (Only for Transfer)
        if (type.equals("TRANSFER")) {
            gbc.gridy++;
            panel.add(new JLabel("Recipient Account Number:"), gbc);
            gbc.gridy++;
            txtTargetAccount = new JTextField();
            UIStyle.styleTextField(txtTargetAccount);
            panel.add(txtTargetAccount, gbc);
        }

        // Amount
        gbc.gridy++;
        panel.add(new JLabel("Enter Amount ($):"), gbc);
        gbc.gridy++;
        txtAmount = new JTextField();
        UIStyle.styleTextField(txtAmount);
        panel.add(txtAmount, gbc);

        // Transaction PIN (for WITHDRAW and TRANSFER)
        if (needsPin) {
            gbc.gridy++;
            panel.add(new JLabel("Transaction PIN:"), gbc);
            gbc.gridy++;
            txtTransactionPin = new JPasswordField();
            UIStyle.styleTextField(txtTransactionPin);
            panel.add(txtTransactionPin, gbc);
        }

        // Action Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 5, 5, 5);
        JButton btnAction = new JButton(type);
        if (type.equals("WITHDRAW")) UIStyle.styleDangerButton(btnAction);
        else UIStyle.styleSuccessButton(btnAction);
        
        btnAction.addActionListener(e -> handleAction());
        panel.add(btnAction, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void handleAction() {
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(txtAmount.getText().trim());
            if (type.equals("DEPOSIT")) {
                bankingService.deposit(accountNumber, amount, userId);
            } else if (type.equals("WITHDRAW")) {
                String pin = new String(txtTransactionPin.getPassword()).trim();
                if (pin.isEmpty()) {
                    UIStyle.showError(this, "Please enter your transaction PIN.");
                    return;
                }
                bankingService.withdraw(accountNumber, amount, pin, userId);
            } else if (type.equals("TRANSFER")) {
                String pin = new String(txtTransactionPin.getPassword()).trim();
                if (pin.isEmpty()) {
                    UIStyle.showError(this, "Please enter your transaction PIN.");
                    return;
                }
                bankingService.transfer(accountNumber, txtTargetAccount.getText().trim(), amount, pin, userId);
            }
            success = true;
            UIStyle.showSuccess(this, type + " Successful!");
            dispose();
        } catch (NumberFormatException ex) {
            UIStyle.showError(this, "Please enter a valid numeric amount.");
        } catch (Exception ex) {
            UIStyle.showError(this, ex.getMessage());
        }
    }

    public boolean isSuccess() { return success; }
}
