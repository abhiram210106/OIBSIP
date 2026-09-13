import java.nio.charset.StandardCharsets;

/**
 * Oasis National Bank of India (ONBI)
 * ATM Interface & Banking Simulation
 *
 * Requirements Checklist Fulfillment:
 * - [x] Startup prompt for User ID and PIN; deny access after 3 incorrect attempts.
 * - [x] Main menu displayed after successful login with 5 options:
 *       1. Transaction History (stored in ArrayList)
 *       2. Withdraw (balance validation, update balance, log transaction)
 *       3. Deposit (prompt amount, update balance, log transaction)
 *       4. Transfer (recipient ID & amount, balance check, update both accounts, log transaction)
 *       5. Quit (display goodbye message and exit)
 * - [x] Balance check before any withdrawal or transfer ("Insufficient Funds")
 * - [x] All transactions stored in an ArrayList
 * - [x] 5 distinct Java classes: ATM, Account, Transaction, Bank, Main
 * - [x] Encapsulation (private fields + getters/setters)
 * - [x] switch-case for the menu system
 */
public class Main {
    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        // 1. Initialize Central Bank (Oasis National Bank of India)
        Bank bank = new Bank("Oasis National Bank of India", "ONBI", "ONBI0001089");

        // 2. Seed Indian Commercial Bank Customer Accounts
        seedDemoAccounts(bank);

        // 3. Initialize and Start ATM Terminal
        ATM atm = new ATM(bank);
        atm.start();
    }

    private static void seedDemoAccounts(Bank bank) {
        Account rajesh = new Account("1001", "1234", "Rajesh Kumar Sharma", "501004928172", "ONBI0001089", "Privilege Savings", 125000.00);
        Account priya = new Account("1002", "4321", "Priya Ramesh Patel", "501008392103", "ONBI0001089", "Classic Savings", 65500.00);
        Account amit = new Account("1003", "9999", "Amit Vikram Verma", "501001192847", "ONBI0001089", "Corporate Salary", 250000.00);
        Account ananya = new Account("1004", "1111", "Ananya Sundaram Iyer", "501006543219", "ONBI0001089", "Student Savings", 42000.00);

        bank.registerAccount(rajesh);
        bank.registerAccount(priya);
        bank.registerAccount(amit);
        bank.registerAccount(ananya);
    }
}
