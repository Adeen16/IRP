import java.sql.Connection;
import java.sql.DriverManager;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:sqlite:bank.db");
            System.out.println("Success connecting to SQLite database.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
