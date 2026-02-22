package banking.exception;

public class AuthenticationException extends BankingException {
    public AuthenticationException(String message) {
        super(message, "AUTH_FAILED");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTH_FAILED", cause);
    }
}
