package banking.service;

import banking.dao.LoanDAO;
import banking.dao.LoanDAOImpl;
import banking.model.Loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class LoanService {
    
    private final LoanDAO loanDAO;

    public LoanService() {
        this.loanDAO = new LoanDAOImpl();
    }

    /**
     * Submits a new loan application.
     * Evaluates basic business logic before passing to DAO.
     */
    public boolean applyForLoan(int customerId, BigDecimal amount, int termMonths) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || termMonths <= 0) {
            return false;
        }
        
        // Standard interest rate logic could go here based on term
        BigDecimal interestRate = new BigDecimal("5.50"); 
        
        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setAmount(amount);
        loan.setInterestRate(interestRate);
        loan.setTermMonths(termMonths);
        loan.setStatus("PENDING");
        
        return loanDAO.insertLoan(loan);
    }

    /**
     * Gets total projected repayment amount including simple interest.
     * Formula: Amount + (Amount * InterestRate / 100 * (TermMonths / 12))
     */
    public BigDecimal calculateTotalRepayment(BigDecimal amount, BigDecimal interestRate, int termMonths) {
        BigDecimal termYears = new BigDecimal(termMonths).divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        BigDecimal interest = amount.multiply(interestRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP).multiply(termYears);
        return amount.add(interest);
    }

    public List<Loan> getCustomerLoans(int customerId) {
        return loanDAO.getLoansByCustomer(customerId);
    }

    public List<Loan> getPendingLoans() {
        return loanDAO.getPendingLoans();
    }

    public boolean approveLoan(int loanId) {
        return loanDAO.updateLoanStatus(loanId, "APPROVED");
    }

    public boolean rejectLoan(int loanId) {
        return loanDAO.updateLoanStatus(loanId, "REJECTED");
    }
}
