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

    public User createUser(String username, String password, User.UserRole role, Integer customerId) throws Exception {
        String hashedPassword = HashUtil.hashPassword(password);
        User user = new User(username, hashedPassword, role);
        
        if (userDAO.create(user)) {
            return user;
        } else {
            throw new Exception("Failed to create user account.");
        }
    }

    public User login(String username, String password) throws Exception {
        try {
            System.out.println("Login attempt for user: " + username);
            User user = userDAO.findByUsername(username);
            
            if (user == null) {
                System.out.println("User not found in database: " + username);
                throw new Exception("User not found.");
            }

            if (!user.isActive()) {
                System.out.println("User account is inactive: " + username);
                throw new Exception("Account is inactive. Please contact your administrator.");
            }

            String hashedInput = HashUtil.hashPassword(password);
            String storedHash = user.getPasswordHash(); // Fixed method name

            // DEBUGGING LOGS (User will see these in console)
            System.out.println("DEBUG: Input password length: " + password.length());
            System.out.println("DEBUG: Hashed input: " + hashedInput);
            System.out.println("DEBUG: Stored hash: " + storedHash);

            if (hashedInput.equalsIgnoreCase(storedHash)) { // Case-insensitive for safety
                System.out.println("Authentication successful for user: " + username);
                userDAO.updateLastLogin(user.getUserId());
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
    
    public List<User> getAllUsers() throws Exception {
        return userDAO.findAll();
    }
    
    public boolean deleteUser(int userId) throws Exception {
        return userDAO.delete(userId);
    }
}
