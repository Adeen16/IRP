package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT }
    
    private int transactionId;
    private String accountNumber;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceAccount;
    private String description;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    public Transaction() {}

    public Transaction(String accountNumber, TransactionType type, BigDecimal amount, BigDecimal balanceAfter) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public TransactionType getTransactionType() { return type; }
    public void setTransactionType(TransactionType type) { this.type = type; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getReferenceAccount() { return referenceAccount; }
    public void setReferenceAccount(String ref) { this.referenceAccount = ref; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public Timestamp getTransactionDate() { return createdAt; }
    public void setTransactionDate(Timestamp ts) { this.createdAt = ts; }
    
    public boolean isCredit() {
        return type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER_IN;
    }
}
