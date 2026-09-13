import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction represents an immutable record of an individual financial operation
 * (e.g., Deposit, Withdrawal, Transfer) performed on a bank account.
 * 
 * Adheres strictly to OOP Encapsulation with private fields and public getters.
 */
public class Transaction {
    public enum TransactionType {
        DEPOSIT("Deposit"),
        WITHDRAWAL("Withdrawal"),
        TRANSFER_SENT("Transfer Out"),
        TRANSFER_RECEIVED("Transfer In");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime timestamp;
    private final String remarks;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    /**
     * Constructs a new Transaction record.
     *
     * @param transactionId Unique alphanumeric identifier for the transaction
     * @param type          Type of transaction (DEPOSIT, WITHDRAWAL, etc.)
     * @param amount        Transaction monetary value
     * @param balanceAfter  Account balance immediately following this transaction
     * @param remarks       Contextual description or recipient/sender information
     */
    public Transaction(String transactionId, TransactionType type, double amount, double balanceAfter, String remarks) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.remarks = remarks;
    }

    // --- Getters (Encapsulation) ---

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Type: %-13s | Amount: $%,10.2f | Balance: $%,10.2f | Note: %s",
                getFormattedTimestamp(), transactionId, type.getDisplayName(), amount, balanceAfter, remarks);
    }
}
