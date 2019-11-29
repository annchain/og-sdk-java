# og-sdk-java
Java SDK for OG. It makes it easy to send transactions, query basic information such as balance and receipts, process with Solidity based smart contract through [Annchain.OG](https://github.com/annchain/OG).  

# Requirement
Only support Java version 8 or higher.

[Web3j](https://github.com/web3j/web3j) is required for Solidity related use cases. Add web3j into pom.xml to use it.
```xml
<dependency>
    <groupId>org.web3j</groupId>
    <artifactId>core</artifactId>
    <version>4.2.0</version>
</dependency>
```

# Usage

### Initialize account
```java
// create Account object by a random private key.
Account accountRandom = new Account();
System.out.println(accountRandom.GetAddress());

// create Account object by a specific private key.
Account accountPriv = new Account("0000000000000000000000000000000000000000000000000000000000000001");
System.out.println(accountPriv.GetAddress());
```

### Initialize a OG server
```java
String OG_URL = "http://localhost:8000";
OG og = new OG(OG_URL);
```
All the usages will relay on this `og` object.

### Query balance
```java
// query default token balance.
QueryBalanceResp balanceResp = og.QueryBalance(account.GetAddress(), 0);
if (balanceResp.getErr().equals("")) {
    System.out.println("the balance is: " + balanceResp.getData().getBalance())
}
```

### Send transaction
```java
Account account = new Account(myPrivateKey);
String to = "receiver address";
BigInteger value = new BigInteger("100");

SendTransactionResp resp = og.SendTransaction(account, to, value);
String txHash = resp.getHash();
System.out.println("tx hash is: " + txHash);
```

### Send async transaction with callback

```java
Account account = new Account(myPrivateKey);
String to = "receiver address";
BigInteger value = new BigInteger("100");

// you should implement your own confirm callback.
ConfirmCallback confirmCallback = new ConfirmCallback();

new Thread(() -> {
    try {
        og.SendTransactionAsync(account, to, value, confirmCallback);
    } catch (IOException | InterruptedException e) {
        // do something
    }
}).start();
```

**Confirm callback interface**
```java
interface IConfirmCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ConfirmEvent(ConfirmCallbackResp result);
}
```

### Deploy contract
> Solidity compiler is not supported for this SDK, so you should compile the source code and get the bytecode by yourself. [remix](https://remix.ethereum.org/) is really recommended to be your Solidity editor and compiler.

Assume you have got the bytecode:
```java
String contractBytecode = "60806040523480156100105760008...";
BigInteger value = new BigInteger("0");

SendTransactionResp deployResp = og.DeployContract(account, value, contractBytecode, null);
```


