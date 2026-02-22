package banking.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password hashing.
 * Hardened to match existing database hashes and provide consistent SHA-256 hex output.
 */
public class HashUtil {

    /**
     * Hashes a password using SHA-256 and returns a hex string.
     * This matches the habit of the existing database schema (schema.sql).
     */
    public static String hashPassword(String password) {
        if (password == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA-256 algorithm not found!");
            return password; // Fallback to plain text (caution)
        }
    }

    /**
     * Verifies a password against a stored hash.
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        String hashedInput = hashPassword(password);
        return hashedInput.equals(storedHash);
    }
}
