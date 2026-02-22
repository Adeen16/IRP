package banking.model;

import java.sql.Timestamp;

public class User {
    public enum UserRole { ADMIN, USER }
    
    private int userId;
    private String username;
    private String passwordHash;
    private UserRole role;
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    private Timestamp lastLogin;
    private boolean active = true;
    
    public User() {}
    
    public User(String username, String passwordHash, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp ts) { this.lastLogin = ts; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
    public boolean isAdmin() { return role == UserRole.ADMIN; }
}
