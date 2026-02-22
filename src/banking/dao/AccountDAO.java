package banking.dao;
import banking.model.Account;
import java.math.BigDecimal;
import java.util.List;
public interface AccountDAO {
    Account findByAccountNumber(String accNumber);
    boolean create(Account account);
    boolean updateBalance(String accNumber, BigDecimal balance);
    List<Account> findByCustomerId(int customerId);
    List<Account> findAll();
    boolean updateStatus(String accNumber, Account.AccountStatus status);
    int getTotalAccountCount();
    BigDecimal getTotalBankBalance();
}
