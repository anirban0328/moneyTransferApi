Money Transfer REST API

A RESTful API that allows transfer money from one Bank Account to another. It also allows creation of new account details.

This API guarantees the data consistency in all cases even if there are large number of concurrent users. It has been achieved by using lock during transaction and using HashTable to store in-memory data.

The API is written in Java 8 and uses Undertow's embedded REST server.

Libraries used
1. Jackson Core 2.2.3
2. Jackson Databind 2.9.9.3
3. Undertow Core 2.0.23
4. Jetty 9.4.7
5. JUnit 4

Features
1. Create an account.
2. Deposit money to an account
3. Withdraw money from an account.
4. Transfer money between accounts.
5. Store all transactions and accounts in-memory
6. Error capturing and returning appropriate HTTP response codes.

Requires
1. Java 8
2. Maven

