package banking.ui.panels;

import banking.model.User;
import banking.service.AuthService;
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
 * System users management panel (Admin only).
 * Create, view, and delete system users with role assignment.
 */
public class UserPanel extends JPanel implements Refreshable {
    private final AuthService authService;
    private final BankingService bankingService;
    private final User currentUser;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserPanel(AuthService authService, User currentUser) {
        this.authService = authService;
        this.bankingService = new BankingService();
        this.currentUser = currentUser;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Header ---
        JLabel title = new JLabel("System Users");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton btnAdd = new JButton("+ Add User");
        UIStyle.styleButton(btnAdd, UIStyle.SUCCESS_COLOR);
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh, UIStyle.SECONDARY_COLOR);
        btnRefresh.addActionListener(e -> loadUsers());

        toolbar.add(btnAdd);
        toolbar.add(btnRefresh);

        JPanel headerArea = new JPanel(new BorderLayout(0, 15));
        headerArea.setBackground(UIStyle.BACKGROUND_COLOR);
        headerArea.add(title, BorderLayout.NORTH);
        headerArea.add(toolbar, BorderLayout.SOUTH);

        add(headerArea, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = {"ID", "Username", "Role", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        UIStyle.styleTable(userTable);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(userTable);
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

        JButton btnDelete = new JButton("Delete Selected");
        UIStyle.styleButton(btnDelete, UIStyle.DANGER_COLOR);
        btnDelete.addActionListener(e -> deleteSelected());

        bottomBar.add(btnDelete);
        add(bottomBar, BorderLayout.SOUTH);
    }

    @Override
    public void onActivated() {
        loadUsers();
    }

    private void loadUsers() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return authService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    UIStyle.showError(UserPanel.this, "Failed to load users: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void populateTable(List<User> users) {
        tableModel.setRowCount(0);
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getRole().name(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toString().substring(0, 10) : ""
            });
        }
    }

    private void openAddDialog() {
        UserFormDialog dialog = new UserFormDialog(
                SwingUtilities.getWindowAncestor(this), authService, bankingService);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadUsers();
        }
    }

    private void deleteSelected() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            UIStyle.showWarning(this, "Please select a user to delete.");
            return;
        }
        int userId = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);

        // Prevent self-deletion
        if (currentUser != null && currentUser.getUserId() == userId) {
            UIStyle.showError(this, "You cannot delete your own account.");
            return;
        }

        if (!UIStyle.showConfirm(this, "Delete user \"" + username + "\"?", "Confirm Delete")) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                authService.deleteUser(userId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIStyle.showSuccess(UserPanel.this, "User deleted.");
                    loadUsers();
                } catch (Exception e) {
                    UIStyle.showError(UserPanel.this, e.getMessage());
                }
            }
        }.execute();
    }
}
