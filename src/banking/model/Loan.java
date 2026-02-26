package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Loan {
    private int loanId;
    private int customerId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private int termMonths;
    private String status;
    private Timestamp createdAt;

    // Default constructor
    public Loan() {}

    // Constructor with all fields
    public Loan(int loanId, int customerId, BigDecimal amount, BigDecimal interestRate, int termMonths, String status, Timestamp createdAt) {
        this.loanId = loanId;
        this.customerId = customerId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public int getLoanId() { return loanId; }
    public int getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public int getTermMonths() { return termMonths; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setLoanId(int loanId) { this.loanId = loanId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
