import java.util.List;
import java.util.Scanner;

/**
 * ATM represents the interactive banking terminal for Oasis National Bank of India (ONBI).
 * 
 * Strict compliance with project checklist:
 * 1. Startup prompt for User ID and PIN; deny access after 3 incorrect attempts.
 * 2. Main menu displayed after successful login with 5 options:
 *    [1] Transaction History — display a log of all past transactions in the current session
 *    [2] Withdraw — prompt for amount; validate sufficient balance; update balance; log transaction
 *    [3] Deposit — prompt for amount; update balance; log transaction
 *    [4] Transfer — prompt for recipient account ID and amount; validate balance; update both accounts; log transaction
 *    [5] Quit — display a goodbye message and exit
 * 3. Balance check before any withdrawal or transfer; display "Insufficient Funds" if balance is too low
 * 4. All transactions stored in an ArrayList and displayed clearly in Transaction History
 * 5. Distinct Java classes: ATM, Account, Transaction, Bank, Main
 * 6. Switch-case for the menu system
 * 
 * Tailored to Indian Banking Standards:
 * - Indian Rupee (₹ / INR) formatting
 * - Indian Banknote denominations (₹500, ₹200, ₹100 notes)
 * - Official Bank thermal receipt with IFSC, UTR, and masked Account Number
 * - Safe input handling
 */
public class ATM {
    // --- ANSI Color Constants ---
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BG_RED = "\u001B[41m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    private final Bank bank;
    private final Scanner scanner;
    private Account currentAccount;
    private final String terminalId;

    public ATM(Bank bank) {
        this.bank = bank;
        this.scanner = new Scanner(System.in);
        this.terminalId = "ATM-MUM-400021";
        this.currentAccount = null;
    }

    public void start() {
        printWelcomeBanner();

        boolean systemRunning = true;
        while (systemRunning) {
            currentAccount = performAuthentication();

            if (currentAccount != null) {
                printNotification(GREEN, "CHIP CARD AUTHENTICATED",
                        "Welcome to Oasis National Bank of India, " + currentAccount.getAccountHolderName() + "!\n" +
                        "A/C: " + currentAccount.getMaskedAccountNumber() + " | Branch IFSC: " + currentAccount.getIfscCode() + " | Session Secured.");
                runSessionMenu();
            }

            System.out.println();
            printDivider();
            System.out.print(BRIGHT_YELLOW + "Insert new debit card / Start another session? (Y/N): " + RESET);
            String resp = safeReadLine();
            if (!resp.equalsIgnoreCase("y") && !resp.equalsIgnoreCase("yes")) {
                systemRunning = false;
                printShutdownBanner();
            }
        }
    }

    private Account performAuthentication() {
        System.out.println("\n" + CYAN + "+------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|           OASIS NATIONAL BANK OF INDIA - CARD READER             |" + RESET);
        System.out.println(CYAN + "+------------------------------------------------------------------+" + RESET);

        int attemptsMade = 0;
        final int MAX_ATTEMPTS = 3;

        while (attemptsMade < MAX_ATTEMPTS) {
            System.out.print("\n" + BRIGHT_WHITE + "  Enter Customer ID / User ID (type 'demo' for test accounts): " + RESET);
            String userId = safeReadLine();

            if (userId.equalsIgnoreCase("demo")) {
                displayDemoAccounts();
                continue;
            }

            if (userId.isEmpty()) {
                printNotification(YELLOW, "INPUT REQUIRED", "Customer ID cannot be empty.");
                continue;
            }

            System.out.print(BRIGHT_WHITE + "  Enter 4-Digit ATM PIN: " + RESET);
            String pin = safeReadLine();

            Bank.AuthResult auth = bank.authenticateUser(userId, pin);

            if (auth.isAuthenticated()) {
                return auth.getAccount();
            } else {
                attemptsMade++;
                int remaining = MAX_ATTEMPTS - attemptsMade;

                System.out.println();
                if (remaining > 0) {
                    printNotification(RED, "AUTHENTICATION FAILED", auth.getMessage());
                    printNotification(YELLOW, "SECURITY NOTICE", remaining + " attempt(s) remaining before account lockout.");
                } else {
                    printNotification(BG_RED + BRIGHT_WHITE, "ACCESS DENIED - SECURITY LOCKOUT",
                            "You have entered an incorrect PIN 3 times consecutively.\n" +
                            "For cardholder protection, your debit card has been blocked.\n" +
                            "Please visit your home branch or call National Toll-Free: 1800-425-3800.");
                    return null;
                }
            }
        }

        return null;
    }

