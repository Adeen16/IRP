package banking.ui.panels;

import banking.model.Account;
import banking.model.Customer;
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
 * Full CRUD panel for managing bank accounts.
 */
public class AccountPanel extends JPanel implements Refreshable {
    private final BankingService bankingService;
    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;

    public AccountPanel(BankingService bankingService) {
        this.bankingService = bankingService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Header ---
        JLabel title = new JLabel("Manage Accounts");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(UIStyle.BACKGROUND_COLOR);

        // Filter removed as status is no longer part of the schema

        // Action buttons
        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton btnAdd = new JButton("+ New Account");
        UIStyle.styleButton(btnAdd, UIStyle.SUCCESS_COLOR);
        btnAdd.addActionListener(e -> openCreateDialog());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh, UIStyle.SECONDARY_COLOR);
        btnRefresh.addActionListener(e -> loadAccounts());

        rightButtons.add(btnAdd);
        rightButtons.add(btnRefresh);


        toolbar.add(rightButtons, BorderLayout.EAST);

        JPanel headerArea = new JPanel(new BorderLayout(0, 15));
        headerArea.setBackground(UIStyle.BACKGROUND_COLOR);
        headerArea.add(title, BorderLayout.NORTH);
        headerArea.add(toolbar, BorderLayout.SOUTH);

        add(headerArea, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = {"Account #", "Customer", "Balance"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        accountTable = new JTable(tableModel);
        UIStyle.styleTable(accountTable);
        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        ModernUIComponents.RoundedPanel tableCard =
                new ModernUIComponents.RoundedPanel(15, Color.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // --- Bottom actions ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton btnClose = new JButton("Close Account");
        UIStyle.styleButton(btnClose, UIStyle.DANGER_COLOR);
        btnClose.addActionListener(e -> closeSelectedAccount());

        bottomBar.add(btnClose);
        add(bottomBar, BorderLayout.SOUTH);
    }

    @Override
    public void onActivated() {
        loadAccounts();
    }

    private void loadAccounts() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                return bankingService.getAllAccounts();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    UIStyle.showError(AccountPanel.this, "Failed to load accounts: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void populateTable(List<Account> accounts) {
        tableModel.setRowCount(0);
        for (Account a : accounts) {
            String customerName = "Customer #" + a.getCustomerId();
            tableModel.addRow(new Object[]{
                    a.getAccountNumber(),
                    customerName,
                    banking.util.Validator.formatCurrency(a.getBalance())
            });
        }
    }

    private void openCreateDialog() {
        AccountFormDialog dialog = new AccountFormDialog(
                SwingUtilities.getWindowAncestor(this), bankingService);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadAccounts();
        }
    }

    private void closeSelectedAccount() {
        int row = accountTable.getSelectedRow();
        if (row < 0) {
            UIStyle.showWarning(this, "Please select an account to close.");
            return;
        }
        String accountNumber = (String) tableModel.getValueAt(row, 0);
        if (!UIStyle.showConfirm(this, "Close account " + accountNumber + "?\nThis action cannot be undone.",
                "Confirm Close")) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bankingService.closeAccount(accountNumber);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIStyle.showSuccess(AccountPanel.this, "Account closed successfully.");
                    loadAccounts();
                } catch (Exception e) {
                    UIStyle.showError(AccountPanel.this, e.getMessage());
                }
            }
        }.execute();
    }
}
