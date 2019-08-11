package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import account.Account;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import transaction.Transaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Utils {
		public static String contentType = "application/json";
		public static String invalidAccNumber = "Error: Please enter a valid account number";
		public static String invalidDestinationAccId = "Invalid request: destAccId and amount is invalid";
		public static String invalidSourceAccId = "Invalid request: sourceAccId and amount is invalid";
		public static String accountNotExist = "Error: Account doesn't exist";
		public static String destOrSrcAccountNotExist = "Error: Destination/Source Account doesn't exist";
		public static String invalidDetails = "Invalid request: sourceAccId, destAccId and amount is invalid";
		
	    public static Account fromJsonToAccount(String jsonAcc) throws Exception {
	        ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(jsonAcc, Account.class);
	    }

	    public static List<Account> fromJsonToAccountList(String jsonString) throws IOException {
	        jsonString = jsonString.substring(16, jsonString.length()-1);
	        ObjectMapper objectMapper = new ObjectMapper();
	        return Arrays.asList(objectMapper.readValue(jsonString, Account[].class));
	    }

	    public static String AccountListToJson(List<Account> accounts) {
	        StringBuilder sb = new StringBuilder();
	        sb.append("{\r\n\"accounts\" : [\r\n");
	        for (Account acc : accounts) {
	            sb.append(acc.toJson());
	            sb.append(",\r\n");
	        }
	        sb.replace(sb.length()-3, sb.length(), "");
	        sb.append("\r\n]\r\n}");
	        return sb.toString();
	    }    
	    
	    public static Transaction fromJsonToTransaction(String jsonTrans) throws Exception {
	        ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(jsonTrans, Transaction.class);
	    }
	    
	    public static List<Transaction> fromJsonToTransactionList(String jsonString) throws IOException {
	        jsonString = jsonString.substring(20, jsonString.length()-1);
	        ObjectMapper objectMapper = new ObjectMapper();
	        return Arrays.asList(objectMapper.readValue(jsonString, Transaction[].class));
	    }

	    public static String TransactionlistToJson(List<Transaction> transactions) {
	        StringBuilder sb = new StringBuilder();
	        sb.append("{\r\n\"transactions\" : [\r\n");
	        for (Transaction trans : transactions) {
	            sb.append(trans.toJson());
	            sb.append(",\r\n");
	        }
	        sb.replace(sb.length()-3, sb.length(), "");
	        sb.append("\r\n]\r\n}");
	        return sb.toString();
	    }
	    
	    public static void createResponse(HttpServerExchange exchange, String contentType,
	    		int status, String message) {
	        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
	        exchange.getResponseHeaders().put(Headers.STATUS, status);
	        exchange.getResponseSender().send(message);
	    }
}
