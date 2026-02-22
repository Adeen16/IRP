package banking.exception;

public class InvalidTransactionException extends BankingException {
    private String transactionType;
    
    public InvalidTransactionException(String message) {
        super(message, "INVALID_TRANSACTION");
    }
    
    public InvalidTransactionException(String message, String transactionType) {
        super(message, "INVALID_TRANSACTION");
        this.transactionType = transactionType;
    }
    
    public String getTransactionType() { return transactionType; }
}
