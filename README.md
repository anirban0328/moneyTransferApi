# Money Transfer REST API

A RESTful API that allows transfer of money from one Bank Account to another. It allows creation of new account details and also supports withdraw and deposit of money into individual account.

Authentication is not supported nor currency conversion is supported. Exception Handling is taken care for all cases and will throw HTTP 400 Bad Request error whenever invalid details are provided. 

This API guarantees the data consistency in all cases even if there are large number of concurrent users. It has been achieved by using lock during transaction and using HashTable to store in-memory data.

The API is written in Java 8 and uses Undertow's embedded REST server.

## Libraries used
1. Jackson Core 2.2.3
2. Jackson Databind 2.9.9.3
3. Undertow Core 2.0.23
4. Jetty 9.4.7
5. JUnit 4

## Features
1. Create an account.
2. Deposit money to an account
3. Withdraw money from an account.
4. Transfer money between accounts.
5. Store all transactions and accounts in-memory.
6. Error capturing and returning appropriate HTTP response codes.

## Requires
1. Java 8
2. Maven

## Start Application
1. Clone the repo and run the command 
   ```mvn package```
   Ensure that maven is installed in the system and JAVA_HOME is set.
   It will create jar file 
   ```MoneyTransferApi-0.0.1-SNAPSHOT-jar-with-dependencies.jar```
   Execute using command 
   ```java -jar MoneyTransferApi-0.0.1-SNAPSHOT-jar-with-dependencies.jar```
2. Another way to execute is to run directly 
   ```java -jar target/MoneyTransferApi-0.0.1-SNAPSHOT-jar-with-dependencies.jar```

## API
### Account

Creating an account
```
GET request: http://localhost:8080/account/create. 
The server will respond with information about the created account in a JSON format:
{
  "id" : 1765465769,
  "balance" : 0,
  "transactions" : []
}
```

Get an account
```
GET request: http://localhost:8080/account/accId=184748950. 
The server will respond with information about the created account in a JSON format:
{
  "id" : 1875465768,
  "balance" : 0,
  "transactions" : []
}
```

Get all accounts
```
GET request: http://localhost:8080/accounts
Example response:
{
"accounts" : [
{
  "id" : 1765489786,
  "balance" : 70000,
  "transactions" : [ {
    "id" : 4675378567,
    "sourceAccId" : 1543785647,
    "destAccId" : 1453975645,
    "amount" : 6000,
    "successful" : true
  }, {
    "id" : 1874356783,
    "sourceAccId" : 1874532678,
    "destAccId" : 1547346593,
    "amount" : 45678,
    "successful" : true
  }]
 ```
 
### Transaction

Deposit money
```
POST request: http://localhost:8080/account/deposit 
{
  "destAccId" : 18453647562,
  "amount" : 1000
}
```

Withdraw money
```
POST request: http://localhost:8080/account/withdraw 
{
  "sourceAccId" : 18956478697,
  "amount" : 650.30
}
```

Transfer money
```
POST request: http://localhost:8080/account/transfer 
{
  "sourceAccId" : 1876453276,
  "destAccId" : 19856473567,
  "amount" : 450.20
}
```

Get all transactions
```
GET request: http://localhost:8080/transactions
```

Exception Handing
```
If there are any error or exception, then HTTP 400 bad request error will be thrown with error message.  
```
