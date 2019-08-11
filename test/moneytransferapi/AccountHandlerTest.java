package moneytransferapi;

import main.Main;
import account.Account;
import dao.Dao;
import utils.Utils;

import io.undertow.util.Headers;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class AccountHandlerTest {

    private HttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        Main.main(new String[]{"-testing"});
        httpClient = new HttpClient();
        httpClient.start();
    }

    @After
    public void tearDown() throws Exception {
        Main.stop();
        httpClient.stop();
    }


    @Test
    public void accountCreateHandler() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Assert.assertEquals("201", response.getHeaders().get(Headers.STATUS_STRING));
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());

        Assert.assertTrue(createdAccount.getId() > 0);
        Assert.assertEquals(new BigDecimal("0"), createdAccount.getBalance());
        Assert.assertTrue(createdAccount.getTransactions().size() == 0);
    }

    @Test
    public void getAccountHandler() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Assert.assertEquals("201", response.getHeaders().get(Headers.STATUS_STRING));
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());

        response = httpClient.GET("http://localhost:8080/account/accId="+createdAccount.getId());
        Assert.assertEquals("200", response.getHeaders().get(Headers.STATUS_STRING));
        Account fetchedAccount = Utils.fromJsonToAccount(response.getContentAsString());

        Assert.assertEquals(fetchedAccount, createdAccount);
    }

    @Test
    public void getAccountsHandler() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/accounts");
        Assert.assertEquals("200", response.getHeaders().get(Headers.STATUS_STRING));

        List<Account> accountsList = Utils.fromJsonToAccountList(response.getContentAsString());
        Assert.assertTrue(accountsList.size() > 0);
    }

    @Test
    public void getAccountHandler_badAccount() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/");
        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Error: Please enter a valid account number", response.getContentAsString());

        Random rand = new Random();
        int id;
        do {
            id = rand.nextInt(Integer.MAX_VALUE);
        } while (Dao.checkIfAccountExists(id));

        response = httpClient.GET("http://localhost:8080/account/"+id);
        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Error: Please enter a valid account number", response.getContentAsString());
    }


    @Test
    public void accountDepositHandler() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());

        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");

        String request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 150\n" +
                "}";

        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();
        Assert.assertEquals("202", response.getHeaders().get(Headers.STATUS_STRING));

        Account returnedAccount = Utils.fromJsonToAccount(response.getContentAsString());

        String expectedResponse = "{\r\n" +
                "  \"id\" : " + createdAccount.getId() + ",\r\n" +
                "  \"balance\" : 150,\r\n" +
                "  \"transactions\" : [ {\r\n" +
                "    \"id\" : " + returnedAccount.getTransactions().get(0).getId() + ",\r\n" +
                "    \"sourceAccId\" : -1,\r\n" +
                "    \"destAccId\" : " + createdAccount.getId() + ",\r\n" +
                "    \"amount\" : 150,\r\n" +
                "    \"successful\" : true\r\n" +
                "  } ]\r\n" +
                "}";

        Assert.assertEquals(expectedResponse.replace("\n", "").replace("\r", ""),
                response.getContentAsString().replace("\n", "").replace("\r", ""));
    }

    @Test
    public void accountDepositHandler_badAccId() throws Exception {
        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");

        String request = "{\n" +
                "\"destAccId\": 6754,\n" +
                "\"amount\": 150\n" +
                "}";

        httpRequest.content(new StringContentProvider(request), "application/json");
        ContentResponse response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Error: Account doesn't exist", response.getContentAsString());
    }

    @Test
    public void accountDepositHandler_badDeposit() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());

        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");

        String request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 0\n" +
                "}";

        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid transaction: Deposit amount must be positive", response.getContentAsString());



        request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": -5\n" +
                "}";

        httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid transaction: Deposit amount must be positive", response.getContentAsString());
    }

    @Test
    public void accountDepositHandler_badRequest() throws Exception {
        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");

        String request = "";

        httpRequest.content(new StringContentProvider(request), "application/json");
        ContentResponse response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid request: Please send destAccId and amount", response.getContentAsString());
    }



    @Test
    public void accountWithdrawalHandler() throws Exception {
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());
        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        String request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 150\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        httpRequest.send();


        httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        request = "{\n" +
                "\"sourceAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 15\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Account returnedAccount = Utils.fromJsonToAccount(response.getContentAsString());

        String expectedResponse = "{\r\n" +
                "  \"id\" : " + createdAccount.getId() + ",\r\n" +
                "  \"balance\" : 135,\r\n" +
                "  \"transactions\" : [ {\r\n" +
                "    \"id\" : " + returnedAccount.getTransactions().get(0).getId() + ",\r\n" +
                "    \"sourceAccId\" : "+ createdAccount.getId() +",\r\n" +
                "    \"destAccId\" : -1,\r\n" +
                "    \"amount\" : 15,\r\n" +
                "    \"successful\" : true\r\n" +
                "  }, {\r\n" +
                "    \"id\" : " + returnedAccount.getTransactions().get(1).getId() + ",\r\n" +
                "    \"sourceAccId\" : -1,\r\n" +
                "    \"destAccId\" : " + createdAccount.getId() + ",\r\n" +
                "    \"amount\" : 150,\r\n" +
                "    \"successful\" : true\r\n" +
                "  } ]\r\n" +
                "}";
        Assert.assertEquals(expectedResponse.replace("\n", "").replace("\r", ""),
                response.getContentAsString().replace("\n", "").replace("\r", ""));
    }

    @Test
    public void accountWithdrawalHandler_badAccId() throws Exception {
        Request httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");

        String request = "{\n" +
                "\"sourceAccId\": 6754,\n" +
                "\"amount\": 150\n" +
                "}";

        httpRequest.content(new StringContentProvider(request), "application/json");
        ContentResponse response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Error: Account doesn't exist", response.getContentAsString());
    }



    @Test
    public void accountWithdrawalHandler_badWithdrawal() throws Exception {
        //create an account
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());
        //deposit 150 in the account
        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        String request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 150\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        httpRequest.send();

        //withdraw 0
        httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        request = "{\n" +
                "\"sourceAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 0\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid transaction: Withdrawal amount must be positive", response.getContentAsString());



        //withdraw -59
        httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        request = "{\n" +
                "\"sourceAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": -59\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid transaction: Withdrawal amount must be positive", response.getContentAsString());


        //withdraw 151
        httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        request = "{\n" +
                "\"sourceAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 151\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Insufficient funds", response.getContentAsString());
    }



    @Test
    public void accountWithdrawalHandler_badRequest() throws Exception {
        //create an account
        ContentResponse response = httpClient.GET("http://localhost:8080/account/create");
        Account createdAccount = Utils.fromJsonToAccount(response.getContentAsString());
        //deposit 150 in the account
        Request httpRequest = httpClient.POST("http://localhost:8080/account/deposit");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        String request = "{\n" +
                "\"destAccId\": " + createdAccount.getId() + ",\n" +
                "\"amount\": 150\n" +
                "}";
        httpRequest.content(new StringContentProvider(request), "application/json");
        httpRequest.send();

        //withdraw with no string in request
        httpRequest = httpClient.POST("http://localhost:8080/account/withdraw");
        httpRequest.header(HttpHeader.ACCEPT, "application/json");
        httpRequest.header(HttpHeader.CONTENT_TYPE, "application/json");
        request = "";
        httpRequest.content(new StringContentProvider(request), "application/json");
        response = httpRequest.send();

        Assert.assertEquals("400", response.getHeaders().get(Headers.STATUS_STRING));
        Assert.assertEquals("Invalid request: Please send sourceAccId and amount", response.getContentAsString());
    }
}