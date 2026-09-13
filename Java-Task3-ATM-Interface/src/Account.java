import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Account represents an Indian Commercial Bank customer account.
 * Follows strict OOP Encapsulation with private fields, balance validation,
 * failed login attempt tracking (3-attempt lockout), and an ArrayList of Transaction records.
 */
public class Account {
    private final String userId;
    private String pin;
    private final String accountHolderName;
    private final String accountNumber;
    private final String ifscCode;
    private final String accountType; // "Savings Account", "Current Account"
    private double balance;
    private boolean isLocked;
    private int failedAttempts;
    private final ArrayList<Transaction> transactionHistory;

    public static final int MAX_FAILED_ATTEMPTS = 3;

    public Account(String userId, String pin, String accountHolderName, String accountNumber, String ifscCode, String accountType, double initialBalance) {
        this.userId = userId;
        this.pin = pin;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.ifscCode = ifscCode;
        this.accountType = accountType;
        this.balance = Math.max(0.0, initialBalance);
        this.isLocked = false;
        this.failedAttempts = 0;
        this.transactionHistory = new ArrayList<>();

        if (initialBalance > 0) {
            String initialTxId = generateTxId("INIT");
            transactionHistory.add(new Transaction(
                    initialTxId,
                    Transaction.TransactionType.DEPOSIT,
                    initialBalance,
                    this.balance,
                    "Opening Balance / Branch Deposit"
            ));
        }
    }

    // --- Authentication & Security ---

    public boolean validatePin(String enteredPin) {
        if (isLocked) {
            return false;
        }

        if (this.pin.equals(enteredPin)) {
            resetFailedAttempts();
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                isLocked = true;
            }
            return false;
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    public void unlockAccount() {
        this.isLocked = false;
        this.failedAttempts = 0;
    }

    // --- Banking Business Logic ---

    public Transaction deposit(double amount, String remarks) {
        if (amount <= 0) {
            return null;
        }
        this.balance += amount;
        String txId = generateTxId("CR");
        Transaction tx = new Transaction(
                txId,
                Transaction.TransactionType.DEPOSIT,
                amount,
                this.balance,
                remarks == null || remarks.isBlank() ? "Cash Deposit Machine (CDM)" : remarks
        );
        this.transactionHistory.add(tx);
        return tx;
    }

    public Transaction withdraw(double amount, String remarks) {
        if (amount <= 0 || amount > this.balance) {
            return null;
        }
        this.balance -= amount;
        String txId = generateTxId("DR");
        Transaction tx = new Transaction(
                txId,
                Transaction.TransactionType.WITHDRAWAL,
                amount,
                this.balance,
                remarks == null || remarks.isBlank() ? "ATM Cash Dispense" : remarks
        );
        this.transactionHistory.add(tx);
        return tx;
    }

    public Transaction transferOut(double amount, String recipientUserId, String recipientName) {
        if (amount <= 0 || amount > this.balance) {
            return null;
        }
        this.balance -= amount;
        String txId = generateTxId("IMPS");
        String note = String.format("IMPS to %s (A/C: %s)", recipientName, recipientUserId);
        Transaction tx = new Transaction(
                txId,
                Transaction.TransactionType.TRANSFER_SENT,
                amount,
                this.balance,
                note
        );
        this.transactionHistory.add(tx);
        return tx;
    }

    public Transaction transferIn(double amount, String senderUserId, String senderName) {
        if (amount <= 0) {
            return null;
        }
        this.balance += amount;
        String txId = generateTxId("IMPS");
        String note = String.format("IMPS from %s (A/C: %s)", senderName, senderUserId);
        Transaction tx = new Transaction(
                txId,
                Transaction.TransactionType.TRANSFER_RECEIVED,
                amount,
                this.balance,
                note
        );
        this.transactionHistory.add(tx);
        return tx;
    }

    private String generateTxId(String prefix) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("ONB%s%s", prefix, randomSuffix);
    }

    // --- Getters & Encapsulation ---

    public String getUserId() {
        return userId;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "XXXX-XXXX";
        }
        return "XXXX-XXXX-" + accountNumber.substring(accountNumber.length() - 4);
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public int getRemainingAttempts() {
        return Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts);
    }

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    public ArrayList<Transaction> getRawTransactionList() {
        return transactionHistory;
    }
}
