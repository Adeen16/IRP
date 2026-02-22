package banking.dao;
import banking.model.Customer;
import java.util.Collections;
import java.util.List;
public class CustomerDAOImpl implements CustomerDAO {
    public int create(Customer customer) { return 1; }
    public Customer findById(int customerId) { return new Customer(); }
    public List<Customer> findAll() { return Collections.emptyList(); }
    public List<Customer> findByName(String name) { return Collections.emptyList(); }
    public boolean update(Customer customer) { return true; }
    public boolean delete(int customerId) { return true; }
}
