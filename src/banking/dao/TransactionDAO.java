package banking.dao;

import banking.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionDAO {
    int getTransactionCountByDate(LocalDate date);
    BigDecimal getTotalTransactionAmountByDate(LocalDate date, Transaction.TransactionType type);
    List<Transaction> findByAccountNumber(String accountNumber);
    List<Transaction> findAll();
    void create(Transaction transaction);
    List<Transaction> findByAccountNumber(String accountNumber, int limit);
    List<Transaction> findByAccountNumberAndDateRange(String accountNumber, LocalDate start, LocalDate end);
    List<Transaction> findByDateRange(LocalDate start, LocalDate end);
}