    private void runSessionMenu() {
        boolean inSession = true;

        while (inSession) {
            displayMainMenu();
            int choice = readIntegerInput(BRIGHT_YELLOW + "  Select Banking Service [1-5]: " + RESET, 1, 5);

            // Mandatory requirement: switch-case for menu system
            switch (choice) {
                case 1:
                    handleTransactionHistory();
                    break;
                case 2:
                    handleWithdraw();
                    break;
                case 3:
                    handleDeposit();
                    break;
                case 4:
                    handleTransfer();
                    break;
                case 5:
                    handleQuit();
                    inSession = false;
                    break;
                default:
                    printNotification(RED, "INVALID SELECTION", "Please select a valid option from 1 to 5.");
                    break;
            }

            if (inSession) {
                pauseForUser();
            }
        }
    }

    private void displayMainMenu() {
        System.out.println("\n" + CYAN + "+------------------------------------------------------------------+" + RESET);
        System.out.printf(CYAN + "|  %-27s Account: %-23s|%n" + RESET,
                truncate("A/C: " + currentAccount.getAccountHolderName(), 27),
                currentAccount.getMaskedAccountNumber());
        System.out.printf(CYAN + "|  Available Bal: " + GREEN + BOLD + "Rs. %-14s" + RESET + CYAN + " Terminal: %-20s|%n" + RESET,
                String.format("%,.2f", currentAccount.getBalance()), terminalId);
        System.out.println(CYAN + "+------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|                    MAIN ATM BANKING SERVICES                     |" + RESET);
        System.out.println(CYAN + "+------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|                                                                  |" + RESET);
        System.out.println(CYAN + "|   " + BRIGHT_WHITE + "[1] Transaction History" + RESET + CYAN + "     View passbook & account statement          |" + RESET);
        System.out.println(CYAN + "|   " + BRIGHT_WHITE + "[2] Cash Withdrawal" + RESET + CYAN + "         Dispense Indian Rupee banknotes            |" + RESET);
        System.out.println(CYAN + "|   " + BRIGHT_WHITE + "[3] Cash Deposit (CDM)" + RESET + CYAN + "      Deposit currency into savings account      |" + RESET);
        System.out.println(CYAN + "|   " + BRIGHT_WHITE + "[4] Fund Transfer (IMPS)" + RESET + CYAN + "    Instant inter-bank / intra-bank transfer   |" + RESET);
        System.out.println(CYAN + "|   " + BRIGHT_WHITE + "[5] Quit & Eject Card" + RESET + CYAN + "       End session, print receipt & exit          |" + RESET);
        System.out.println(CYAN + "|                                                                  |" + RESET);
        System.out.println(CYAN + "+------------------------------------------------------------------+" + RESET);
    }

