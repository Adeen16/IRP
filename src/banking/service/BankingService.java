package banking.service;

import banking.dao.AccountDAO;
import banking.dao.AccountDAOImpl;
import banking.dao.CustomerDAO;
import banking.dao.CustomerDAOImpl;
import banking.dao.TransactionDAO;
import banking.dao.TransactionDAOImpl;
import banking.exception.AccountNotFoundException;
import banking.exception.BankingException;
import banking.exception.InsufficientBalanceException;
import banking.exception.InvalidTransactionException;
import banking.exception.ValidationException;
import banking.model.Account;
import banking.model.Customer;
import banking.model.Transaction;
import banking.util.DatabaseConnection;
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
    
    public BankingService() {
        this.accountDAO = new AccountDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }
    
    public Customer createCustomer(String name, String phone, String email, String address) throws Exception {
        Validator.ValidationResult validation = Validator.validateCustomer(name, phone, email);
        if (!validation.isValid()) {
            throw new ValidationException(validation.getMessage());
        }
        
        Customer customer = new Customer(name, phone, email);
        customer.setAddress(address);
        int customerId = customerDAO.create(customer);
        customer.setCustomerId(customerId);
        return customer;
    }
    
    public Account createAccount(int customerId, Account.AccountType accountType) throws Exception {
        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new ValidationException("Customer not found");
        }
        
        // Ensure account number is unique
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountDAO.findByAccountNumber(accountNumber) != null);

        Account account = new Account(accountNumber, customerId, accountType);
        accountDAO.create(account);
        return account;
    }
    
    private String generateAccountNumber() {
        return "BA" + String.format("%010d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 10000000000L);
    }
    
    public Transaction deposit(String accountNumber, BigDecimal amount) throws Exception {
        if (!Validator.isDepositAllowed(amount)) {
            throw new InvalidTransactionException("Invalid deposit amount. Amount must be positive and within limits.");
        }
        
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        if (!account.isActive()) {
            throw new InvalidTransactionException("Account is not active");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal newBalance = Validator.roundToTwoDecimals(account.getBalance().add(amount));
            account.setBalance(newBalance);
            accountDAO.updateBalance(accountNumber, newBalance);
            
            Transaction transaction = new Transaction(accountNumber, Transaction.TransactionType.DEPOSIT, amount, newBalance);
            transaction.setDescription("Cash deposit");
            transactionDAO.create(transaction);
            
            conn.commit();
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
    
    public Transaction withdraw(String accountNumber, BigDecimal amount) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        if (!account.isActive()) {
            throw new InvalidTransactionException("Account is not active");
        }
        
        if (!Validator.isWithdrawalAllowed(account.getBalance(), amount)) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(account.getBalance().doubleValue(), amount.doubleValue());
            }
            throw new InvalidTransactionException("Withdrawal not allowed. Check amount limits and minimum balance.");
        }
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            BigDecimal newBalance = Validator.roundToTwoDecimals(account.getBalance().subtract(amount));
            account.setBalance(newBalance);
            accountDAO.updateBalance(accountNumber, newBalance);
            
            Transaction transaction = new Transaction(accountNumber, Transaction.TransactionType.WITHDRAWAL, amount, newBalance);
            transaction.setDescription("Cash withdrawal");
            transactionDAO.create(transaction);
            
            conn.commit();
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
    
    public Transaction[] transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) throws Exception {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidTransactionException("Cannot transfer to the same account");
        }
        
        Account fromAccount = accountDAO.findByAccountNumber(fromAccountNumber);
        if (fromAccount == null) {
            throw new AccountNotFoundException(fromAccountNumber);
        }
        if (!fromAccount.isActive()) {
            throw new InvalidTransactionException("Source account is not active");
        }
        
        Account toAccount = accountDAO.findByAccountNumber(toAccountNumber);
        if (toAccount == null) {
            throw new AccountNotFoundException(toAccountNumber);
        }
        if (!toAccount.isActive()) {
            throw new InvalidTransactionException("Destination account is not active");
        }
        
        if (!Validator.isTransferAllowed(fromAccount.getBalance(), amount)) {
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientBalanceException(fromAccount.getBalance().doubleValue(), amount.doubleValue());
            }
            throw new InvalidTransactionException("Transfer not allowed. Check amount limits and minimum balance.");
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
            
            Transaction outTransaction = new Transaction(fromAccountNumber, Transaction.TransactionType.TRANSFER_OUT, amount, newFromBalance);
            outTransaction.setReferenceAccount(toAccountNumber);
            outTransaction.setDescription("Transfer to " + toAccountNumber);
            transactionDAO.create(outTransaction);
            
            Transaction inTransaction = new Transaction(toAccountNumber, Transaction.TransactionType.TRANSFER_IN, amount, newToBalance);
            inTransaction.setReferenceAccount(fromAccountNumber);
            inTransaction.setDescription("Transfer from " + fromAccountNumber);
            transactionDAO.create(inTransaction);
            
            conn.commit();
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
        String sql = "SELECT balance FROM accounts WHERE account_number = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            stmt.executeQuery();
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
        return accountDAO.updateStatus(accountNumber, Account.AccountStatus.CLOSED);
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
}
