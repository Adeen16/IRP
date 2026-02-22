package banking.exception;

public class AccountNotFoundException extends BankingException {
    private String accountNumber;
    
    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber, "ACCOUNT_NOT_FOUND");
        this.accountNumber = accountNumber;
    }
    
    public String getAccountNumber() { return accountNumber; }
}
