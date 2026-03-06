package banking.security;

import banking.model.User;

public final class AuthSession {
    private static User currentUser;

    private AuthSession() {
    }

    public static synchronized void start(User user) {
        currentUser = user;
    }

    public static synchronized void clear() {
        currentUser = null;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isAuthenticated() {
        return currentUser != null;
    }

    public static synchronized boolean matches(User user) {
        return currentUser != null && user != null && currentUser.getUserId() == user.getUserId();
    }
}
