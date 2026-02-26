package banking.ui.panels;

import banking.model.Account;
import banking.model.Transaction;
import banking.service.BankingService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Transaction reports panel with date and account filtering.
 */
public class TransactionPanel extends JPanel implements Refreshable {
    private final BankingService bankingService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> accountCombo;
    private JTextField txtFrom;
    private JTextField txtTo;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TransactionPanel(BankingService bankingService) {
        this.bankingService = bankingService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Header ---
        JLabel title = new JLabel("Transaction Reports");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);

        // --- Filter toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setBackground(UIStyle.BACKGROUND_COLOR);

        toolbar.add(new JLabel("Account:"));
        accountCombo = new JComboBox<>();
        accountCombo.addItem("ALL");
        UIStyle.styleComboBox(accountCombo);
        accountCombo.setPreferredSize(new Dimension(180, 35));
        toolbar.add(accountCombo);

        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(new JLabel("From:"));
        txtFrom = new JTextField(10);
        UIStyle.styleTextField(txtFrom);
        txtFrom.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");
        toolbar.add(txtFrom);

        toolbar.add(new JLabel("To:"));
        txtTo = new JTextField(10);
        UIStyle.styleTextField(txtTo);
        txtTo.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd");
        toolbar.add(txtTo);

        JButton btnSearch = new JButton("Search");
        UIStyle.styleButton(btnSearch, UIStyle.ACCENT_COLOR);
        btnSearch.addActionListener(e -> searchTransactions());
        toolbar.add(btnSearch);

        JButton btnLast5 = new JButton("Last 5");
        UIStyle.styleButton(btnLast5, UIStyle.WARNING_COLOR);
        btnLast5.addActionListener(e -> loadLast5());
        toolbar.add(btnLast5);

        JButton btnAll = new JButton("Show All");
        UIStyle.styleButton(btnAll, UIStyle.SECONDARY_COLOR);
        btnAll.addActionListener(e -> {
            txtFrom.setText("");
            txtTo.setText("");
            accountCombo.setSelectedItem("ALL");
            searchTransactions();
        });
        toolbar.add(btnAll);

        JPanel headerArea = new JPanel(new BorderLayout(0, 15));
        headerArea.setBackground(UIStyle.BACKGROUND_COLOR);
        headerArea.add(title, BorderLayout.NORTH);
        headerArea.add(toolbar, BorderLayout.SOUTH);

        add(headerArea, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = {"ID", "Account #", "Type", "Amount", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        UIStyle.styleTable(transactionTable);
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        ModernUIComponents.RoundedPanel tableCard =
                new ModernUIComponents.RoundedPanel(15, Color.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public void onActivated() {
        loadAccountList();
        searchTransactions();
    }

    private void loadAccountList() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                return bankingService.getAllAccounts();
            }

            @Override
            protected void done() {
                try {
                    String selected = (String) accountCombo.getSelectedItem();
                    accountCombo.removeAllItems();
                    accountCombo.addItem("ALL");
                    for (Account a : get()) {
                        accountCombo.addItem(a.getAccountNumber());
                    }
                    if (selected != null) {
                        accountCombo.setSelectedItem(selected);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    private void searchTransactions() {
        String account = (String) accountCombo.getSelectedItem();
        String fromStr = txtFrom.getText().trim();
        String toStr = txtTo.getText().trim();

        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                boolean hasAccount = account != null && !"ALL".equals(account);
                boolean hasDateRange = !fromStr.isEmpty() && !toStr.isEmpty();

                if (hasAccount && hasDateRange) {
                    LocalDate from = LocalDate.parse(fromStr, DATE_FMT);
                    LocalDate to = LocalDate.parse(toStr, DATE_FMT);
                    return bankingService.getTransactionsByDateRange(account, from, to);
                } else if (hasAccount) {
                    return bankingService.getTransactionHistory(account);
                } else if (hasDateRange) {
                    LocalDate from = LocalDate.parse(fromStr, DATE_FMT);
                    LocalDate to = LocalDate.parse(toStr, DATE_FMT);
                    // Use all accounts — fetch all and filter by date range
                    return bankingService.getAllTransactionsByDateRange(from, to);
                } else {
                    return bankingService.getAllTransactions();
                }
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    String msg = e.getMessage();
                    if (e.getCause() instanceof DateTimeParseException) {
                        msg = "Invalid date format. Use yyyy-MM-dd.";
                    }
                    UIStyle.showError(TransactionPanel.this, msg);
                }
            }
        }.execute();
    }

    private void loadLast5() {
        String account = (String) accountCombo.getSelectedItem();
        if (account == null || "ALL".equals(account)) {
            UIStyle.showWarning(this, "Please select a specific account for Last 5.");
            return;
        }

        new SwingWorker<List<Transaction>, Void>() {
            @Override
            protected List<Transaction> doInBackground() throws Exception {
                return bankingService.getMiniStatement(account);
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    UIStyle.showError(TransactionPanel.this, e.getMessage());
                }
            }
        }.execute();
    }

    private void populateTable(List<Transaction> transactions) {
        tableModel.setRowCount(0);
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getAccountNumber(),
                    t.getType().name(),
                    banking.util.Validator.formatCurrency(t.getAmount()),
                    t.getCreatedAt() != null ? t.getCreatedAt().toString().substring(0, 10) : ""
            });
        }
    }
}
