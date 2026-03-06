package banking.model;

import java.math.BigDecimal;

public class Account {
    public enum AccountType {
        SAVINGS,
        CURRENT
    }

    private String accountNumber;
    private int customerId;
    private BigDecimal balance = BigDecimal.ZERO;
    private AccountType accountType = AccountType.SAVINGS;
    private String transactionPassword;

    public Account() {}

    public Account(String accountNumber, int customerId, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.balance = balance;
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public String getTransactionPassword() { return transactionPassword; }
    public void setTransactionPassword(String transactionPassword) { this.transactionPassword = transactionPassword; }
}
