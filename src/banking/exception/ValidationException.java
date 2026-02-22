package banking.exception;

public class ValidationException extends BankingException {
    private String fieldName;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
    }
    
    public ValidationException(String fieldName, String message) {
        super(fieldName + ": " + message, "VALIDATION_ERROR");
        this.fieldName = fieldName;
    }
    
    public String getFieldName() { return fieldName; }
}
