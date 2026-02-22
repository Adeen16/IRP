package banking.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = 
        Pattern.compile("^[A-Z]{2}[0-9]{10}$");
    
    // Monetary constants using BigDecimal
    private static final BigDecimal MIN_BALANCE = new BigDecimal("100.00");
    private static final BigDecimal MAX_WITHDRAWAL = new BigDecimal("50000.00");
    private static final BigDecimal MAX_TRANSFER = new BigDecimal("100000.00");
    
    public static boolean isValidEmail(String email) {
        return email != null && !email.isEmpty() && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        return PHONE_PATTERN.matcher(phone.replaceAll("[\\s-]", "")).matches();
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() >= 2 && name.length() <= 100;
    }
    
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
    }
    
    public static boolean isWithdrawalAllowed(BigDecimal balance, BigDecimal amount) {
        if (!isValidAmount(amount)) return false;
        if (amount.compareTo(MAX_WITHDRAWAL) > 0) return false;
        BigDecimal remainingBalance = balance.subtract(amount);
        return remainingBalance.compareTo(MIN_BALANCE) >= 0;
    }
    
    public static boolean isTransferAllowed(BigDecimal balance, BigDecimal amount) {
        if (!isValidAmount(amount)) return false;
        if (amount.compareTo(MAX_TRANSFER) > 0) return false;
        BigDecimal remainingBalance = balance.subtract(amount);
        return remainingBalance.compareTo(MIN_BALANCE) >= 0;
    }
    
    public static boolean isDepositAllowed(BigDecimal amount) {
        return isValidAmount(amount) && amount.compareTo(MAX_TRANSFER) <= 0;
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.length() <= 50 &&
               username.matches("^[a-zA-Z0-9_]+$");
    }
    
    public static boolean isValidDate(String dateStr, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public static boolean isDateInRange(LocalDate date, LocalDate start, LocalDate end) {
        return (date.isEqual(start) || date.isAfter(start)) &&
               (date.isEqual(end) || date.isBefore(end));
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                   .replace("'", "''")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
    
    public static BigDecimal roundToTwoDecimals(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return value.setScale(2, RoundingMode.HALF_UP);
    }
    
    public static String formatCurrency(BigDecimal amount) {
        return String.format("$%,.2f", roundToTwoDecimals(amount));
    }
    
    public static ValidationResult validateCustomer(String name, String phone, String email) {
        if (!isValidName(name)) {
            return new ValidationResult(false, "Invalid name. Name must be 2-100 characters.");
        }
        if (!isValidPhone(phone)) {
            return new ValidationResult(false, "Invalid phone number. Must be 10-15 digits.");
        }
        if (!isValidEmail(email)) {
            return new ValidationResult(false, "Invalid email format.");
        }
        return new ValidationResult(true, "Valid customer data");
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}
