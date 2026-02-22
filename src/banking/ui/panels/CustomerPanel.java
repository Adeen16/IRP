package banking.ui.panels;

import banking.model.Customer;
import banking.service.BankingService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;
import banking.util.Validator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Full CRUD panel for managing bank customers.
 * Provides search, add, edit, and delete with JTable display.
 */
public class CustomerPanel extends JPanel implements Refreshable {
    private final BankingService bankingService;
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public CustomerPanel(BankingService bankingService) {
        this.bankingService = bankingService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Header ---
        JLabel title = new JLabel("Manage Customers");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);

        // --- Toolbar ---
        JPanel toolbar = new JPanel(new BorderLayout(15, 0));
        toolbar.setBackground(UIStyle.BACKGROUND_COLOR);

        searchField = new JTextField(20);
        UIStyle.styleTextField(searchField);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name...");
        searchField.addActionListener(e -> loadCustomers());

        JButton btnSearch = new JButton("Search");
        UIStyle.styleButton(btnSearch, UIStyle.ACCENT_COLOR);
        btnSearch.addActionListener(e -> loadCustomers());

        JButton btnAdd = new JButton("+ Add Customer");
        UIStyle.styleButton(btnAdd, UIStyle.SUCCESS_COLOR);
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnRefresh = new JButton("Refresh");
        UIStyle.styleButton(btnRefresh, UIStyle.SECONDARY_COLOR);
        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            loadCustomers();
        });

        JPanel searchBox = new JPanel(new BorderLayout(5, 0));
        searchBox.setBackground(UIStyle.BACKGROUND_COLOR);
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(btnSearch, BorderLayout.EAST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setBackground(UIStyle.BACKGROUND_COLOR);
        rightButtons.add(btnAdd);
        rightButtons.add(btnRefresh);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(rightButtons, BorderLayout.EAST);

        JPanel headerArea = new JPanel(new BorderLayout(0, 15));
        headerArea.setBackground(UIStyle.BACKGROUND_COLOR);
        headerArea.add(title, BorderLayout.NORTH);
        headerArea.add(toolbar, BorderLayout.SOUTH);

        add(headerArea, BorderLayout.NORTH);

        // --- Table ---
        String[] columns = {"ID", "Name", "Phone", "Email", "Address", "Created"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        customerTable = new JTable(tableModel);
        UIStyle.styleTable(customerTable);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        customerTable.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        ModernUIComponents.RoundedPanel tableCard =
                new ModernUIComponents.RoundedPanel(15, Color.WHITE);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(new EmptyBorder(15, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        // --- Bottom action buttons ---
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomBar.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton btnEdit = new JButton("Edit Selected");
        UIStyle.styleButton(btnEdit, UIStyle.ACCENT_COLOR);
        btnEdit.addActionListener(e -> editSelected());

        JButton btnDelete = new JButton("Delete Selected");
        UIStyle.styleButton(btnDelete, UIStyle.DANGER_COLOR);
        btnDelete.addActionListener(e -> deleteSelected());

        bottomBar.add(btnEdit);
        bottomBar.add(btnDelete);

        add(bottomBar, BorderLayout.SOUTH);
    }

    @Override
    public void onActivated() {
        loadCustomers();
    }

    private void loadCustomers() {
        new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                String query = searchField.getText().trim();
                if (!query.isEmpty()) {
                    return bankingService.searchCustomersByName(query);
                }
                return bankingService.getAllCustomers();
            }

            @Override
            protected void done() {
                try {
                    populateTable(get());
                } catch (Exception e) {
                    UIStyle.showError(CustomerPanel.this, "Failed to load customers: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void populateTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getName(),
                    c.getPhone(),
                    c.getEmail(),
                    c.getAddress() != null ? c.getAddress() : "",
                    c.getCreatedAt() != null ? c.getCreatedAt().toString().substring(0, 10) : ""
            });
        }
    }

    private void openAddDialog() {
        CustomerFormDialog dialog = new CustomerFormDialog(
                SwingUtilities.getWindowAncestor(this), bankingService, null);
        dialog.setVisible(true);
        if (dialog.isSuccess()) {
            loadCustomers();
        }
    }

    private void editSelected() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIStyle.showWarning(this, "Please select a customer to edit.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(row, 0);

        new SwingWorker<Customer, Void>() {
            @Override
            protected Customer doInBackground() throws Exception {
                return bankingService.getCustomer(customerId);
            }

            @Override
            protected void done() {
                try {
                    Customer customer = get();
                    if (customer != null) {
                        CustomerFormDialog dialog = new CustomerFormDialog(
                                SwingUtilities.getWindowAncestor(CustomerPanel.this),
                                bankingService, customer);
                        dialog.setVisible(true);
                        if (dialog.isSuccess()) {
                            loadCustomers();
                        }
                    }
                } catch (Exception e) {
                    UIStyle.showError(CustomerPanel.this, e.getMessage());
                }
            }
        }.execute();
    }

    private void deleteSelected() {
        int row = customerTable.getSelectedRow();
        if (row < 0) {
            UIStyle.showWarning(this, "Please select a customer to delete.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        if (!UIStyle.showConfirm(this,
                "Delete customer \"" + name + "\"?\nThis will also delete all their accounts and transactions.",
                "Confirm Delete")) {
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bankingService.deleteCustomer(customerId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIStyle.showSuccess(CustomerPanel.this, "Customer deleted.");
                    loadCustomers();
                } catch (Exception e) {
                    UIStyle.showError(CustomerPanel.this, e.getMessage());
                }
            }
        }.execute();
    }
}
