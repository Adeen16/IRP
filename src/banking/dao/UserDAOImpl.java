package banking.dao;
import banking.model.User;
import java.util.Collections;
import java.util.List;
public class UserDAOImpl implements UserDAO {
    public boolean create(User user) { return true; }
    public User findByUsername(String username) { return new User(username, "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9", User.UserRole.ADMIN); }
    public void updateLastLogin(int userId) {}
    public User findById(int userId) { return new User("user", "hash", User.UserRole.USER); }
    public boolean updatePassword(int userId, String hash) { return true; }
    public List<User> findAll() { return Collections.emptyList(); }
    public boolean delete(int userId) { return true; }
}
