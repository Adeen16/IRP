package banking.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Loan {
    private int loanId;
    private int customerId;
    private String accountNumber;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private int loanDuration;
    private BigDecimal emi;
    private String loanType;
    private String status;
    private Timestamp createdAt;

    // Default constructor
    public Loan() {}

    // Constructor with all fields
    public Loan(int loanId, int customerId, BigDecimal loanAmount, BigDecimal interestRate, int loanDuration,
                BigDecimal emi, String loanType, String status, Timestamp createdAt) {
        this.loanId = loanId;
        this.customerId = customerId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.loanDuration = loanDuration;
        this.emi = emi;
        this.loanType = loanType;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters
    public int getLoanId() { return loanId; }
    public int getCustomerId() { return customerId; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public int getLoanDuration() { return loanDuration; }
    public BigDecimal getEmi() { return emi; }
    public String getLoanType() { return loanType; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }

    public BigDecimal getAmount() { return loanAmount; }
    public int getTermMonths() { return loanDuration; }

    // Setters
    public void setLoanId(int loanId) { this.loanId = loanId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public void setLoanDuration(int loanDuration) { this.loanDuration = loanDuration; }
    public void setEmi(BigDecimal emi) { this.emi = emi; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public void setAmount(BigDecimal amount) { this.loanAmount = amount; }
    public void setTermMonths(int termMonths) { this.loanDuration = termMonths; }
}
