package banking.dao;

import banking.model.Loan;
import banking.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {

    @Override
    public boolean insertLoan(Loan loan) {
        String query = "INSERT INTO loan_requests (customer_id, amount, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, loan.getCustomerId());
            stmt.setBigDecimal(2, loan.getAmount());
            stmt.setString(3, loan.getStatus() != null ? loan.getStatus() : "PENDING");
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Loan> getLoansByCustomer(int customerId) {
        List<Loan> loans = new ArrayList<>();
        String query = "SELECT loan_id, customer_id, amount, status, timestamp as created_at FROM loan_requests WHERE customer_id = ? ORDER BY timestamp DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
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
        String query = "SELECT loan_id, customer_id, amount, status, timestamp as created_at FROM loan_requests WHERE status = 'PENDING' ORDER BY timestamp ASC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                loans.add(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public boolean updateLoanStatus(int loanId, String status) {
        String query = "UPDATE loan_requests SET status = ? WHERE loan_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, loanId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Loan loan = new Loan();
        loan.setLoanId(rs.getInt("loan_id"));
        loan.setCustomerId(rs.getInt("customer_id"));
        loan.setAmount(rs.getBigDecimal("amount"));
        loan.setInterestRate(new java.math.BigDecimal("0.0"));
        loan.setTermMonths(12);
        loan.setStatus(rs.getString("status"));
        loan.setCreatedAt(rs.getTimestamp("created_at"));
        return loan;
    }
}
