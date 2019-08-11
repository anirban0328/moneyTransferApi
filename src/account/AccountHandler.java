package account;

import transaction.Transaction;
import dao.Dao;
import utils.Utils;
import io.undertow.server.HttpServerExchange;

import java.util.List;

public class AccountHandler {

    public static void accountCreateHandler(HttpServerExchange exchange) {    	
        Account newAccount = new Account();
        Utils.createResponse(exchange, Utils.contentType, 201, newAccount.toJson());
    }

    public static void getAccountHandler(HttpServerExchange exchange) {
        try {
            String stingAccId = exchange.getQueryParameters().get("accId").getFirst().split("=")[1];
            int accId = Integer.parseInt(stingAccId);

            if (!Dao.checkIfAccountExists(accId)) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.invalidAccNumber);
                return;
            }

            Account requestedAccount = Dao.getAccount(accId);
            Utils.createResponse(exchange, Utils.contentType, 200, requestedAccount.toJson());
        }
        catch (Exception e) {
            Utils.createResponse(exchange, Utils.contentType, 400, Utils.invalidAccNumber);
        }
    }

    public static void getAccountsHandler(HttpServerExchange exchange) {
        List<Account> accounts = Dao.getAccounts();
        Utils.createResponse(exchange, Utils.contentType, 200, Utils.AccountListToJson(accounts));
    }

    public static void accountDepositHandler(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString(((httpServerExchange, s) -> { 
            Transaction depositTransaction;
            try {
                depositTransaction = Utils.fromJsonToTransaction(s);
            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.invalidDestinationAccId);
                return;
            }

            if (!Dao.checkIfAccountExists(depositTransaction.getDestAccId())) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.accountNotExist);
                return;
            }

            try {
                Account account = Dao.getAccount(depositTransaction.getDestAccId());
                account.deposit(depositTransaction);
                Utils.createResponse(exchange, Utils.contentType, 202, account.toJson());

            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, e.getMessage());
            }
        }));
    }

    public static void accountWithdrawalHandler(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString(((httpServerExchange, s) -> {
            Transaction withdrawalTransaction;
            try {
                withdrawalTransaction = Utils.fromJsonToTransaction(s);
            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.invalidSourceAccId);
                return;
            }

            if (!Dao.checkIfAccountExists(withdrawalTransaction.getSourceAccId())) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.accountNotExist);
                return;
            }

            try {
                Account account = Dao.getAccount(withdrawalTransaction.getSourceAccId());
                account.withdraw(withdrawalTransaction);
                Utils.createResponse(exchange, Utils.contentType, 202, account.toJson());
            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, e.getMessage());
            }
        }));
    }
}
