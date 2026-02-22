package banking.exception;

public class BankingException extends Exception {
    private String errorCode;
    
    public BankingException(String message) {
        super(message);
    }
    
    public BankingException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public BankingException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() { return errorCode; }
}
