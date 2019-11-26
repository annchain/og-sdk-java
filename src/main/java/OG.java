import com.alibaba.fastjson.JSON;
import model.*;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes1;
import server.OGRequestGET;
import server.OGRequestPOST;
import server.OGServer;
import types.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

interface ConfirmCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ConfirmEvent(String result);
    void FailedEvent(String err);
}

interface ContractCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ContractEvent(List<Type> result);
    void FailedEvent(String err);
}

public class OG {

    private String node_url;
    private OGServer server;
    private final Integer DEFAULT_TOKENID = 0;

    private final Integer RECEIPT_STATUS_SUCCESS = 0;
    private final Integer RECEIPT_STATUS_OVMFAILED = 1;
    private final Integer RECEIPT_STATUS_UNKNOWNTYPE = 2;
    private final Integer RECEIPT_STATUS_FAILED = 3;

    public OG(String url) {
        this.node_url = url;
        this.server = new OGServer(url);
    }

    private SendTransactionResp SendRawTransaction(Account account, String to, Integer tokenID, BigInteger value, String data) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX tx = new TX(account, to, nonce, value, data, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post("new_transaction", req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp SendTransaction(Account account, String to, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, this.DEFAULT_TOKENID, value, "");
    }

    public SendTransactionResp SendTransactionAsync(Account account, String to, BigInteger value, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.SendTransaction(account, to, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp TransferToken(Account account, String to, Integer tokenID, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, tokenID, value, "");
    }

    public SendTransactionResp TransferTokenAsync(Account account, String to, Integer tokenID, BigInteger value, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.TransferToken(account, to, tokenID, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp InitialTokenOffering(Account account, BigInteger value, Boolean additionalIssue, String tokenName) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_InitialTokenOffering tx = new TX_InitialTokenOffering(account, nonce, value, additionalIssue, tokenName);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp InitialTokenOfferingAsync(Account account, BigInteger value, Boolean additionalIssue, String tokenName, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.InitialTokenOffering(account, value, additionalIssue, tokenName);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp OfferMoreToken(Account account, BigInteger value, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_AdditionalTokenOffering tx = new TX_AdditionalTokenOffering(account, nonce, value, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp OfferMoreTokenAsync(Account account, BigInteger value, Integer tokenID, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.OfferMoreToken(account, value, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp DestroyToken(Account account, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_DestroyToken tx = new TX_DestroyToken(account, nonce, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp DestroyTokenAsync(Account account, Integer tokenID, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DestroyToken(account, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp DeployContract(Account account, BigInteger value, String bytecode, List<Type> constructorParameters) throws IOException {
        String data = bytecode + FunctionEncoder.encodeConstructor(constructorParameters);
        return this.SendRawTransaction(account, "", this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp DeployContractAsync(Account account, BigInteger value, String bytecode, List<Type> constructorParameters, ConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DeployContract(account, value, bytecode, constructorParameters);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp CallContract(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<Class<Type>> outputTypes) throws IOException {
        List<TypeReference<?>> outputParams = new ArrayList<>();
        for ( Class<Type> cls: outputTypes) {
            outputParams.add(TypeReference.create(cls));
        }
        Function func = new Function(funcName, inputs, outputParams);
        String data = FunctionEncoder.encode(func);
        return this.SendRawTransaction(account, contractAddress, this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp CallContractAsync(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<Class<Type>> outputTypes, ContractCallback callback) throws IOException, InterruptedException {
        List<TypeReference<?>> outputFuncParams = new ArrayList<>();
        List<TypeReference<Type>> outputParams = new ArrayList<>();
        for ( Class<Type> cls: outputTypes) {
            outputFuncParams.add(TypeReference.create(cls));
            outputParams.add(TypeReference.create(cls));
        }
        Function func = new Function(funcName, inputs, outputFuncParams);
        String data = FunctionEncoder.encode(func);

        SendTransactionResp resp = this.SendRawTransaction(account, contractAddress, this.DEFAULT_TOKENID, value, data);
        checkContractCall(resp, outputParams, callback);
        return resp;
    }

    public QueryContractResp QueryContract(String contractAddress, String funcName, List<Type> inputs, List<Class<Type>> outputTypes) throws IOException {
        List<TypeReference<?>> outputFuncParams = new ArrayList<>();
        List<TypeReference<Type>> outputParams = new ArrayList<>();
        for ( Class<Type> cls: outputTypes) {
            outputFuncParams.add(TypeReference.create(cls));
        }
        Function func = new Function(funcName, inputs, outputFuncParams);
        String data = FunctionEncoder.encode(func);

        OGRequestPOST req = new OGRequestPOST();
        req.SetVariable("address", contractAddress);
        req.SetVariable("data", data);

        String resp = this.server.Post("query_contract", req);
        CallContractResp contractResp = JSON.parseObject(resp, CallContractResp.class);

        QueryContractResp queryContractResp = new QueryContractResp();
        queryContractResp.setErr(contractResp.getErr());
        if (contractResp.getErr().equals("")) {
            queryContractResp.setOutputs(FunctionReturnDecoder.decode(contractResp.getData(), outputParams));
        }
        return queryContractResp;
    }

    public QueryTokenResp QueryToken(Integer tokenID) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("id", tokenID.toString());

        String resp = this.server.Get("token", req);
        return JSON.parseObject(resp, QueryTokenResp.class);
    }

    public QueryReceiptResp QueryReceipt(String hash) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("hash", hash);

        String resp = this.server.Get("query_receipt", req);
        return JSON.parseObject(resp, QueryReceiptResp.class);
    }

    public QueryBalanceResp QueryBalance(String address, Integer tokenID) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        req.SetVariable("token_id", tokenID.toString());

        String resp = this.server.Get("query_balance", req);
        return JSON.parseObject(resp, QueryBalanceResp.class);
    }

    public QueryNonceResp QueryNonce(String address) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        String resp = server.Get("query_nonce", req);
        return JSON.parseObject(resp, QueryNonceResp.class);
    }

    public QueryTransactionResp QueryTransaction(String hash) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("hash", hash);
        String resp = server.Get("transaction", req);
        return JSON.parseObject(resp, QueryTransactionResp.class);
    }

    public QueryTransactionListResp QueryTransactionsByHeight(Long height) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("height", height.toString());
        String resp = server.Get("transactions", req);
        return JSON.parseObject(resp, QueryTransactionListResp.class);
    }

    private void checkTransactionConfirmed(SendTransactionResp txResp, ConfirmCallback callback) throws IOException, InterruptedException {
        if (!txResp.getErr().equals("")) {
            callback.FailedEvent("tx error: "+ txResp.getErr());
            return;
        }
        QueryReceiptResp receipt = checkHelper(txResp, callback.IntervalTime(), callback.Timeout());
        if (receipt == null) {
            callback.FailedEvent("confirm timeout");
            return;
        }
        String result = receipt.getData().getResult();
        if (!receipt.getData().getStatus().equals(RECEIPT_STATUS_SUCCESS)) {
            callback.FailedEvent(result);
            return;
        }
        callback.ConfirmEvent(result);
    }

    private void checkContractCall(SendTransactionResp txResp, List<TypeReference<Type>> outputParams, ContractCallback callback) throws IOException, InterruptedException  {
        if (!txResp.getErr().equals("")) {
            callback.FailedEvent("tx error: "+ txResp.getErr());
            return;
        }
        QueryReceiptResp receipt = checkHelper(txResp, callback.IntervalTime(), callback.Timeout());
        if (receipt == null) {
            callback.FailedEvent("confirm timeout");
            return;
        }
        String result = receipt.getData().getResult();
        if (!receipt.getData().getStatus().equals(RECEIPT_STATUS_SUCCESS)) {
            callback.FailedEvent(result);
            return;
        }
        List<Type> contractResults = FunctionReturnDecoder.decode(result, outputParams);
        callback.ContractEvent(contractResults);
    }

    private QueryReceiptResp checkHelper(SendTransactionResp txResp, Integer callbackInterval, Integer callbackTimeout) throws IOException, InterruptedException {
        String hash = txResp.getHash();
        Long timeout = System.currentTimeMillis() + callbackTimeout;
        while (System.currentTimeMillis() < timeout) {
            QueryReceiptResp resp = this.QueryReceipt(hash);
            if (resp.getData() == null) {
                Thread.sleep(callbackInterval);
                continue;
            }
            return resp;
        }
        return null;
    }

    public static void main(String args[]) throws IOException {

        Address addr = new Address("cb88e47e1426149c4354474339b2e2ee13143ca3");
        byte[] b1 = { 0x32 };
        BytesType b = new Bytes1(b1);

        List<Type> parameters = new ArrayList<>();
        parameters.add(addr);
        parameters.add(b);

        String s = FunctionEncoder.encodeConstructor(parameters);
        System.out.println("encoded: " + s);

        List<TypeReference<?>> outputs = new ArrayList<>();
        outputs.add(TypeReference.create(Address.class));

        Function func = new Function("abcd", parameters, outputs);

//        FunctionReturnDecoder.

//        String url = "http://localhost:8000";
//        OG og = new OG(url);

        // publish a token.
//        Account account = new Account("0000000000000000000000000000000000000000000000000000000000000001");
//        System.out.println(account.GetAddress());

//        String hash = og.InitialTokenOffering(account, new BigInteger("10000"), true, "uni");
//
//        System.out.println("account private key: " + Hex.toHexString(account.GetPrivateKey()));
//        System.out.println("pub key: " + Hex.toHexString(account.GetPublicKey()));
//        System.out.println(hash);

//        // test query receipt
//        try {
//            TimeUnit.SECONDS.sleep(5);
//        } catch (Exception e) {
//            // do nothing.
//        }
//        String receiptJsonStr = og.QueryReceipt(hash);
//        System.out.println("receipt: " + receiptJsonStr);
//
//        JSONObject receiptJson = JSONObject.parseObject(receiptJsonStr);

        // test query balance
//        QueryBalanceResp balance = og.QueryBalance("c6b6e9949e1e4a4c4df8b80a9a07c95008b563ea", 0);
//        System.out.println("balance is: " + balance.getData().getBalance());
//
//        balance = og.QueryBalance("0xcb88e47e1426149c4354474339b2e2ee13143ca3", 0);
//        System.out.println("balance is: " + balance.toString());

        // test query token
//        String tokenInfo = og.QueryToken(1);
//        System.out.println(tokenInfo);

        // test offer more token

//        String offerMoreTokenStr = og.OfferMoreToken(account, new BigInteger("10000"), 1);
//        System.out.println("offer more token tx hash: " + offerMoreTokenStr);

        // test token destroy

//        String destroyToken = og.DestroyToken(account, 1);
//        System.out.println("offer destroy token tx hash: " + destroyToken);

        // test token transfer

//        Account toAccount = new Account();
//        String transferToken = og.TransferToken(account, toAccount.GetAddress(), 0, new BigInteger("100"));
//
//        System.out.println("transfer token tx hash: " + transferToken);

    }

}
