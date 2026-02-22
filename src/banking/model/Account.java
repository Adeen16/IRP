package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    public enum AccountType { SAVINGS, CHECKING }
    public enum AccountStatus { ACTIVE, CLOSED }

    private String accountNumber;
    private int customerId;
    private BigDecimal balance;
    private AccountType accountType = AccountType.SAVINGS;
    private AccountStatus status = AccountStatus.ACTIVE;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    private Customer customer;

    public Account() {
        this.balance = BigDecimal.ZERO;
    }

    public Account(String accountNumber, int customerId) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = BigDecimal.ZERO;
    }

    public Account(String accountNumber, int customerId, AccountType type) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = type;
        this.balance = BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType type) { this.accountType = type; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp ts) { this.createdAt = ts; }
    public boolean isActive() { return status == AccountStatus.ACTIVE; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer c) { this.customer = c; }
}
