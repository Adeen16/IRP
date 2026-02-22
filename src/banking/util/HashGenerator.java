package banking.util;

public class HashGenerator {
    public static void main(String[] args) {
        String password = "admin123";
        String saltedHash = PasswordHasher.hashPassword(password);
        System.out.println("SALTED_HASH:" + saltedHash);
    }
}
