package banking.ui.panels;

import banking.model.Account;
import banking.model.User;
import banking.service.BankingService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;
import banking.ui.user.TransactionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern dashboard panel for standard users.
 * Shows account selection, balance, and quick action buttons.
 */
public class UserOverviewPanel extends JPanel implements Refreshable {
    private final User currentUser;
    private final BankingService bankingService;
    private final Frame parentFrame;
    
    private JComboBox<String> accountSelector;
    private JLabel lblBalance;
    private JLabel lblWelcome;

    public UserOverviewPanel(User user, BankingService service, Frame parent) {
        this.currentUser = user;
        this.bankingService = service;
        this.parentFrame = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(25, 25));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Account Overview Card ---
        ModernUIComponents.RoundedPanel accountCard = 
            new ModernUIComponents.RoundedPanel(20, Color.WHITE);
        accountCard.setLayout(new BorderLayout(20, 20));
        accountCard.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setBackground(Color.WHITE);
        
        lblWelcome = new JLabel("Welcome back, " + currentUser.getUsername());
        lblWelcome.setFont(UIStyle.HEADER_FONT);
        lblWelcome.setForeground(UIStyle.TEXT_COLOR);
        topRow.add(lblWelcome, BorderLayout.WEST);
        
        accountSelector = new JComboBox<>();
        UIStyle.styleComboBox(accountSelector);
        accountSelector.setPreferredSize(new Dimension(200, 35));
        accountSelector.addActionListener(e -> {
            if (accountSelector.getSelectedItem() != null) {
                updateBalanceDisplay((String) accountSelector.getSelectedItem());
            }
        });
        topRow.add(accountSelector, BorderLayout.EAST);
        
        accountCard.add(topRow, BorderLayout.NORTH);
        
        lblBalance = new JLabel("$0.00", SwingConstants.CENTER);
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 64));
        lblBalance.setForeground(UIStyle.ACCENT_COLOR);
        accountCard.add(lblBalance, BorderLayout.CENTER);
        
        JLabel lblStatus = new JLabel("Available Balance", SwingConstants.CENTER);
        lblStatus.setFont(UIStyle.LABEL_FONT);
        lblStatus.setForeground(UIStyle.TEXT_LIGHT);
        accountCard.add(lblStatus, BorderLayout.SOUTH);

        add(accountCard, BorderLayout.NORTH);

        // --- Quick Actions ---
        JPanel actionsWrapper = new JPanel(new BorderLayout(0, 25));
        actionsWrapper.setBackground(UIStyle.BACKGROUND_COLOR);

        JPanel actionsGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        actionsGrid.setBackground(UIStyle.BACKGROUND_COLOR);
        
        JButton btnDeposit = new JButton("\u2193  QUICK DEPOSIT");
        UIStyle.styleSuccessButton(btnDeposit);
        btnDeposit.addActionListener(e -> openTransaction("DEPOSIT"));
        actionsGrid.add(btnDeposit);
        
        JButton btnWithdraw = new JButton("\u2191  QUICK WITHDRAW");
        UIStyle.stylePrimaryButton(btnWithdraw);
        btnWithdraw.addActionListener(e -> openTransaction("WITHDRAW"));
        actionsGrid.add(btnWithdraw);

        JButton btnTransfer = new JButton("\u21C4  SEND MONEY (TRANSFER)");
        UIStyle.styleButton(btnTransfer, UIStyle.SECONDARY_COLOR);
        btnTransfer.setPreferredSize(new Dimension(0, 60));
        btnTransfer.addActionListener(e -> openTransaction("TRANSFER"));

        actionsWrapper.add(actionsGrid, BorderLayout.NORTH);
        actionsWrapper.add(btnTransfer, BorderLayout.CENTER);
        
        add(actionsWrapper, BorderLayout.CENTER);
    }

    private void openTransaction(String type) {
        String acc = (String) accountSelector.getSelectedItem();
        if (acc == null || acc.isEmpty()) {
            UIStyle.showWarning(this, "Please select an account first.");
            return;
        }
        TransactionDialog dialog = new TransactionDialog(parentFrame, acc, type);
        dialog.setVisible(true);
        onActivated(); // Refresh after transaction
    }

    private void updateBalanceDisplay(String accNum) {
        new SwingWorker<java.math.BigDecimal, Void>() {
            @Override
            protected java.math.BigDecimal doInBackground() throws Exception {
                return bankingService.getAccount(accNum).getBalance();
            }
            @Override
            protected void done() {
                try {
                    lblBalance.setText(banking.util.Validator.formatCurrency(get()));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    @Override
    public void onActivated() {
        new SwingWorker<List<Account>, Void>() {
            @Override
            protected List<Account> doInBackground() throws Exception {
                // Mock customerId
                return bankingService.getAccountsByCustomer(1);
            }
            @Override
            protected void done() {
                try {
                    List<Account> accounts = get();
                    String selected = (String) accountSelector.getSelectedItem();
                    accountSelector.removeAllItems();
                    for (Account acc : accounts) {
                        accountSelector.addItem(acc.getAccountNumber());
                    }
                    if (selected != null) {
                        accountSelector.setSelectedItem(selected);
                    } else if (!accounts.isEmpty()) {
                        accountSelector.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    accountSelector.addItem("Error loading accounts");
                }
            }
        }.execute();
    }
}
