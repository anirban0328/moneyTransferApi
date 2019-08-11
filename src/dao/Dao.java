package dao;

import account.Account;
import transaction.Transaction;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

public final class Dao {
    private static Hashtable<Integer, Account> accounts = new Hashtable<>();
    private static Hashtable<Integer, Transaction> transactions = new Hashtable<>();

    public static boolean checkIfAccountExists(int accountId) {
        return accounts.containsKey(accountId);
    }

    public static boolean checkIfTransactionExists(int transactionId) {
        return transactions.containsKey(transactionId);
    }

    public static void addAccount(Account account) {
        accounts.put(account.getId(), account);
    }

    public static Account getAccount(int accId) {
        return accounts.get(accId);
    }
    
    public static List<Account> getAccounts() {
        return new LinkedList<>(accounts.values());
    }

    public static void addTransaction(Transaction transaction) {
        transactions.put(transaction.getId(), transaction);
    }

    public static Transaction getTransaction(int transId) {
        return transactions.get(transId);
    }

    public static List<Transaction> getTransactions() {
        return new LinkedList<>(transactions.values());
    }  
}
