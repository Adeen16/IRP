package banking.dao;

import banking.model.Account;
import banking.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {

    @Override
    public Account findByAccountNumber(String accNumber) {
        String sql = "SELECT * FROM account WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setCustomerId(rs.getInt("customer_id"));
                    acc.setBalance(rs.getBigDecimal("balance"));
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean create(Account account) {
        String sql = "INSERT INTO account (account_number, customer_id, balance) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, account.getAccountNumber());
            stmt.setInt(2, account.getCustomerId());
            stmt.setBigDecimal(3, account.getBalance());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateBalance(String accNumber, BigDecimal balance) {
        String sql = "UPDATE account SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBigDecimal(1, balance);
            stmt.setString(2, accNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Account> findByCustomerId(int customerId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Account acc = new Account();
                    acc.setAccountNumber(rs.getString("account_number"));
                    acc.setCustomerId(rs.getInt("customer_id"));
                    acc.setBalance(rs.getBigDecimal("balance"));
                    list.add(acc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Account acc = new Account();
                acc.setAccountNumber(rs.getString("account_number"));
                acc.setCustomerId(rs.getInt("customer_id"));
                acc.setBalance(rs.getBigDecimal("balance"));
                list.add(acc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public int getTotalAccountCount() {
        String sql = "SELECT COUNT(*) FROM account";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public BigDecimal getTotalBankBalance() {
        String sql = "SELECT SUM(balance) FROM account";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                BigDecimal sum = rs.getBigDecimal(1);
                return sum != null ? sum : BigDecimal.ZERO;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
