package banking.service;

import banking.dao.UserDAO;
import banking.dao.UserDAOImpl;
import banking.model.User;
import banking.util.HashUtil;

import java.util.List;

public class AuthService {
    private UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public User createUser(String username, String password, User.UserRole role, Integer existingCustomerId) throws Exception {
        String hashedPassword = HashUtil.hashPassword(password);
        User user = new User(username, hashedPassword, role);
        
        if (userDAO.create(user)) {
            if (role == User.UserRole.USER) {
                BankingService bankingService = new BankingService();
                if (existingCustomerId != null) {
                    banking.model.Customer c = bankingService.getCustomer(existingCustomerId);
                    if (c != null) {
                        c.setUserId(user.getUserId());
                        bankingService.updateCustomer(c);
                    }
                } else {
                    banking.model.Customer newCustomer = bankingService.createCustomer(username, "000-000-0000", username + "@securebank.com", "N/A");
                    newCustomer.setUserId(user.getUserId());
                    bankingService.updateCustomer(newCustomer);
                    // Default SAVINGS account with a generated 4-digit transaction PIN
                    String defaultPin = String.format("%04d", new java.util.Random().nextInt(10000));
                    bankingService.createAccount(newCustomer.getCustomerId(), banking.model.Account.AccountType.SAVINGS, defaultPin);
                }
            }
            return user;
        } else {
            throw new Exception("Failed to create user account.");
        }
    }

    public User login(String username, String password) throws Exception {
        if (!banking.util.DatabaseConnection.testConnection()) {
            throw new Exception("Database is Offline. Please start the MySQL service.");
        }
        try {
            System.out.println("Login attempt for user: " + username);
            User user = userDAO.findByUsername(username);
            
            if (user == null) {
                System.out.println("User not found in database: " + username);
                throw new Exception("User not found.");
            }



            String hashedInput = HashUtil.hashPassword(password);
            String storedHash = user.getPasswordHash(); // Fixed method name

            // DEBUGGING LOGS (User will see these in console)
            System.out.println("DEBUG: Input password length: " + password.length());
            System.out.println("DEBUG: Hashed input: " + hashedInput);
            System.out.println("DEBUG: Stored hash: " + storedHash);

            if (hashedInput.equalsIgnoreCase(storedHash)) { // Case-insensitive for safety
                return user;
            } else {
                System.out.println("Authentication failed: Password mismatch for user: " + username);
                throw new Exception("Invalid username or password.");
            }
        } catch (Exception e) {
            // Check if it's a database connection issue
            if (e.getMessage() != null && e.getMessage().contains("Database connection pool not initialized")) {
                throw new Exception("Database is currently unreachable. Please ensure MySQL is running.");
            }
            throw e;
        }
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }

        String hashedOld = HashUtil.hashPassword(oldPassword);
        if (!hashedOld.equals(user.getPasswordHash())) {
            throw new Exception("Current password is incorrect.");
        }

        String hashedNew = HashUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, hashedNew);
    }
    
    public banking.model.Customer authenticateByNameAndAccountNumber(String customerName, String accountNumber) throws Exception {
        if (!banking.util.DatabaseConnection.testConnection()) {
            throw new Exception("Database is Offline. Please start the MySQL service.");
        }
        banking.dao.CustomerDAO customerDAO = new banking.dao.CustomerDAOImpl();
        banking.model.Customer customer = customerDAO.getCustomerByAccountNumber(accountNumber);
        
        if (customer != null) {
            String dbName = customer.getName().trim();
            String username = "";
            if (customer.getUserId() > 0) {
                User u = userDAO.findById(customer.getUserId());
                if (u != null) username = u.getUsername();
            }
            String emailPrefix = customer.getEmail() != null && customer.getEmail().contains("@") 
                                 ? customer.getEmail().split("@")[0] : "";
            
            String input = customerName.trim();
            
            if (dbName.equalsIgnoreCase(input) || 
                (!username.isEmpty() && username.equalsIgnoreCase(input)) || 
                (!emailPrefix.isEmpty() && emailPrefix.equalsIgnoreCase(input)) ||
                input.toLowerCase().contains(dbName.toLowerCase()) ||
                dbName.toLowerCase().contains(input.toLowerCase())) {
                return customer;
            }
        }
        throw new Exception("Invalid Customer Name or Account Number.");
    }

    public List<User> getAllUsers() throws Exception {
        return userDAO.findAll();
    }
    
    public boolean deleteUser(int userId) throws Exception {
        return userDAO.delete(userId);
    }
}
