package transfer;

import transaction.Transaction;
import dao.Dao;
import utils.Utils;
import io.undertow.server.HttpServerExchange;

public class TransferHandler {

    public static void transferHandler(HttpServerExchange exchange) {
        exchange.getRequestReceiver().receiveFullString(((httpServerExchange, s) -> {
            Transaction transaction;
            try {
                transaction = Utils.fromJsonToTransaction(s);
            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.invalidDetails);
                return;
            }

            if (!Dao.checkIfAccountExists(transaction.getDestAccId()) ||
                    !Dao.checkIfAccountExists(transaction.getSourceAccId())) {
                Utils.createResponse(exchange, Utils.contentType, 400, Utils.destOrSrcAccountNotExist);
                return;
            }

            try {
                transaction.execute();
                String response = "{\r\n" +
                        "sourceAcc: " + Dao.getAccount(transaction.getSourceAccId()).toJson() + ",\r\n" +
                        "destAcc: " + Dao.getAccount(transaction.getDestAccId()).toJson() + "\r\n" +
                        "}";
                Utils.createResponse(exchange, Utils.contentType, 202, response);

            } catch (Exception e) {
                Utils.createResponse(exchange, Utils.contentType, 400, e.getMessage());
            }
        }));
    }

}

