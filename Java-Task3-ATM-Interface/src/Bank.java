import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Bank represents Oasis National Bank of India (ONBI).
 * Manages customer account records, authentication with 3-attempt security lockout,
 * and handles inter-account transfers (IMPS / Internal Transfers).
 */
public class Bank {
    private final String bankName;
    private final String bankShortCode;
    private final String defaultIfsc;
    private final Map<String, Account> accounts;

    public static class TransferResult {
        private final boolean success;
        private final String message;
        private final Transaction senderTransaction;

        public TransferResult(boolean success, String message, Transaction senderTransaction) {
            this.success = success;
            this.message = message;
            this.senderTransaction = senderTransaction;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Transaction getSenderTransaction() {
            return senderTransaction;
        }
    }

    public Bank(String bankName, String bankShortCode, String defaultIfsc) {
        this.bankName = bankName;
        this.bankShortCode = bankShortCode;
        this.defaultIfsc = defaultIfsc;
        this.accounts = new HashMap<>();
    }

    public void registerAccount(Account account) {
        if (account != null && account.getUserId() != null) {
            accounts.put(account.getUserId(), account);
        }
    }

    public boolean accountExists(String userId) {
        return userId != null && accounts.containsKey(userId);
    }

    public Account getAccount(String userId) {
        return accounts.get(userId);
    }

    public AuthResult authenticateUser(String userId, String enteredPin) {
        Account account = getAccount(userId);
        if (account == null) {
            return new AuthResult(false, "Customer User ID not registered in bank records.", null, 0);
        }

        if (account.isLocked()) {
            return new AuthResult(false, "ACCOUNT BLOCKED: 3 consecutive incorrect PIN attempts. Please visit your home branch with KYC documents or call 1800-425-3800.", account, 0);
        }

        boolean pinMatches = account.validatePin(enteredPin);
        if (pinMatches) {
            return new AuthResult(true, "Authentication successful.", account, account.getRemainingAttempts());
        } else {
            if (account.isLocked()) {
                return new AuthResult(false, "3 consecutive incorrect PIN attempts! Account has been TEMPORARILY FROZEN for security.", account, 0);
            } else {
                return new AuthResult(false, "Incorrect PIN. Attempts remaining: " + account.getRemainingAttempts(), account, account.getRemainingAttempts());
            }
        }
    }

    public TransferResult transferFunds(String senderUserId, String recipientUserId, double amount) {
        if (senderUserId == null || recipientUserId == null) {
            return new TransferResult(false, "Invalid account identifiers.", null);
        }

        if (senderUserId.trim().equalsIgnoreCase(recipientUserId.trim())) {
            return new TransferResult(false, "Self-transfer not permitted. Please enter a different beneficiary account.", null);
        }

        Account sender = getAccount(senderUserId);
        if (sender == null) {
            return new TransferResult(false, "Remitter account not found.", null);
        }

        Account recipient = getAccount(recipientUserId);
        if (recipient == null) {
            return new TransferResult(false, "Beneficiary Account ID '" + recipientUserId + "' not found.", null);
        }

        if (amount <= 0) {
            return new TransferResult(false, "Transfer amount must be strictly greater than ₹0.00.", null);
        }

        // Strict balance check before transfer (Requirement: display 'Insufficient Funds')
        if (sender.getBalance() < amount) {
            return new TransferResult(false, "Insufficient Funds: Available balance is ₹" + String.format("%,.2f", sender.getBalance()), null);
        }

        Transaction outTx = sender.transferOut(amount, recipient.getUserId(), recipient.getAccountHolderName());
        if (outTx == null) {
            return new TransferResult(false, "Remitter debit failed.", null);
        }

        Transaction inTx = recipient.transferIn(amount, sender.getUserId(), sender.getAccountHolderName());
        if (inTx == null) {
            sender.deposit(amount, "Reversal: Failed IMPS to " + recipientUserId);
            return new TransferResult(false, "Beneficiary credit failed. Funds reversed.", null);
        }

        return new TransferResult(true, "IMPS Fund Transfer of ₹" + String.format("%,.2f", amount) + " to " + recipient.getAccountHolderName() + " successful. UTR: " + outTx.getTransactionId(), outTx);
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankShortCode() {
        return bankShortCode;
    }

    public String getDefaultIfsc() {
        return defaultIfsc;
    }

    public Map<String, Account> getAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    public static class AuthResult {
        private final boolean authenticated;
        private final String message;
        private final Account account;
        private final int remainingAttempts;

        public AuthResult(boolean authenticated, String message, Account account, int remainingAttempts) {
            this.authenticated = authenticated;
            this.message = message;
            this.account = account;
            this.remainingAttempts = remainingAttempts;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public String getMessage() {
            return message;
        }

        public Account getAccount() {
            return account;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }
    }
}
