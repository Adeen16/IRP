package banking.service;

import banking.dao.UserDAO;
import banking.dao.UserDAOImpl;
import banking.model.User;
import banking.security.AuthSession;
import banking.util.PasswordSecurity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final UserDAO userDAO;
    private static final Map<String, Integer> FAILED_ATTEMPTS = new HashMap<>();
    private static final Map<String, Long> LOCKED_UNTIL = new HashMap<>();

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public User createUser(String username, String password, User.UserRole role, Integer existingCustomerId) throws Exception {
        validatePasswordStrength(password);

        String hashedPassword = PasswordSecurity.hashPassword(password);
        User user = new User(username, hashedPassword, role);

        if (!userDAO.create(user)) {
            throw new Exception("Failed to create user account.");
        }

        if (role == User.UserRole.USER) {
            BankingService bankingService = new BankingService();
            if (existingCustomerId != null) {
                banking.model.Customer customer = bankingService.getCustomer(existingCustomerId);
                if (customer != null) {
                    customer.setUserId(user.getUserId());
                    bankingService.updateCustomer(customer);
                }
            } else {
                banking.model.Customer newCustomer = new banking.model.Customer(username, "000-000-0000",
                    username + "@securebank.com");
                newCustomer.setUserId(user.getUserId());
                int customerId = new banking.dao.CustomerDAOImpl().create(newCustomer);
                newCustomer.setCustomerId(customerId);
                String defaultPin = String.format("%04d", new java.util.Random().nextInt(10000));
                bankingService.createAccount(customerId, banking.model.Account.AccountType.SAVINGS, defaultPin);
            }
        }

        return user;
    }

    public User login(String username, String password) throws Exception {
        validateLoginAccess(username);

        if (!banking.util.DatabaseConnection.testConnection()) {
            throw new Exception("Database is offline. Please ensure SQLite is accessible.");
        }

        User user = userDAO.findByUsername(username);
        if (user == null) {
            registerFailedAttempt(username);
            throw new Exception("User not found.");
        }

        String storedHash = user.getPasswordHash();
        if (!PasswordSecurity.verifyPassword(password, storedHash)) {
            registerFailedAttempt(username);
            throw new Exception("Invalid username or password.");
        }

        if (PasswordSecurity.needsRehash(storedHash)) {
            userDAO.updatePassword(user.getUserId(), PasswordSecurity.hashPassword(password));
        }

        clearFailedAttempts(username);
        AuthSession.start(user);
        return user;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("User not found.");
        }

        if (!PasswordSecurity.verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new Exception("Current password is incorrect.");
        }

        validatePasswordStrength(newPassword);
        return userDAO.updatePassword(userId, PasswordSecurity.hashPassword(newPassword));
    }

    public banking.model.Customer authenticateByNameAndAccountNumber(String customerName, String accountNumber) throws Exception {
        if (!banking.util.DatabaseConnection.testConnection()) {
            throw new Exception("Database is offline. Please ensure SQLite is accessible.");
        }

        banking.dao.CustomerDAO customerDAO = new banking.dao.CustomerDAOImpl();
        banking.model.Customer customer = customerDAO.getCustomerByAccountNumber(accountNumber);
        if (customer != null) {
            String dbName = customer.getName().trim();
            String username = "";
            if (customer.getUserId() > 0) {
                User user = userDAO.findById(customer.getUserId());
                if (user != null) {
                    username = user.getUsername();
                }
            }
            String emailPrefix = customer.getEmail() != null && customer.getEmail().contains("@")
                ? customer.getEmail().split("@")[0] : "";
            String input = customerName.trim();

            if (dbName.equalsIgnoreCase(input)
                || (!username.isEmpty() && username.equalsIgnoreCase(input))
                || (!emailPrefix.isEmpty() && emailPrefix.equalsIgnoreCase(input))
                || input.toLowerCase().contains(dbName.toLowerCase())
                || dbName.toLowerCase().contains(input.toLowerCase())) {
                User sessionUser = new User(customer.getName(), "", User.UserRole.USER);
                sessionUser.setUserId(customer.getUserId() > 0 ? customer.getUserId() : -customer.getCustomerId());
                AuthSession.start(sessionUser);
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

    private void validatePasswordStrength(String password) throws Exception {
        if (!PasswordSecurity.isStrongPassword(password)) {
            throw new Exception(PasswordSecurity.getPasswordRequirements());
        }
    }

    private void validateLoginAccess(String username) throws Exception {
        Long lockedAt = LOCKED_UNTIL.get(username);
        if (lockedAt != null && lockedAt > System.currentTimeMillis()) {
            long remainingMinutes = Math.max(1L, (lockedAt - System.currentTimeMillis()) / 60000L);
            throw new Exception("Too many failed attempts. Account locked for " + remainingMinutes + " more minute(s).");
        }
        if (lockedAt != null && lockedAt <= System.currentTimeMillis()) {
            LOCKED_UNTIL.remove(username);
            FAILED_ATTEMPTS.remove(username);
        }
    }

    private void registerFailedAttempt(String username) {
        int attempts = FAILED_ATTEMPTS.getOrDefault(username, 0) + 1;
        FAILED_ATTEMPTS.put(username, attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            LOCKED_UNTIL.put(username, System.currentTimeMillis() + LOCK_DURATION_MS);
        }
    }

    private void clearFailedAttempts(String username) {
        FAILED_ATTEMPTS.remove(username);
        LOCKED_UNTIL.remove(username);
    }
}
