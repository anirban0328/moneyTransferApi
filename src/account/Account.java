package account;

import transaction.Transaction;
import dao.Dao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private int id;
    private BigDecimal balance;
    private List<Transaction> transactions;
    private Lock balanceLock = new ReentrantLock();

    public Account() {
        this(new BigDecimal(0));
    }

    public Account(BigDecimal _balance) {
        Random rand = new Random();
        do {
            id = rand.nextInt(Integer.MAX_VALUE);
        } while (Dao.checkIfAccountExists(id));

        balance = _balance;
        transactions = new LinkedList<>();
        Dao.addAccount(this);
    }

    public synchronized void deposit(Transaction transaction) throws Exception {
        balanceLock.lock();
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            balanceLock.unlock();
            throw new Exception("Deposit amount must be greater than zero");
        }

        if (transaction.getDestAccId() != this.getId()) {
            balanceLock.unlock();
            throw new Exception("Destination Account ID doesn't match the account ID");
        }

        balance = balance.add(transaction.getAmount());
        balanceLock.unlock();
        transaction.setSuccessful(true);
        transactions.add(0, transaction);
    }

    public synchronized void withdraw(Transaction transaction) throws Exception {
        balanceLock.lock();
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            balanceLock.unlock();
            throw new Exception("Withdrawal amount must be positive");
        }
        if (balance.subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) < 0) {
            balanceLock.unlock();
            throw new Exception("Insufficient funds");
        }
        if (transaction.getSourceAccId() != this.getId()) {
            balanceLock.unlock();
            throw new Exception("Source Account ID doesn't match the account ID");
        }

        balance = balance.subtract(transaction.getAmount());
        balanceLock.unlock();
        transaction.setSuccessful(true);
        transactions.add(0, transaction);
    }

    public static void createDemoAccounts(int num) {
        for (int i=0; i < num; i++) {
            new Account(Transaction.getRandomAmount(99999));
        }
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        BigDecimal bal;
        balanceLock.lock();
        bal = this.balance;
        balanceLock.unlock();
        return bal;
    }

    public void setBalance(BigDecimal balance) {
    	balanceLock.lock();
        this.balance = balance;
        balanceLock.unlock();
    }

    public List<Transaction> getTransactions( ) {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    public void refundTransaction(Transaction transaction) {
        balanceLock.lock();
        balance = balance.add(transaction.getAmount());
        balanceLock.unlock();
        transactions.remove(transaction);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;

        Account otherAcc = (Account) o;

        return (this.getId() == otherAcc.getId()) &&
                (this.getBalance().compareTo(otherAcc.getBalance()) == 0);
    }
    
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "Error";
    }
}
