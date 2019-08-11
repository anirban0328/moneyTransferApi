package transaction;

import dao.Dao;
import utils.Utils;
import io.undertow.server.HttpServerExchange;
import java.util.List;

public class TransactionHandler {

    public static void getTransactionsHandler(HttpServerExchange exchange) {
        List<Transaction> transactions = Dao.getTransactions();
        Utils.createResponse(exchange, Utils.contentType, 200, Utils.TransactionlistToJson(transactions));
    }
}
