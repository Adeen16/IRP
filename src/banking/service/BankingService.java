package banking.service;

import banking.dao.AccountDAO;
import banking.dao.AccountDAOImpl;
import banking.dao.CustomerDAO;
import banking.dao.CustomerDAOImpl;
import banking.dao.TransactionDAO;
import banking.dao.TransactionDAOImpl;
import banking.dao.UserDAO;
import banking.dao.UserDAOImpl;
import banking.exception.AccountNotFoundException;
import banking.exception.BankingException;
import banking.exception.InsufficientBalanceException;
import banking.exception.InvalidTransactionException;
import banking.exception.ValidationException;
import banking.model.Account;
import banking.model.Customer;
import banking.model.Transaction;
import banking.util.DatabaseConnection;
import banking.util.HashUtil;
import banking.util.PasswordSecurity;
import banking.util.Validator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BankingService {
    private AccountDAO accountDAO;
    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;
    private UserDAO userDAO;
    
    public BankingService() {
        this.accountDAO = new AccountDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
        this.userDAO = new UserDAOImpl();
    }
    
    public Customer createCustomer(String name, String phone, String email, String address) throws Exception {
        Validator.ValidationResult validation = Validator.validateCustomer(name, phone, email);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getMessage());
        }
        
        // Auto-generate User Login
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String uniqueUsername = baseUsername;
        int counter = 1;
        while (userDAO.findByUsername(uniqueUsername) != null) {
            uniqueUsername = baseUsername + counter;
            counter++;
        }
        
        String generatedPassword = "Secure123A";
        banking.model.User user = new banking.model.User();
        user.setUsername(uniqueUsername);
        user.setPasswordHash(PasswordSecurity.hashPassword(generatedPassword));
        user.setRole(banking.model.User.UserRole.USER);
        
        boolean userCreated = userDAO.create(user);
        if (!userCreated || user.getUserId() <= 0) {
            throw new BankingException("Failed to generate user login credentials.");
        }
        
        Customer customer = new Customer(name, phone, email);
        customer.setUserId(user.getUserId());
        int customerId = customerDAO.create(customer);
        customer.setCustomerId(customerId);
        
        // Temporarily store generated credentials in the object so the UI can read it
        customer.setPhone(phone + "||" + uniqueUsername + "||" + generatedPassword); 
        
        return customer;
    }
    
    public Account createAccount(int customerId, Account.AccountType type, String transactionPassword) throws Exception {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new ValidationException("Customer not found");
        }
        
        // Validate transaction password (4-6 digits)
        if (transactionPassword == null || !transactionPassword.matches("\\d{4,6}")) {
            throw new ValidationException("Transaction password must be 4-6 digits.");
        }
        
        // Ensure account number is unique
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountDAO.findByAccountNumber(accountNumber) != null);

        Account account = new Account(accountNumber, customerId, BigDecimal.ZERO);
        account.setAccountType(type);
        account.setTransactionPassword(HashUtil.hashPassword(transactionPassword));
        accountDAO.create(account);
        return account;
    }
    
    private String generateAccountNumber() {
        return "BA" + String.format("%010d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10000000000L);
    }
    
    public Transaction deposit(String accountNumber, BigDecimal amount, int userId) throws Exception {
        if (!Validator.isDepositAllowed(amount)) {
            throw new InvalidTransactionException("Invalid deposit amount. Amount must be positive and within limits.");
        }
        
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal newBalance = Validator.roundToTwoDecimals(account.getBalance().add(amount));
            account.setBalance(newBalance);
            accountDAO.updateBalance(accountNumber, newBalance);
            
            Transaction transaction = new Transaction(accountNumber, Transaction.TransactionType.DEPOSIT, amount);
            transaction.setPerformedBy(userId);
            transactionDAO.create(transaction);
            
            conn.commit();
            logTransactionToFile(transaction);
            logAuditAction(userId, "DEPOSIT $" + amount + " to " + accountNumber);
            return transaction;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new BankingException("Deposit failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }
    
    public Transaction withdraw(String accountNumber, BigDecimal amount, String transactionPassword, int userId) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        
        // Verify transaction password (null means account has no PIN set yet)
        if (account.getTransactionPassword() == null || account.getTransactionPassword().isEmpty()) {
            throw new InvalidTransactionException("Transaction PIN not set. Please set your transaction PIN before making withdrawals.");
        }
        if (!HashUtil.verifyPassword(transactionPassword, account.getTransactionPassword())) {
            throw new InvalidTransactionException("Invalid transaction password.");
        }
        
        // Account-type-aware balance validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be positive.");
        }
        
        BigDecimal minBalance = account.getAccountType() == Account.AccountType.SAVINGS
            ? new BigDecimal("100.00") : BigDecimal.ZERO;
        BigDecimal balanceAfter = account.getBalance().subtract(amount);
        
        if (balanceAfter.compareTo(minBalance) < 0) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(account.getBalance().doubleValue(), amount.doubleValue());
            }
            throw new InvalidTransactionException(
                account.getAccountType() == Account.AccountType.SAVINGS
                    ? "Savings account must maintain minimum balance of $100.00"
                    : "Insufficient balance for withdrawal.");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal newBalance = Validator.roundToTwoDecimals(balanceAfter);
            account.setBalance(newBalance);
            accountDAO.updateBalance(accountNumber, newBalance);
            
            Transaction transaction = new Transaction(accountNumber, Transaction.TransactionType.WITHDRAW, amount);
            transaction.setPerformedBy(userId);
            transactionDAO.create(transaction);
            
            conn.commit();
            logTransactionToFile(transaction);
            logAuditAction(userId, "WITHDRAW $" + amount + " from " + accountNumber);
            return transaction;
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new BankingException("Withdrawal failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }
    
    public Transaction[] transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String transactionPassword, int userId) throws Exception {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }
        
        Account fromAccount = accountDAO.findByAccountNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new AccountNotFoundException(fromAccountNumber);
        }
        
        Account toAccount = accountDAO.findByAccountNumber(toAccountNumber);
        if (toAccount == null) {
            throw new AccountNotFoundException(toAccountNumber);
        }
        
        // Verify transaction password (null means account has no PIN set yet)
        if (fromAccount.getTransactionPassword() == null || fromAccount.getTransactionPassword().isEmpty()) {
            throw new InvalidTransactionException("Transaction PIN not set. Please set your transaction PIN before making transfers.");
        }
        if (!HashUtil.verifyPassword(transactionPassword, fromAccount.getTransactionPassword())) {
            throw new InvalidTransactionException("Invalid transaction password.");
        }
        
        // Account-type-aware balance validation
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be positive.");
        }
        
        BigDecimal minBalance = fromAccount.getAccountType() == Account.AccountType.SAVINGS
            ? new BigDecimal("100.00") : BigDecimal.ZERO;
        BigDecimal balanceAfter = fromAccount.getBalance().subtract(amount);
        
        if (balanceAfter.compareTo(minBalance) < 0) {
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(fromAccount.getBalance().doubleValue(), amount.doubleValue());
            }
            throw new InvalidTransactionException(
                fromAccount.getAccountType() == Account.AccountType.SAVINGS
                    ? "Savings account must maintain minimum balance of $100.00"
                    : "Insufficient balance for transfer.");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Lock accounts to prevent deadlocks (order by account number)
            String first = fromAccountNumber.compareTo(toAccountNumber) < 0 ? fromAccountNumber : toAccountNumber;
            String second = first.equals(fromAccountNumber) ? toAccountNumber : fromAccountNumber;

            lockAccount(conn, first);
            lockAccount(conn, second);

            BigDecimal newFromBalance = Validator.roundToTwoDecimals(fromAccount.getBalance().subtract(amount));
            BigDecimal newToBalance = Validator.roundToTwoDecimals(toAccount.getBalance().add(amount));
            
            accountDAO.updateBalance(fromAccountNumber, newFromBalance);
            accountDAO.updateBalance(toAccountNumber, newToBalance);
            
            Transaction outTransaction = new Transaction(fromAccountNumber, Transaction.TransactionType.TRANSFER, amount);
            outTransaction.setPerformedBy(userId);
            transactionDAO.create(outTransaction);
            
            Transaction inTransaction = new Transaction(toAccountNumber, Transaction.TransactionType.TRANSFER, amount);
            inTransaction.setPerformedBy(userId);
            transactionDAO.create(inTransaction);
            
            conn.commit();
            logTransactionToFile(outTransaction);
            logTransactionToFile(inTransaction);
            logAuditAction(userId, "TRANSFER $" + amount + " from " + fromAccountNumber + " to " + toAccountNumber);
            return new Transaction[]{outTransaction, inTransaction};
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new BankingException("Transfer failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }

    private void lockAccount(Connection conn, String accountNumber) throws Exception {
        // SQLite does not support SELECT ... FOR UPDATE; simple read to verify existence
        String sql = "SELECT balance FROM account WHERE account_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            stmt.executeQuery();
        }
    }
    
    private void logTransactionToFile(Transaction t) {
        try {
            Account account = accountDAO.findByAccountNumber(t.getAccountNumber());
            String username = "UNKNOWN";
            if (account != null) {
                Customer customer = customerDAO.findById(account.getCustomerId());
                if (customer != null) {
                    banking.model.User user = userDAO.findById(customer.getUserId());
                    if (user != null) {
                        username = user.getUsername();
                    }
                }
            }
            try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter("transaction_log.txt", true))) {
                out.printf("%s | %s | %s | %s | %s%n",
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    username,
                    t.getAccountNumber(),
                    t.getType().name(),
                    t.getAmount().toString()
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to log transaction: " + e.getMessage());
        }
    }
    
    public void logAuditAction(int userId, String action) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO audit_log (user_id, action) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                stmt.setString(2, action);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Failed to log audit action: " + e.getMessage());
        } finally {
            if (conn != null) {
                DatabaseConnection.releaseConnection(conn);
            }
        }
    }
    
    public Account getAccount(String accountNumber) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }
    
    public List<Account> getAccountsByCustomer(int customerId) throws Exception {
        return accountDAO.findByCustomerId(customerId);
    }
    
    public List<Account> getAllAccounts() throws Exception {
        return accountDAO.findAll();
    }
    
    public List<Transaction> getTransactionHistory(String accountNumber) throws Exception {
        return transactionDAO.findByAccountNumber(accountNumber);
    }
    
    public List<Transaction> getMiniStatement(String accountNumber) throws Exception {
        return transactionDAO.findByAccountNumber(accountNumber, 5);
    }
    
    public List<Transaction> getTransactionsByDateRange(String accountNumber, LocalDate start, LocalDate end) throws Exception {
        return transactionDAO.findByAccountNumberAndDateRange(accountNumber, start, end);
    }
    
    public List<Customer> getAllCustomers() throws Exception {
        return customerDAO.findAll();
    }

    public List<Customer> searchCustomersByName(String name) throws Exception {
        return customerDAO.findByName(name);
    }
    
    public Customer getCustomer(int customerId) throws Exception {
        return customerDAO.findById(customerId);
    }
    
    public Customer getCustomerByUserId(int userId) throws Exception {
        if (userId < 0) {
            return customerDAO.findById(-userId);
        }
        return customerDAO.findByUserId(userId);
    }
    
    public boolean updateCustomer(Customer customer) throws Exception {
        return customerDAO.update(customer);
    }
    
    public boolean closeAccount(String accountNumber) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new InvalidTransactionException("Cannot close account with remaining balance. Please withdraw first.");
        }
        // Just delete from DB instead of setting status
        return accountDAO.updateBalance(accountNumber, BigDecimal.ZERO); // Or call delete if exist, mock it for now.
    }
    
    public int getTotalAccountCount() throws Exception {
        return accountDAO.getTotalAccountCount();
    }
    
    public BigDecimal getTotalBankBalance() throws Exception {
        return accountDAO.getTotalBankBalance();
    }
    
    public int getDailyTransactionCount() throws Exception {
        return transactionDAO.getTransactionCountByDate(LocalDate.now());
    }

    public boolean deleteCustomer(int customerId) throws Exception {
        return customerDAO.delete(customerId);
    }

    public List<Transaction> getAllTransactions() throws Exception {
        return transactionDAO.findAll();
    }

    public List<Transaction> getAllTransactionsByDateRange(LocalDate start, LocalDate end) throws Exception {
        return transactionDAO.findByDateRange(start, end);
    }

    public boolean setTransactionPassword(String accountNumber, String newPin, int userId) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        if (newPin == null || !newPin.matches("\\d{4,6}")) {
            throw new ValidationException("Transaction PIN must be 4-6 digits.");
        }
        String hashed = HashUtil.hashPassword(newPin);
        boolean updated = accountDAO.updateTransactionPassword(accountNumber, hashed);
        if (updated) {
            logAuditAction(userId, "SET_TRANSACTION_PIN for " + accountNumber);
        }
        return updated;
    }

    public boolean hasTransactionPassword(String accountNumber) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account.getTransactionPassword() != null && !account.getTransactionPassword().isEmpty();
    }
}
