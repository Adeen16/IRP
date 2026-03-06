package banking.dao;

import banking.model.Loan;
import banking.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {
    @Override
    public boolean insertLoan(Loan loan) {
        String sql = "INSERT INTO loan (customer_id, loan_amount, interest_rate, loan_duration, emi, loan_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, loan.getCustomerId());
            stmt.setBigDecimal(2, loan.getLoanAmount());
            stmt.setBigDecimal(3, loan.getInterestRate());
            stmt.setInt(4, loan.getLoanDuration());
            stmt.setBigDecimal(5, loan.getEmi());
            stmt.setString(6, loan.getLoanType());
            stmt.setString(7, loan.getStatus());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Loan> getLoansByCustomer(int customerId) {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT loan_id, customer_id, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
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
        String sql = "SELECT loan_id, customer_id, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
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
        String sql = "SELECT loan_id, customer_id, loan_amount, interest_rate, loan_duration, emi, loan_type, status, created_at " +
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
        loan.setLoanAmount(rs.getBigDecimal("loan_amount"));
        loan.setInterestRate(rs.getBigDecimal("interest_rate"));
        loan.setLoanDuration(rs.getInt("loan_duration"));
        loan.setEmi(rs.getBigDecimal("emi"));
        loan.setLoanType(rs.getString("loan_type"));
        loan.setStatus(rs.getString("status"));
        loan.setCreatedAt(rs.getTimestamp("created_at"));
        return loan;
    }
}
