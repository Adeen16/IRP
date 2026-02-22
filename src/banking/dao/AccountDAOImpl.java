package banking.dao;
import banking.model.Account;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
public class AccountDAOImpl implements AccountDAO {
    public Account findByAccountNumber(String accNumber) { return new Account(); }
    public boolean create(Account account) { return true; }
    public boolean updateBalance(String accNumber, BigDecimal balance) { return true; }
    public List<Account> findByCustomerId(int customerId) { return Collections.emptyList(); }
    public List<Account> findAll() { return Collections.emptyList(); }
    public boolean updateStatus(String accNumber, Account.AccountStatus status) { return true; }
    public int getTotalAccountCount() { return 0; }
    public BigDecimal getTotalBankBalance() { return BigDecimal.ZERO; }
}