    private void handleTransactionHistory() {
        printSubHeader("ACCOUNT PASSBOOK & TRANSACTION STATEMENT");

        List<Transaction> history = currentAccount.getTransactionHistory();

        if (history.isEmpty()) {
            printNotification(YELLOW, "PASSBOOK EMPTY", "No transaction records found for this account.");
            return;
        }

        System.out.println(CYAN + "+----------------------+--------------+------------------+--------------+--------------+----------------------------------+" + RESET);
        System.out.printf(CYAN + "| " + BOLD + "%-20s" + RESET + CYAN + " | " + BOLD + "%-12s" + RESET + CYAN + " | " + BOLD + "%-16s" + RESET + CYAN + " | " + BOLD + "%-12s" + RESET + CYAN + " | " + BOLD + "%-12s" + RESET + CYAN + " | " + BOLD + "%-32s" + RESET + CYAN + " |%n",
                "Date & Time", "Txn Ref No", "Mode / Type", "Amount (Rs.)", "Balance (Rs.)", "Narration / Remarks");
        System.out.println(CYAN + "+----------------------+--------------+------------------+--------------+--------------+----------------------------------+" + RESET);

        for (Transaction tx : history) {
            String color = switch (tx.getType()) {
                case DEPOSIT, TRANSFER_RECEIVED -> GREEN;
                case WITHDRAWAL, TRANSFER_SENT -> RED;
            };

            String typeStr = tx.getType().getDisplayName();
            String amtSign = (tx.getType() == Transaction.TransactionType.DEPOSIT || tx.getType() == Transaction.TransactionType.TRANSFER_RECEIVED) ? "+" : "-";

            System.out.printf(CYAN + "|" + RESET + " %-20s " + CYAN + "|" + RESET + " %-12s " + CYAN + "|" + RESET + " %-16s " + CYAN + "|" + color + " " + amtSign + "%-11s " + RESET + CYAN + "|" + RESET + " %-12s " + CYAN + "|" + RESET + " %-32s " + CYAN + "|%n" + RESET,
                    tx.getFormattedTimestamp(),
                    tx.getTransactionId(),
                    typeStr,
                    String.format("%,.2f", tx.getAmount()),
                    String.format("%,.2f", tx.getBalanceAfter()),
                    truncate(tx.getRemarks(), 32)
            );
        }

        System.out.println(CYAN + "+----------------------+--------------+------------------+--------------+--------------+----------------------------------+" + RESET);
        System.out.printf("  Total Ledger Entries: " + BOLD + "%d" + RESET + " | Current Balance: " + GREEN + BOLD + "Rs. %,.2f\n" + RESET,
                history.size(), currentAccount.getBalance());

        System.out.print("\n" + BRIGHT_YELLOW + "Print physical ATM thermal receipt for latest entry? (Y/N): " + RESET);
        String choice = safeReadLine();
        if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
            printReceipt(history.get(history.size() - 1));
        }
    }

    private void handleWithdraw() {
        printSubHeader("CASH WITHDRAWAL");

        System.out.println("  Fast Cash Shortcuts:");
        System.out.println("  [1] Rs. 500       [2] Rs. 1,000      [3] Rs. 2,000");
        System.out.println("  [4] Rs. 5,000     [5] Rs. 10,000     [6] Other Custom Amount");
        System.out.println("  [7] Cancel");

        int option = readIntegerInput("\n" + BRIGHT_YELLOW + "Select option [1-7]: " + RESET, 1, 7);

        double amount = 0;
        switch (option) {
            case 1 -> amount = 500;
            case 2 -> amount = 1000;
            case 3 -> amount = 2000;
            case 4 -> amount = 5000;
            case 5 -> amount = 10000;
            case 6 -> amount = readDoubleInput("  Enter custom withdrawal amount (Rs.): ");
            case 7 -> {
                System.out.println(YELLOW + "  Withdrawal cancelled." + RESET);
                return;
            }
        }

        if (amount <= 0) {
            printNotification(RED, "INVALID AMOUNT", "Withdrawal amount must be greater than Rs. 0.00.");
            return;
        }

        // Strict Requirement: Balance check before withdrawal -> display "Insufficient Funds"
        if (amount > currentAccount.getBalance()) {
            printNotification(BG_RED + BRIGHT_WHITE, "INSUFFICIENT FUNDS",
                    "Transaction Declined by Bank!\n" +
                    "Requested Amount  : Rs. " + String.format("%,.2f", amount) + "\n" +
                    "Available Balance : Rs. " + String.format("%,.2f", currentAccount.getBalance()) + "\n" +
                    "Shortfall Amount  : Rs. " + String.format("%,.2f", (amount - currentAccount.getBalance())));
            return;
        }

        simulateProcessing("Authorizing cash dispensation with National Financial Switch (NFS)...", 600);

        Transaction tx = currentAccount.withdraw(amount, "ATM Cash Dispenser");
        if (tx != null) {
            dispenseIndianCashNotes(amount);
            printNotification(GREEN, "TRANSACTION SUCCESSFUL",
                    "Amount Dispensed   : Rs. " + String.format("%,.2f", amount) + "\n" +
                    "Updated Balance    : Rs. " + String.format("%,.2f", currentAccount.getBalance()) + "\n" +
                    "Please collect cash from the cash tray below.");

            askForReceipt(tx);
        } else {
            printNotification(RED, "ERROR", "Withdrawal processing error. Please contact branch.");
        }
    }

    private void handleDeposit() {
        printSubHeader("CASH DEPOSIT (CDM / CASH RECYCLER)");

        System.out.println("  Please insert banknotes into the Cash Deposit Machine (CDM) slot.");
        double amount = readDoubleInput(BRIGHT_WHITE + "  Enter total deposit value (Rs.): " + RESET);

        if (amount <= 0) {
            printNotification(RED, "INVALID AMOUNT", "Deposit value must be strictly greater than Rs. 0.00.");
            return;
        }

        simulateProcessing("Validating banknote security thread, watermark, and magnetic ink...", 600);

        Transaction tx = currentAccount.deposit(amount, "CDM Cash Deposit");
        if (tx != null) {
            printNotification(GREEN, "CASH DEPOSITED SUCCESSFULLY",
                    "Amount Credited    : Rs. " + String.format("%,.2f", amount) + "\n" +
                    "Updated Balance    : Rs. " + String.format("%,.2f", currentAccount.getBalance()));

            askForReceipt(tx);
        } else {
            printNotification(RED, "ERROR", "Deposit failed. Please try again.");
        }
    }

    private void handleTransfer() {
        printSubHeader("INTER-BANK / INTRA-BANK FUND TRANSFER (IMPS 24x7)");

        System.out.print(BRIGHT_WHITE + "  Enter Beneficiary Customer ID (e.g. 1002, 1003): " + RESET);
        String recipientId = safeReadLine();

        if (recipientId.isEmpty()) {
            printNotification(RED, "INPUT REQUIRED", "Beneficiary ID cannot be empty.");
            return;
        }

        if (recipientId.equalsIgnoreCase(currentAccount.getUserId())) {
            printNotification(RED, "TRANSFER REJECTED", "Self-transfer not permitted. Please enter a different beneficiary.");
            return;
        }

        if (!bank.accountExists(recipientId)) {
            printNotification(RED, "BENEFICIARY NOT FOUND", "No registered account found with Customer ID '" + recipientId + "'.");
            return;
        }

        Account recipient = bank.getAccount(recipientId);
        System.out.println(CYAN + "  -> Beneficiary Verified: " + BOLD + recipient.getAccountHolderName() + RESET +
                " (" + recipient.getMaskedAccountNumber() + ") | IFSC: " + recipient.getIfscCode());

        double amount = readDoubleInput(BRIGHT_WHITE + "  Enter transfer amount (Rs.): " + RESET);

        if (amount <= 0) {
            printNotification(RED, "INVALID AMOUNT", "Transfer amount must be strictly greater than Rs. 0.00.");
            return;
        }

        // Strict Requirement: Balance check before transfer -> display "Insufficient Funds"
        if (amount > currentAccount.getBalance()) {
            printNotification(BG_RED + BRIGHT_WHITE, "INSUFFICIENT FUNDS",
                    "Fund Transfer Declined!\n" +
                    "Transfer Requested: Rs. " + String.format("%,.2f", amount) + "\n" +
                    "Available Balance : Rs. " + String.format("%,.2f", currentAccount.getBalance()));
            return;
        }

        System.out.print("\n" + BRIGHT_YELLOW + "Confirm IMPS transfer of Rs. " + String.format("%,.2f", amount) +
                " to " + recipient.getAccountHolderName() + "? (Y/N): " + RESET);
        String confirm = safeReadLine();
        if (!confirm.equalsIgnoreCase("y") && !confirm.equalsIgnoreCase("yes")) {
            System.out.println(YELLOW + "  Transfer aborted by user." + RESET);
            return;
        }

        simulateProcessing("Connecting to National Payments Corporation of India (NPCI) gateway...", 600);

        Bank.TransferResult result = bank.transferFunds(currentAccount.getUserId(), recipientId, amount);

        if (result.isSuccess()) {
            printNotification(GREEN, "IMPS TRANSFER COMPLETED",
                    result.getMessage() + "\n" +
                    "Available Balance: Rs. " + String.format("%,.2f", currentAccount.getBalance()));

            askForReceipt(result.getSenderTransaction());
        } else {
            printNotification(RED, "TRANSFER FAILED", result.getMessage());
        }
    }

    private void handleQuit() {
        System.out.println();
        simulateProcessing("Safely closing encrypted session and releasing card lock...", 600);

        System.out.println(GREEN + "+------------------------------------------------------------------+" + RESET);
        System.out.println(GREEN + "|       THANK YOU FOR BANKING WITH OASIS NATIONAL BANK OF INDIA     |" + RESET);
        System.out.println(GREEN + "+------------------------------------------------------------------+" + RESET);
        System.out.printf(GREEN + "| " + RESET + " Dhanyavaad, " + BOLD + "%-47s" + RESET + GREEN + " |%n", currentAccount.getAccountHolderName() + "!");
        System.out.println(GREEN + "| " + RESET + " Please remember to collect your RuPay debit card & advice slip. " + GREEN + " |" + RESET);
        System.out.println(GREEN + "| " + RESET + " 24x7 Helpline: 1800-425-3800 | Cyber Fraud Reporting: 1930     " + GREEN + " |" + RESET);
        System.out.println(GREEN + "+------------------------------------------------------------------+" + RESET);

        currentAccount = null;
    }

    public void printReceipt(Transaction tx) {
        if (tx == null) return;

        System.out.println("\n" + BRIGHT_WHITE + "  [Dispensing thermal receipt advice slip...]" + RESET);
        simulateProcessing("Printing customer receipt...", 500);

        System.out.println(WHITE + "  +--------------------------------------------+");
        System.out.println("  |        OASIS NATIONAL BANK OF INDIA        |");
        System.out.println("  |      (A Govt. of India Undertaking)        |");
        System.out.println("  |          ATM TRANSACTION ADVICE            |");
        System.out.println("  +--------------------------------------------+");
        System.out.printf("  | Terminal ID  : %-27s |%n", terminalId);
        System.out.printf("  | Date & Time  : %-27s |%n", tx.getFormattedTimestamp());
        System.out.printf("  | Account No.  : %-27s |%n", currentAccount.getMaskedAccountNumber());
        System.out.printf("  | Cardholder   : %-27s |%n", currentAccount.getAccountHolderName());
        System.out.printf("  | IFSC Code    : %-27s |%n", currentAccount.getIfscCode());
        System.out.printf("  | UTR / Ref No : %-27s |%n", tx.getTransactionId());
        System.out.println("  +--------------------------------------------+");
        System.out.printf("  | Txn Type     : %-27s |%n", tx.getType().getDisplayName().toUpperCase());
        System.out.printf("  | Amount       : Rs. %-22s |%n", String.format("%,.2f", tx.getAmount()));
        System.out.printf("  | Available Bal: Rs. %-22s |%n", String.format("%,.2f", tx.getBalanceAfter()));
        System.out.println("  | Txn Status   : SUCCESS / APPROVED (00)     |");
        System.out.println("  | Remarks      : " + truncate(tx.getRemarks(), 27) + " |");
        System.out.println("  +--------------------------------------------+");
        System.out.println("  | RuPay / NPCI / RBI Regulated Banking       |");
        System.out.println("  +--------------------------------------------+" + RESET);
    }

    private void askForReceipt(Transaction tx) {
        System.out.print("\n" + BRIGHT_YELLOW + "Would you like to print a paper receipt? (Y/N): " + RESET);
        String resp = safeReadLine();
        if (resp.equalsIgnoreCase("y") || resp.equalsIgnoreCase("yes")) {
            printReceipt(tx);
        }
    }

    private void dispenseIndianCashNotes(double totalAmount) {
        int amount = (int) totalAmount;
        int fiveHundreds = amount / 500;
        amount %= 500;
        int twoHundreds = amount / 200;
        amount %= 200;
        int hundreds = amount / 100;

        System.out.println("\n" + CYAN + "  [Cash Dispenser Note Breakdown]:" + RESET);
        if (fiveHundreds > 0) System.out.println(GREEN + "   ▸ Rs. 500 notes : " + fiveHundreds + RESET);
        if (twoHundreds > 0)  System.out.println(GREEN + "   ▸ Rs. 200 notes : " + twoHundreds + RESET);
        if (hundreds > 0)     System.out.println(GREEN + "   ▸ Rs. 100 notes : " + hundreds + RESET);
        System.out.println(YELLOW + "  * * * Chaching! Cash shutter opened. Please collect cash. * * *" + RESET);
    }

    private void displayDemoAccounts() {
        System.out.println("\n" + CYAN + "+--------------------------------------------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|                         ACTIVE CUSTOMER DEMO ACCOUNTS                                |" + RESET);
        System.out.println(CYAN + "+----------+------+------------------------+------------------+-------------+----------+" + RESET);
        System.out.println(CYAN + "| Cust ID  | PIN  | Customer Name          | Account Number   | IFSC        | Balance  |" + RESET);
        System.out.println(CYAN + "+----------+------+------------------------+------------------+-------------+----------+" + RESET);

        for (Account acc : bank.getAllAccounts().values()) {
            System.out.printf(CYAN + "|" + RESET + " %-8s " + CYAN + "|" + RESET + " %-4s " + CYAN + "|" + RESET + " %-22s " + CYAN + "|" + RESET + " %-16s " + CYAN + "|" + RESET + " %-11s " + CYAN + "|" + GREEN + " Rs. %-8s " + CYAN + "|%n" + RESET,
                    acc.getUserId(),
                    "****",
                    acc.getAccountHolderName(),
                    acc.getMaskedAccountNumber(),
                    acc.getIfscCode(),
                    String.format("%,.0f", acc.getBalance()));
        }
        System.out.println(CYAN + "+----------+------+------------------------+------------------+-------------+----------+" + RESET);
        System.out.println(YELLOW + "  Demo PINs: Rajesh (1234), Priya (4321), Amit (9999), Ananya (1111)" + RESET);
    }

    private void printWelcomeBanner() {
        System.out.println("\n" + CYAN + BOLD);
        System.out.println("  ================================================================");
        System.out.println("                 OASIS NATIONAL BANK OF INDIA                     ");
        System.out.println("                 (A Govt. of India Undertaking)                   ");
        System.out.println("  ================================================================" + RESET);
        System.out.println(BRIGHT_WHITE + "       NATIONAL 24x7 ATM & CASH RECYCLER TERMINAL NETWORK" + RESET);
        System.out.println(CYAN + "       Regulated by Reserve Bank of India (RBI) | NPCI RuPay" + RESET);
        System.out.println(CYAN + "  ================================================================" + RESET);
    }

    private void printShutdownBanner() {
        System.out.println("\n" + CYAN + "==================================================================" + RESET);
        System.out.println(BOLD + " ATM Session safely terminated. Audit logs saved. Shutter closed." + RESET);
        System.out.println(CYAN + "==================================================================" + RESET);
    }

    private void printSubHeader(String title) {
        System.out.println("\n" + CYAN + "---------- [ " + BOLD + BRIGHT_WHITE + title + RESET + CYAN + " ] ----------" + RESET);
    }

    private void printNotification(String colorStyle, String title, String body) {
        System.out.println("\n" + colorStyle + " +-- [ " + title + " ] " + "-".repeat(Math.max(2, 50 - title.length())) + RESET);
        for (String line : body.split("\n")) {
            System.out.println(colorStyle + " | " + RESET + line);
        }
        System.out.println(colorStyle + " +" + "-".repeat(56) + RESET);
    }

    private void printDivider() {
        System.out.println(CYAN + "------------------------------------------------------------------" + RESET);
    }

    private void pauseForUser() {
        if (!scanner.hasNextLine()) return;
        System.out.print("\n" + WHITE + "Press [Enter] to return to Main Services Menu..." + RESET);
        scanner.nextLine();
    }

    private String safeReadLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return "";
    }

    private int readIntegerInput(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine();
            if (line.isEmpty() && !scanner.hasNextLine()) {
                return min;
            }
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println(RED + "  Invalid choice. Please enter a number between " + min + " and " + max + "." + RESET);
            } catch (NumberFormatException e) {
                System.out.println(RED + "  Invalid input format. Please enter a valid number." + RESET);
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = safeReadLine();
            if (line.isEmpty() && !scanner.hasNextLine()) {
                return 0.0;
            }
            try {
                double value = Double.parseDouble(line);
                return value;
            } catch (NumberFormatException e) {
                System.out.println(RED + "  Invalid amount. Please enter a valid numeric amount in Rupees." + RESET);
            }
        }
    }

    private void simulateProcessing(String message, int milliseconds) {
        System.out.print(BRIGHT_YELLOW + "  [*] " + message + RESET);
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        System.out.println(" " + GREEN + "[SUCCESS]" + RESET);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
