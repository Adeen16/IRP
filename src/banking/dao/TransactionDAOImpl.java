package banking.dao;

import banking.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    @Override
    public int getTransactionCountByDate(LocalDate date) {
        return 0;
    }

    @Override
    public BigDecimal getTotalTransactionAmountByDate(LocalDate date, Transaction.TransactionType type) {
        return BigDecimal.ZERO;
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) {
        return new ArrayList<>();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>();
    }

    @Override
    public void create(Transaction transaction) {
        // Dummy implementation
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber, int limit) {
        return new ArrayList<>();
    }

    @Override
    public List<Transaction> findByAccountNumberAndDateRange(String accountNumber, LocalDate start, LocalDate end) {
        return new ArrayList<>();
    }

    @Override
    public List<Transaction> findByDateRange(LocalDate start, LocalDate end) {
        return new ArrayList<>();
    }
}
