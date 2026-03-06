package banking.service;

import banking.dao.LoanDAO;
import banking.dao.LoanDAOImpl;
import banking.exception.ValidationException;
import banking.model.Loan;
import banking.model.LoanDecision;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class LoanService {
    private static final BigDecimal EMI_INCOME_LIMIT = new BigDecimal("0.40");
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

    private final LoanDAO loanDAO;

    public LoanService() {
        this.loanDAO = new LoanDAOImpl();
    }

    public LoanDecision evaluateLoan(int creditScore, BigDecimal monthlyIncome, BigDecimal loanAmount,
                                     String loanType, int loanDuration) throws ValidationException {
        validateLoanInputs(monthlyIncome, loanAmount, loanType, loanDuration);

        LoanDecision decision = new LoanDecision();
        decision.setLoanDuration(loanDuration);

        BigDecimal interestRate = determineInterestRate(creditScore);
        if (interestRate == null) {
            decision.setStatus("REJECTED");
            decision.setInterestRate(BigDecimal.ZERO);
            decision.setEmi(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            decision.setReason("Credit score below 650 does not meet minimum approval policy.");
            return decision;
        }

        BigDecimal emi = calculateEmi(loanAmount, interestRate, loanDuration);
        BigDecimal maxEmi = monthlyIncome.multiply(EMI_INCOME_LIMIT).setScale(2, RoundingMode.HALF_UP);

        decision.setInterestRate(interestRate);
        decision.setEmi(emi);

        if (emi.compareTo(maxEmi) <= 0) {
            decision.setStatus("APPROVED");
            decision.setReason("EMI is within 40% of monthly income and credit score meets policy.");
        } else {
            decision.setStatus("REJECTED");
            decision.setReason("EMI exceeds 40% of monthly income. Maximum affordable EMI is $" + maxEmi + ".");
        }

        return decision;
    }

    public LoanDecision submitLoanRequest(int customerId, int creditScore, BigDecimal monthlyIncome,
                                          BigDecimal loanAmount, String loanType, int loanDuration) throws Exception {
        LoanDecision decision = evaluateLoan(creditScore, monthlyIncome, loanAmount, loanType, loanDuration);

        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setLoanAmount(loanAmount.setScale(2, RoundingMode.HALF_UP));
        loan.setInterestRate(decision.getInterestRate().setScale(2, RoundingMode.HALF_UP));
        loan.setLoanDuration(loanDuration);
        loan.setEmi(decision.getEmi().setScale(2, RoundingMode.HALF_UP));
        loan.setLoanType(loanType);
        loan.setStatus(decision.getStatus());

        if (!loanDAO.insertLoan(loan)) {
            throw new Exception("Failed to store loan request.");
        }

        return decision;
    }

    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (principal == null || annualRate == null || termMonths <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 12, RoundingMode.HALF_UP);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate, MC);
        BigDecimal factor = onePlusR.pow(termMonths, MC);
        BigDecimal numerator = principal.multiply(monthlyRate, MC).multiply(factor, MC);
        BigDecimal denominator = factor.subtract(BigDecimal.ONE, MC);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
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

    private void validateLoanInputs(BigDecimal monthlyIncome, BigDecimal loanAmount, String loanType,
                                    int loanDuration) throws ValidationException {
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Monthly income must be greater than zero.");
        }
        if (loanAmount == null || loanAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Loan amount must be greater than zero.");
        }
        if (loanType == null || loanType.trim().isEmpty()) {
            throw new ValidationException("Loan type is required.");
        }
        if (loanDuration <= 0) {
            throw new ValidationException("Loan duration must be greater than zero.");
        }
    }

    private BigDecimal determineInterestRate(int creditScore) {
        if (creditScore >= 750) {
            return new BigDecimal("8.50");
        }
        if (creditScore >= 700) {
            return new BigDecimal("10.00");
        }
        if (creditScore >= 650) {
            return new BigDecimal("12.00");
        }
        return null;
    }
}
