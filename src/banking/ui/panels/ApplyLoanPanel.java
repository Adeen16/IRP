package banking.ui.panels;

import banking.model.LoanDecision;
import banking.service.BankingService;
import banking.service.LoanService;
import banking.ui.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ApplyLoanPanel extends JPanel {
    private final int customerId;
    private final LoanService loanService;
    private final BankingService bankingService;

    private JComboBox<String> accountSelector;
    private JTextField amountField;
    private JTextField incomeField;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> termComboBox;
    private JTextArea resultArea;

    public ApplyLoanPanel(int customerId) {
        this.customerId = customerId;
        this.loanService = new LoanService();
        this.bankingService = new BankingService();
        setupUI();
        loadAccounts();
    }

    private void loadAccounts() {
        try {
            var accounts = bankingService.getAccountsByCustomer(customerId);
            accountSelector.removeAllItems();
            for (var acc : accounts) {
                accountSelector.addItem(acc.getAccountNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Apply for a Loan");
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(UIStyle.TEXT_COLOR);
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel accountLabel = new JLabel("Select Account:");
        UIStyle.styleLabel(accountLabel);
        formPanel.add(accountLabel, gbc);

        accountSelector = new JComboBox<>();
        UIStyle.styleComboBox(accountSelector);
        gbc.gridx = 1;
        formPanel.add(accountSelector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel amountLabel = new JLabel("Loan Amount ($):");
        UIStyle.styleLabel(amountLabel);
        formPanel.add(amountLabel, gbc);

        amountField = new JTextField(15);
        UIStyle.styleTextField(amountField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel incomeLabel = new JLabel("Monthly Income ($):");
        UIStyle.styleLabel(incomeLabel);
        formPanel.add(incomeLabel, gbc);

        incomeField = new JTextField(15);
        UIStyle.styleTextField(incomeField);
        gbc.gridx = 1;
        formPanel.add(incomeField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel typeLabel = new JLabel("Loan Type:");
        UIStyle.styleLabel(typeLabel);
        formPanel.add(typeLabel, gbc);

        typeComboBox = new JComboBox<>(new String[]{"PERSONAL", "STUDENT", "HOME", "AUTO"});
        UIStyle.styleComboBox(typeComboBox);
        gbc.gridx = 1;
        formPanel.add(typeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel termLabel = new JLabel("Repayment Term:");
        UIStyle.styleLabel(termLabel);
        formPanel.add(termLabel, gbc);

        termComboBox = new JComboBox<>(new String[]{"12 Months", "24 Months", "36 Months", "48 Months", "60 Months"});
        UIStyle.styleComboBox(termComboBox);
        gbc.gridx = 1;
        formPanel.add(termComboBox, gbc);

        JButton applyButton = new JButton("Submit Application");
        UIStyle.stylePrimaryButton(applyButton);
        applyButton.addActionListener(e -> handleApplication());
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(applyButton, gbc);

        resultArea = new JTextArea(8, 28);
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        UIStyle.styleTextArea(resultArea);

        JPanel center = new JPanel(new BorderLayout(20, 20));
        center.setBackground(UIStyle.BACKGROUND_COLOR);
        center.add(formPanel, BorderLayout.NORTH);
        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        UIStyle.styleScrollPane(resultScrollPane);
        center.add(resultScrollPane, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void handleApplication() {
        String selectedAccount = (String) accountSelector.getSelectedItem();
        if (selectedAccount == null || selectedAccount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an account first.", "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            BigDecimal income = new BigDecimal(incomeField.getText().trim());
            int termMonths = Integer.parseInt(((String) termComboBox.getSelectedItem()).split(" ")[0]);
            String loanType = (String) typeComboBox.getSelectedItem();

            banking.model.Customer customer = bankingService.getCustomer(customerId);
            LoanDecision decision = loanService.submitLoanRequest(customerId, selectedAccount, customer.getCibilScore(), income, amount, loanType, termMonths);

            resultArea.setText(
                "Loan Status: " + decision.getStatus() + "\n" +
                "Interest Rate: " + decision.getInterestRate() + "%\n" +
                "Loan Duration: " + decision.getLoanDuration() + " months\n" +
                "Monthly EMI: $" + decision.getEmi() + "\n" +
                "Reason: " + decision.getReason()
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Loan Request Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
