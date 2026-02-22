package banking.ui.panels;

import banking.model.Account;
import banking.model.Transaction;
import banking.model.User;
import banking.service.BankingService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User-specific transaction history panel.
 */
public class UserStatementPanel extends JPanel implements Refreshable {
    private final User currentUser;
    private final BankingService bankingService;
    
    private JComboBox<String> accountCombo;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public UserStatementPanel(User user, BankingService service) {
        this.currentUser = user;
        this.bankingService = service;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.BACKGROUND_COLOR);
        
        JLabel title = new JLabel("Account Statement");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);
        header.add(title, BorderLayout.WEST);

        accountCombo = new JComboBox<>();
        UIStyle.styleComboBox(accountCombo);
        accountCombo.setPreferredSize(new Dimension(200, 35));
        accountCombo.addActionListener(e -> loadTransactions());
        header.add(accountCombo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"Date", "Description", "Type", "Amount", "Balance After"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        transactionTable = new JTable(tableModel);
        UIStyle.styleTable(transactionTable);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        ModernUIComponents.RoundedPanel tableCard = new ModernUIComponents.RoundedPanel(15, Color.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private void loadTransactions() {
        String accountNumber = (String) accountCombo.getSelectedItem();
        if (accountNumber == null || accountNumber.isEmpty()) return;

        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return bankingService.getTransactionHistory(accountNumber);
            }
            @Override
            protected void done() {
                try {
                    List<Transaction> txns = get();
                    tableModel.setRowCount(0);
                    for (Transaction t : txns) {
                        tableModel.addRow(new Object[]{
                            t.getTransactionDate().toString().substring(0, 16),
                            t.getDescription(),
                            t.getTransactionType().name(),
                            banking.util.Validator.formatCurrency(t.getAmount()),
                            banking.util.Validator.formatCurrency(t.getBalanceAfter())
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    @Override
    public void onActivated() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                return bankingService.getAccountsByCustomer(1);
            }
            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    String selected = (String) accountCombo.getSelectedItem();
                    accountCombo.removeAllItems();
                    for (Account acc : accounts) {
                        accountCombo.addItem(acc.getAccountNumber());
                    }
                    if (selected != null) {
                        accountCombo.setSelectedItem(selected);
                    } else if (!accounts.isEmpty()) {
                        accountCombo.setSelectedIndex(0);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }
}
