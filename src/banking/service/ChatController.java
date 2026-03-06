package banking.service;

import banking.model.Account;
import banking.model.Customer;
import banking.model.Transaction;
import banking.model.User;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller that sits between the Chat UI and the LLM.
 * Responsibilities:
 *   1. Detect user intent from their message
 *   2. Fetch relevant banking data via BankingService (the ONLY data path)
 *   3. Build context strings for the LLM prompt
 *   4. Call LLMService and return the response
 *
 * The LLM NEVER touches the database directly.
 */
public class ChatController {

    private final LLMService llmService;
    private final BankingService bankingService;
    private final User currentUser;

    public ChatController(User currentUser, BankingService bankingService) {
        this.currentUser = currentUser;
        this.bankingService = bankingService;
        this.llmService = new LLMService();
    }

    public ChatController(User currentUser, BankingService bankingService, String model) {
        this.currentUser = currentUser;
        this.bankingService = bankingService;
        this.llmService = new LLMService(model);
    }

    /**
     * Main entry point: processes a user message and returns the LLM response.
     * This method should be called from a background thread.
     */
    public String processMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Please type a message.";
        }

        String message = userMessage.trim().toLowerCase();
        String bankingContext = "";

        try {
            Intent intent = detectIntent(message);

            switch (intent) {
                case TRANSACTION_HISTORY:
                    bankingContext = buildTransactionContext();
                    break;
                case SPENDING_SUMMARY:
                    bankingContext = buildSpendingSummaryContext();
                    break;
                case BALANCE_INQUIRY:
                    bankingContext = buildBalanceContext();
                    break;
                case TRANSFER_HELP:
                    bankingContext = buildTransferGuideContext();
                    break;
                case ACCOUNT_INFO:
                    bankingContext = buildAccountInfoContext();
                    break;
                case GENERAL_HELP:
                default:
                    bankingContext = buildGeneralContext();
                    break;
            }
        } catch (Exception e) {
            bankingContext = "[Could not retrieve banking data: " + e.getMessage() + "]";
        }

        return llmService.generate(userMessage.trim(), bankingContext);
    }

    /**
     * Checks if the Ollama server is reachable.
     */
    public boolean isLLMAvailable() {
        return llmService.isAvailable();
    }

    public String getModelName() {
        return llmService.getModel();
    }

    public void setModel(String model) {
        llmService.setModel(model);
    }

    // ========== Intent Detection ==========

    private enum Intent {
        TRANSACTION_HISTORY,
        SPENDING_SUMMARY,
        BALANCE_INQUIRY,
        TRANSFER_HELP,
        ACCOUNT_INFO,
        GENERAL_HELP
    }

    /**
     * Simple keyword-based intent detection.
     * Matches the user's message against known patterns.
     */
    private Intent detectIntent(String message) {
        // Transaction/history related
        if (containsAny(message, "transaction", "history", "recent", "activity",
                "last deposit", "last withdraw", "what happened", "why is my balance lower",
                "why did my balance", "explain my")) {
            return Intent.TRANSACTION_HISTORY;
        }

        // Spending summary
        if (containsAny(message, "spending", "summary", "total", "analytics",
                "how much did i spend", "how much have i", "breakdown", "report")) {
            return Intent.SPENDING_SUMMARY;
        }

        // Balance inquiry
        if (containsAny(message, "balance", "how much do i have", "my money",
                "funds", "available")) {
            return Intent.BALANCE_INQUIRY;
        }

        // Transfer guidance
        if (containsAny(message, "transfer", "send money", "how to transfer",
                "how do i send", "move money", "pay someone")) {
            return Intent.TRANSFER_HELP;
        }

        // Account info
        if (containsAny(message, "account", "account type", "savings", "current",
                "account number", "my account", "pin", "transaction pin")) {
            return Intent.ACCOUNT_INFO;
        }

        return Intent.GENERAL_HELP;
    }

    private boolean containsAny(String message, String... keywords) {
        for (String kw : keywords) {
            if (message.contains(kw)) return true;
        }
        return false;
    }

    // ========== Context Builders ==========

    /**
     * Builds context with the user's recent transactions (last 10).
     */
    private String buildTransactionContext() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        if (customer == null) return "No customer profile found.";

        List<Account> accounts = bankingService.getAccountsByCustomer(customer.getCustomerId());
        if (accounts.isEmpty()) return "No accounts found.";

        StringBuilder ctx = new StringBuilder();
        ctx.append("Customer: ").append(customer.getName()).append("\n");

        for (Account acc : accounts) {
            ctx.append("\nAccount: ").append(acc.getAccountNumber());
            ctx.append(" (").append(acc.getAccountType().name()).append(")");
            ctx.append(" | Balance: $").append(acc.getBalance()).append("\n");

            List<Transaction> txns = bankingService.getTransactionHistory(acc.getAccountNumber());
            int limit = Math.min(txns.size(), 10);
            if (limit == 0) {
                ctx.append("  No recent transactions.\n");
            } else {
                ctx.append("  Recent transactions:\n");
                for (int i = 0; i < limit; i++) {
                    Transaction t = txns.get(i);
                    ctx.append("  - ").append(t.getType().name())
                       .append(" $").append(t.getAmount())
                       .append(" on ").append(t.getCreatedAt())
                       .append("\n");
                }
            }
        }
        return ctx.toString();
    }

    /**
     * Builds a spending summary with totals by transaction type.
     */
    private String buildSpendingSummaryContext() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        if (customer == null) return "No customer profile found.";

        List<Account> accounts = bankingService.getAccountsByCustomer(customer.getCustomerId());
        if (accounts.isEmpty()) return "No accounts found.";

        BigDecimal totalDeposits = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalTransfers = BigDecimal.ZERO;
        int txnCount = 0;

        StringBuilder ctx = new StringBuilder();
        ctx.append("Customer: ").append(customer.getName()).append("\n\n");

        for (Account acc : accounts) {
            List<Transaction> txns = bankingService.getTransactionHistory(acc.getAccountNumber());
            for (Transaction t : txns) {
                txnCount++;
                switch (t.getType()) {
                    case DEPOSIT:  totalDeposits = totalDeposits.add(t.getAmount()); break;
                    case WITHDRAW: totalWithdrawals = totalWithdrawals.add(t.getAmount()); break;
                    case TRANSFER: totalTransfers = totalTransfers.add(t.getAmount()); break;
                }
            }
        }

        ctx.append("Spending Summary (all accounts combined):\n");
        ctx.append("  Total Deposits:    $").append(totalDeposits).append("\n");
        ctx.append("  Total Withdrawals: $").append(totalWithdrawals).append("\n");
        ctx.append("  Total Transfers:   $").append(totalTransfers).append("\n");
        ctx.append("  Total Transactions: ").append(txnCount).append("\n");

        BigDecimal currentTotal = BigDecimal.ZERO;
        for (Account acc : accounts) {
            currentTotal = currentTotal.add(acc.getBalance());
        }
        ctx.append("  Combined Balance:  $").append(currentTotal).append("\n");

        return ctx.toString();
    }

    /**
     * Builds balance context for all user accounts.
     */
    private String buildBalanceContext() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        if (customer == null) return "No customer profile found.";

        List<Account> accounts = bankingService.getAccountsByCustomer(customer.getCustomerId());
        if (accounts.isEmpty()) return "No accounts found.";

        StringBuilder ctx = new StringBuilder();
        ctx.append("Customer: ").append(customer.getName()).append("\n\n");
        ctx.append("Account Balances:\n");

        for (Account acc : accounts) {
            ctx.append("  ").append(acc.getAccountNumber())
               .append(" (").append(acc.getAccountType().name()).append(")")
               .append(": $").append(acc.getBalance()).append("\n");
        }

        return ctx.toString();
    }

    /**
     * Builds context to help with transfer guidance.
     */
    private String buildTransferGuideContext() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        StringBuilder ctx = new StringBuilder();

        ctx.append("Transfer Feature Guide:\n");
        ctx.append("- Users can transfer money from their account to another account number.\n");
        ctx.append("- A transaction PIN is required for transfers.\n");
        ctx.append("- SAVINGS accounts must maintain a minimum balance of $100.00 after transfer.\n");
        ctx.append("- CURRENT accounts can transfer down to $0.00.\n");
        ctx.append("- To transfer: go to Dashboard Overview > click 'SEND MONEY (TRANSFER)'.\n");

        if (customer != null) {
            List<Account> accounts = bankingService.getAccountsByCustomer(customer.getCustomerId());
            ctx.append("\nUser's accounts:\n");
            for (Account acc : accounts) {
                ctx.append("  ").append(acc.getAccountNumber())
                   .append(" (").append(acc.getAccountType().name()).append(")")
                   .append(": $").append(acc.getBalance()).append("\n");
            }
        }

        return ctx.toString();
    }

    /**
     * Builds account info context.
     */
    private String buildAccountInfoContext() throws Exception {
        Customer customer = bankingService.getCustomerByUserId(currentUser.getUserId());
        if (customer == null) return "No customer profile found.";

        List<Account> accounts = bankingService.getAccountsByCustomer(customer.getCustomerId());

        StringBuilder ctx = new StringBuilder();
        ctx.append("Customer: ").append(customer.getName()).append("\n");
        ctx.append("CIBIL Score: ").append(customer.getCibilScore()).append("\n\n");
        ctx.append("Accounts:\n");

        for (Account acc : accounts) {
            ctx.append("  Account #: ").append(acc.getAccountNumber()).append("\n");
            ctx.append("  Type: ").append(acc.getAccountType().name()).append("\n");
            ctx.append("  Balance: $").append(acc.getBalance()).append("\n");
            boolean hasPin = bankingService.hasTransactionPassword(acc.getAccountNumber());
            ctx.append("  Transaction PIN set: ").append(hasPin ? "Yes" : "No").append("\n\n");
        }

        return ctx.toString();
    }

    /**
     * Builds minimal context for general help questions.
     */
    private String buildGeneralContext() throws Exception {
        StringBuilder ctx = new StringBuilder();
        ctx.append("Application: Secure Bank Desktop Banking System\n");
        ctx.append("Features available to users:\n");
        ctx.append("  - Dashboard Overview: view balance, quick deposit/withdraw/transfer\n");
        ctx.append("  - Account Statement: view full transaction history with date filtering\n");
        ctx.append("  - Security Settings: change login password\n");
        ctx.append("  - Set Transaction PIN: required for withdrawals and transfers\n");
        ctx.append("  - Request Loan: submit loan requests evaluated by CIBIL score\n");
        ctx.append("  - AI Assistant: this chat (explain transactions, spending summaries, help)\n");
        return ctx.toString();
    }
}
