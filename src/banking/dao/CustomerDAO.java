package banking.dao;
import banking.model.Customer;
import java.util.List;
public interface CustomerDAO {
    int create(Customer customer);
    Customer findById(int customerId);
    Customer findByUserId(int userId);
    List<Customer> findAll();
    List<Customer> findByName(String name);
    Customer getCustomerByAccountNumber(String accountNumber);
    boolean update(Customer customer);
    boolean delete(int customerId);
}
