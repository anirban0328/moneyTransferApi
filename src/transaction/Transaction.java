package transaction;

import account.Account;
import dao.Dao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Random;
import java.math.BigDecimal;

public class Transaction {
    private int id;
    private int sourceAccId;
    private int destAccId;
    private BigDecimal amount;
    private boolean successful = false;

    public Transaction() {
        this(0, 0, new BigDecimal("0"));
    }

    public Transaction(int sourceAccount, int destAccount, BigDecimal amount) {
        Random rand = new Random();
        do {
            id = rand.nextInt(Integer.MAX_VALUE);
        } while (Dao.checkIfTransactionExists(id));

        this.sourceAccId = sourceAccount;
        this.destAccId = destAccount;
        this.amount = amount;

        Dao.addTransaction(this);
    }

    public void execute() throws Exception {
        if (this.amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new Exception("Transfer amount must be greater than zero");

        Account sourceAcc = Dao.getAccount(sourceAccId);
        Account destAcc = Dao.getAccount(destAccId);

        sourceAcc.withdraw(this);
        try {
            destAcc.deposit(this);
        }
        catch (Exception e) {
            //if can't deposit money then refund
            sourceAcc.refundTransaction(this);
            throw e;
        }
        successful = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSourceAccId() {
        return sourceAccId;
    }

    public int getDestAccId() {
        return destAccId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean status) {
        successful = status;
    }

    public static void createDummyTransactions() {
        List<Account> accounts = Dao.getAccounts();

        // deposit money in alternate accounts
        for (int i=0; i < accounts.size(); i+=2 ) {
            try {
                accounts.get(i).deposit(
                        new Transaction(-1, accounts.get(i).getId(), getRandomAmount(99999)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //transfer money between accounts
        for (int i=1; i < accounts.size(); i+=2 ) {
            Account sourceAcc = accounts.get(i-1);
            Account destAcc = accounts.get(i);

            Transaction transaction = new Transaction(sourceAcc.getId(), destAcc.getId(),
                    getRandomAmount(sourceAcc.getBalance().intValue()));
            try {
                transaction.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static BigDecimal getRandomAmount(int max) {
        Random rand = new Random();
        String stringBal = rand.nextInt(max) + "." + rand.nextInt(99);
        return new BigDecimal(stringBal);
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
