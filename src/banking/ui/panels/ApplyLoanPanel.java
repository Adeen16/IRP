package banking.ui.panels;

import banking.service.LoanService;
import banking.ui.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class ApplyLoanPanel extends JPanel {
    private final int customerId;
    private final LoanService loanService;
    
    private JTextField amountField;
    private JComboBox<String> termComboBox;
    
    public ApplyLoanPanel(int customerId) {
        this.customerId = customerId;
        this.loanService = new LoanService();
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Apply for a Loan");
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);
        add(titleLabel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel amountLabel = new JLabel("Loan Amount ($):");
        UIStyle.styleLabel(amountLabel);
        formPanel.add(amountLabel, gbc);
        
        amountField = new JTextField(15);
        UIStyle.styleTextField(amountField);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(amountField, gbc);
        
        // Term
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel termLabel = new JLabel("Repayment Term:");
        UIStyle.styleLabel(termLabel);
        formPanel.add(termLabel, gbc);
        
        String[] terms = {"12 Months", "24 Months", "36 Months", "48 Months", "60 Months"};
        termComboBox = new JComboBox<>(terms);
        UIStyle.styleComboBox(termComboBox);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(termComboBox, gbc);
        
        // Submit Button
        JButton applyButton = new JButton("Submit Application");
        UIStyle.stylePrimaryButton(applyButton);
        applyButton.addActionListener(e -> handleApplication());
        
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(applyButton, gbc);
        
        // Wrap form to prevent stretching
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setBackground(UIStyle.BACKGROUND_COLOR);
        wrapper.add(formPanel);
        
        add(wrapper, BorderLayout.CENTER);
    }
    
    private void handleApplication() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            int termMonths = Integer.parseInt(((String) termComboBox.getSelectedItem()).split(" ")[0]);
            
            boolean success = loanService.applyForLoan(customerId, amount, termMonths);
            if (success) {
                JOptionPane.showMessageDialog(this, 
                    "Loan application submitted successfully and is pending review.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                amountField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to submit application. Please try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid amount.", 
                "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }
}
