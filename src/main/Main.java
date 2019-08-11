package main;

import account.Account;
import account.AccountHandler;
import transaction.Transaction;
import transaction.TransactionHandler;
import transfer.TransferHandler;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;

public class Main {
	private static String HOSTNAME = "localhost";
    private static int PORT = 8080;
    private static Undertow server;
    
    private static final HttpHandler Endpoints = new RoutingHandler()
            .get("/", Main::defaultHandler)
            .get("/accounts", AccountHandler::getAccountsHandler)
            .get("/account/{accId}", AccountHandler::getAccountHandler)
            .get("/account/create", AccountHandler::accountCreateHandler)   
            .post("/account/deposit", AccountHandler::accountDepositHandler)
            .post("/account/withdraw", AccountHandler::accountWithdrawalHandler)
            .post("/transfer", TransferHandler::transferHandler)
            .get("/transactions", TransactionHandler::getTransactionsHandler);
    
    private static void defaultHandler(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Welcome! ");
    }

    public static void stop() {
        server.stop();
    }
    
	public static void main(String[] args) {
		if (args != null && args.length > 0 && args[0].equals("-testing")) {
            Account.createDemoAccounts(10);
            Transaction.createDummyTransactions();
        }
		
        try {
            server = Undertow.builder()
                    .addHttpListener(PORT, HOSTNAME)
                    .setHandler(Endpoints)
                    .build();
            server.start();
        } catch (RuntimeException e) {
            System.out.println("Error" + e);
        } finally {
            System.out.println("server is running on http://" + HOSTNAME + ":" + PORT +"/");
        }
	}
}
