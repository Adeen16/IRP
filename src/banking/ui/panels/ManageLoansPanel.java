package banking.ui.panels;

import banking.model.Loan;
import banking.service.LoanService;
import banking.ui.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageLoansPanel extends JPanel {
    private final LoanService loanService;
    private JTable loanTable;
    private DefaultTableModel tableModel;

    public ManageLoansPanel() {
        this.loanService = new LoanService();
        setupUI();
        loadPendingLoans();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Manage Pending Loans");
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);
        add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Loan ID", "Customer ID", "Amount", "Interest %", "Term (Months)", "Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        loanTable = new JTable(tableModel);
        UIStyle.styleTable(loanTable);
        JScrollPane scrollPane = new JScrollPane(loanTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        JButton approveBtn = new JButton("Approve Selected");
        UIStyle.styleSuccessButton(approveBtn);
        
        JButton rejectBtn = new JButton("Reject Selected");
        UIStyle.styleDangerButton(rejectBtn);
        
        JButton refreshBtn = new JButton("Refresh");
        UIStyle.stylePrimaryButton(refreshBtn);

        approveBtn.addActionListener(e -> processSelectedLoan(true));
        rejectBtn.addActionListener(e -> processSelectedLoan(false));
        refreshBtn.addActionListener(e -> loadPendingLoans());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(approveBtn);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPendingLoans() {
        tableModel.setRowCount(0);
        List<Loan> pendingLoans = loanService.getPendingLoans();

        for (Loan loan : pendingLoans) {
            Object[] row = {
                loan.getLoanId(),
                loan.getCustomerId(),
                "$" + String.format("%.2f", loan.getAmount()),
                loan.getInterestRate() + "%",
                loan.getTermMonths(),
                loan.getCreatedAt()
            };
            tableModel.addRow(row);
        }
    }

    private void processSelectedLoan(boolean approve) {
        int selectedRow = loanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a loan first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loanId = (int) loanTable.getValueAt(selectedRow, 0);
        
        boolean success;
        if (approve) {
            success = loanService.approveLoan(loanId);
        } else {
            success = loanService.rejectLoan(loanId);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Loan status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadPendingLoans(); // Refresh table
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update loan status.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
