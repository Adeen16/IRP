import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true", "root", "");
            System.out.println("Success connecting without password.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
