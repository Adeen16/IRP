package banking.service;

import banking.dao.AccountDAO;
import banking.dao.AccountDAOImpl;
import banking.dao.TransactionDAO;
import banking.dao.TransactionDAOImpl;
import banking.model.Account;
import banking.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsService {
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    
    public AnalyticsService() {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }
    
    public Map<String, Object> getDashboardSummary() throws Exception {
        Map<String, Object> summary = new HashMap<>();
        
        summary.put("totalAccounts", accountDAO.getTotalAccountCount());
        summary.put("totalBalance", accountDAO.getTotalBankBalance());
        summary.put("todayTransactions", transactionDAO.getTransactionCountByDate(LocalDate.now()));
        summary.put("todayDeposits", transactionDAO.getTotalTransactionAmountByDate(LocalDate.now(), Transaction.TransactionType.DEPOSIT));
        summary.put("todayWithdrawals", transactionDAO.getTotalTransactionAmountByDate(LocalDate.now(), Transaction.TransactionType.WITHDRAWAL));
        
        return summary;
    }
    
    public Map<String, BigDecimal> getTransactionSummaryByDate(LocalDate date) throws Exception {
        Map<String, BigDecimal> summary = new HashMap<>();
        
        summary.put("deposits", transactionDAO.getTotalTransactionAmountByDate(date, Transaction.TransactionType.DEPOSIT));
        summary.put("withdrawals", transactionDAO.getTotalTransactionAmountByDate(date, Transaction.TransactionType.WITHDRAWAL));
        summary.put("transfersIn", transactionDAO.getTotalTransactionAmountByDate(date, Transaction.TransactionType.TRANSFER_IN));
        summary.put("transfersOut", transactionDAO.getTotalTransactionAmountByDate(date, Transaction.TransactionType.TRANSFER_OUT));
        
        return summary;
    }
    
    public Map<String, Integer> getAccountTypeDistribution() throws Exception {
        Map<String, Integer> distribution = new HashMap<>();
        List<Account> accounts = accountDAO.findAll();
        
        for (Account account : accounts) {
            String type = account.getAccountType().name();
            distribution.put(type, distribution.getOrDefault(type, 0) + 1);
        }
        
        return distribution;
    }
    
    public Map<String, Object> getAccountAnalytics(String accountNumber) throws Exception {
        Map<String, Object> analytics = new HashMap<>();
        
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            return null;
        }
        
        List<Transaction> transactions = transactionDAO.findByAccountNumber(accountNumber);
        
        analytics.put("accountNumber", accountNumber);
        analytics.put("currentBalance", account.getBalance());
        analytics.put("accountType", account.getAccountType().name());
        analytics.put("status", account.getStatus().name());
        analytics.put("totalTransactions", transactions.size());
        
        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        
        for (Transaction t : transactions) {
            if (t.isCredit()) {
                totalDeposits = totalDeposits.add(t.getAmount());
            } else {
                totalWithdrawals = totalWithdrawals.add(t.getAmount());
            }
        }
        
        analytics.put("totalDeposits", totalDeposits);
        analytics.put("totalWithdrawals", totalWithdrawals);
        analytics.put("netFlow", totalDeposits.subtract(totalWithdrawals));
        
        return analytics;
    }
    
    public List<Transaction> getRecentTransactions(int limit) throws Exception {
        List<Transaction> allTransactions = transactionDAO.findAll();
        if (allTransactions.size() <= limit) {
            return allTransactions;
        }
        return allTransactions.subList(0, limit);
    }
}
