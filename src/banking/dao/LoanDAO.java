package banking.dao;

import banking.model.Loan;
import java.util.List;

public interface LoanDAO {
    boolean insertLoan(Loan loan);
    List<Loan> getLoansByCustomer(int customerId);
    List<Loan> getPendingLoans();
    boolean updateLoanStatus(int loanId, String status);
    Loan findById(int loanId);
    boolean approveAndCreditLoan(int loanId);
}
