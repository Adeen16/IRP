package banking.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordSecurity {
    private PasswordSecurity() {
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        if (isBCryptHash(storedHash)) {
            try {
                return BCrypt.checkpw(password, storedHash);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        return HashUtil.verifyPassword(password, storedHash);
    }

    public static boolean needsRehash(String storedHash) {
        return !isBCryptHash(storedHash);
    }

    public static boolean isStrongPassword(String password) {
        return password != null
            && password.length() >= 8
            && password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*\\d.*");
    }

    public static String getPasswordRequirements() {
        return "Password must be at least 8 characters and include uppercase, lowercase, and a number.";
    }

    private static boolean isBCryptHash(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }
}
