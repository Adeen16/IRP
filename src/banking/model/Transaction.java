package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    public enum TransactionType { DEPOSIT, WITHDRAW, TRANSFER }

    private int transactionId;
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    public Transaction() {}

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp ts) { this.createdAt = ts; }
    
    public boolean isCredit() {
        return type == TransactionType.DEPOSIT;
    }
}
