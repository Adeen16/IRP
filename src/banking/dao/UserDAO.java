package banking.dao;
import banking.model.User;
import java.util.List;
public interface UserDAO {
    boolean create(User user);
    User findByUsername(String username);
    void updateLastLogin(int userId);
    User findById(int userId);
    boolean updatePassword(int userId, String hash);
    List<User> findAll();
    boolean delete(int userId);
}
