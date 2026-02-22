package banking.exception;

public class InsufficientBalanceException extends BankingException {
    private double currentBalance;
    private double requestedAmount;
    
    public InsufficientBalanceException(double currentBalance, double requestedAmount) {
        super(String.format("Insufficient balance. Current: %.2f, Requested: %.2f", 
              currentBalance, requestedAmount), "INSUFFICIENT_BALANCE");
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public double getCurrentBalance() { return currentBalance; }
    public double getRequestedAmount() { return requestedAmount; }
}
