package banking.dao;

import banking.model.Loan;
import banking.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {
    @Override
    public boolean insertLoan(Loan loan) {
        String sql = "INSERT INTO loan (customer_id, account_number, loan_amount, interest_rate, loan_duration, emi, loan_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loan.getCustomerId());
            stmt.setString(2, loan.getAccountNumber());
            stmt.setBigDecimal(3, loan.getLoanAmount());
            stmt.setBigDecimal(4, loan.getInterestRate());
            stmt.setInt(5, loan.getLoanDuration());
            stmt.setBigDecimal(6, loan.getEmi());
            stmt.setString(7, loan.getLoanType());
            stmt.setString(8, loan.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Loan> getLoansByCustomer(int customerId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT loan_id, customer_id, account_number, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
            "FROM loan WHERE customer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public List<Loan> getPendingLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT loan_id, customer_id, account_number, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
            "FROM loan WHERE status = ? ORDER BY created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "PENDING");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    loans.add(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public boolean updateLoanStatus(int loanId, String status) {
        String sql = "UPDATE loan SET status = ? WHERE loan_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, loanId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Loan findById(int loanId) {
        String sql = "SELECT loan_id, customer_id, account_number, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
            "FROM loan WHERE loan_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loanId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLoan(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setCustomerId(rs.getInt("customer_id"));
        loan.setAccountNumber(rs.getString("account_number"));
        loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
        loan.setInterestRate(rs.getBigDecimal("interest_rate"));
        loan.setLoanDuration(rs.getInt("loan_duration"));
        loan.setEmi(rs.getBigDecimal("emi"));
        loan.setLoanType(rs.getString("loan_type"));
        loan.setStatus(rs.getString("status"));
        loan.setCreatedAt(rs.getTimestamp("created_at"));
        return loan;
    }

    @Override
    public boolean approveAndCreditLoan(int loanId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            System.out.println("[LOAN APPROVAL] Starting approval for loan_id: " + loanId);

            // 1. Get loan details
            Loan loan = null;
            String loanSql = "SELECT * FROM loan WHERE loan_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(loanSql)) {
                stmt.setInt(1, loanId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        loan = mapResultSetToLoan(rs);
                    }
                }
            }
            if (loan == null) {
                System.err.println("[LOAN APPROVAL] ERROR: Loan not found for loan_id: " + loanId);
                throw new SQLException("Loan not found");
            }

            System.out.println("[LOAN APPROVAL] Loan found - customer_id: " + loan.getCustomerId() + 
                             ", amount: " + loan.getLoanAmount() + ", status: " + loan.getStatus());

            // 2. Validate loan status
            if ("APPROVED".equals(loan.getStatus())) {
                System.err.println("[LOAN APPROVAL] ERROR: Loan is already APPROVED");
                throw new SQLException("Loan is already approved");
            }
            if ("REJECTED".equals(loan.getStatus())) {
                System.err.println("[LOAN APPROVAL] ERROR: Loan is already REJECTED");
                throw new SQLException("Cannot approve a rejected loan");
            }

            // 3. Get account number (prefer stored account_number, fallback to finding by customer)
            String accountNumber = loan.getAccountNumber();
            if (accountNumber == null || accountNumber.isEmpty()) {
                String accountSql = "SELECT account_number FROM account WHERE customer_id = ? LIMIT 1";
                try (PreparedStatement stmt = conn.prepareStatement(accountSql)) {
                    stmt.setInt(1, loan.getCustomerId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            accountNumber = rs.getString("account_number");
                        }
                    }
                }
            }

            if (accountNumber == null || accountNumber.isEmpty()) {
                System.err.println("[LOAN APPROVAL] ERROR: No account found for customer_id: " + loan.getCustomerId());
                throw new SQLException("No account found for customer");
            }

            System.out.println("[LOAN APPROVAL] Target account: " + accountNumber);

            // 4. Get current balance before credit
            BigDecimal balanceBefore = BigDecimal.ZERO;
            String balanceSql = "SELECT balance FROM account WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(balanceSql)) {
                stmt.setString(1, accountNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        balanceBefore = rs.getBigDecimal("balance");
                    }
                }
            }
            System.out.println("[LOAN APPROVAL] Balance before credit: " + balanceBefore);

            // 5. Update loan status to APPROVED
            String updateLoanSql = "UPDATE loan SET status = 'APPROVED' WHERE loan_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateLoanSql)) {
                stmt.setInt(1, loanId);
                int rows = stmt.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to update loan status");
                }
            }
            System.out.println("[LOAN APPROVAL] Loan status updated to APPROVED");

            // 6. Credit the account balance
            String creditSql = "UPDATE account SET balance = balance + ? WHERE account_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(creditSql)) {
                stmt.setBigDecimal(1, loan.getLoanAmount());
                stmt.setString(2, accountNumber);
                int rows = stmt.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to credit account");
                }
            }
            System.out.println("[LOAN APPROVAL] Account credited with: " + loan.getLoanAmount());

            // 7. Verify updated balance
            BigDecimal balanceAfter = BigDecimal.ZERO;
            try (PreparedStatement stmt = conn.prepareStatement(balanceSql)) {
                stmt.setString(1, accountNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        balanceAfter = rs.getBigDecimal("balance");
                    }
                }
            }
            System.out.println("[LOAN APPROVAL] Balance after credit: " + balanceAfter);

            // 8. Insert transaction record
            String transSql = "INSERT INTO \"transaction\" (account_number, type, amount, performed_by) VALUES (?, 'LOAN_CREDIT', ?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(transSql)) {
                stmt.setString(1, accountNumber);
                stmt.setBigDecimal(2, loan.getLoanAmount());
                stmt.executeUpdate();
            }
            System.out.println("[LOAN APPROVAL] Transaction record inserted");

            conn.commit();
            System.out.println("[LOAN APPROVAL] Transaction committed successfully!");
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("[LOAN APPROVAL] Transaction rolled back due to error");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }
}
