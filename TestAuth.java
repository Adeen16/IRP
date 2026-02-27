import banking.service.AuthService;
import banking.model.User;

public class TestAuth {
    public static void main(String[] args) {
        AuthService authService = new AuthService();
        try {
            User u = authService.login("admin", "admin123");
            System.out.println("SUCCESS: " + u.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            User u = authService.login("user", "user123");
            System.out.println("SUCCESS: " + u.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
