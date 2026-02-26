package banking.dao;

import banking.model.Loan;
import java.util.List;

public interface LoanDAO {
    /**
     * Inserts a new loan application into the database.
     * @param loan The loan object containing the details
     * @return true if successful, false otherwise
     */
    boolean insertLoan(Loan loan);

    /**
     * Retrieves all loans associated with a specific customer.
     * @param customerId The ID of the customer
     * @return A list of Loan objects
     */
    List<Loan> getLoansByCustomer(int customerId);

    /**
     * Retrieves all loans that are currently in 'PENDING' status for admin review.
     * @return A list of PENDING Loan objects
     */
    List<Loan> getPendingLoans();

    /**
     * Updates the status of an existing loan (e.g., from PENDING to APPROVED).
     * @param loanId The ID of the loan to update
     * @param status The new status value
     * @return true if successful, false otherwise
     */
    boolean updateLoanStatus(int loanId, String status);
}
