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

interface IContractCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ContractEvent(ContractCallbackResp result);
}

public class OG {

    private String node_url;
    private OGServer server;
    public final Integer DEFAULT_TOKENID = 0;
    public final String EMPTY_ADDRESS = "0000000000000000000000000000000000000000";
    public final String EMPTY_DATA = "";
    public final String EMPTY_ERROR = "";

    public final Integer RECEIPT_STATUS_SUCCESS = 0;
    public final Integer RECEIPT_STATUS_OVMFAILED = 1;
    public final Integer RECEIPT_STATUS_UNKNOWNTYPE = 2;
    public final Integer RECEIPT_STATUS_FAILED = 3;

    public OG(String url) {
        this.node_url = url;
        this.server = new OGServer(url);
    }

    private SendTransactionResp SendRawTransaction(Account account, String to, Integer tokenID, BigInteger value, String data) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX tx = new TX(account, to, nonce, value, data, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post("new_transaction", req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp SendTransaction(Account account, String to, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, this.DEFAULT_TOKENID, value, EMPTY_DATA);
    }

    public SendTransactionResp SendTransactionAsync(Account account, String to, BigInteger value, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.SendTransaction(account, to, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp TransferToken(Account account, String to, Integer tokenID, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, tokenID, value, EMPTY_DATA);
    }

    public SendTransactionResp TransferTokenAsync(Account account, String to, Integer tokenID, BigInteger value, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.TransferToken(account, to, tokenID, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp InitialTokenOffering(Account account, BigInteger value, Boolean additionalIssue, String tokenName) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_InitialTokenOffering tx = new TX_InitialTokenOffering(account, nonce, value, additionalIssue, tokenName);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp InitialTokenOfferingAsync(Account account, BigInteger value, Boolean additionalIssue, String tokenName, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.InitialTokenOffering(account, value, additionalIssue, tokenName);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp OfferMoreToken(Account account, BigInteger value, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_AdditionalTokenOffering tx = new TX_AdditionalTokenOffering(account, nonce, value, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp OfferMoreTokenAsync(Account account, BigInteger value, Integer tokenID, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.OfferMoreToken(account, value, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp DestroyToken(Account account, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_DestroyToken tx = new TX_DestroyToken(account, nonce, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    public SendTransactionResp DestroyTokenAsync(Account account, Integer tokenID, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DestroyToken(account, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp DeployContract(Account account, BigInteger value, String bytecode, List<Type> constructorParameters) throws IOException {
        String data = bytecode + FunctionEncoder.encodeConstructor(constructorParameters);
        return this.SendRawTransaction(account, EMPTY_ADDRESS, this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp DeployContractAsync(Account account, BigInteger value, String bytecode, List<Type> constructorParameters, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DeployContract(account, value, bytecode, constructorParameters);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp CallContract(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs) throws IOException {
        Function func = new Function(funcName, inputs, new ArrayList<>());
        String data = FunctionEncoder.encode(func);
        return this.SendRawTransaction(account, contractAddress, this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp CallContractAsync(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams, IContractCallback callback) throws IOException, InterruptedException {
        Function func = new Function(funcName, inputs, outputParams);
        SendTransactionResp resp = this.CallContract(account, contractAddress, value, funcName, inputs);
        checkContractCall(resp, func.getOutputParameters(), callback);
        return resp;
    }

    public QueryContractResp QueryContract(String contractAddress, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams) throws IOException {
        Function func = new Function(funcName, inputs, outputParams);
        String data = FunctionEncoder.encode(func);

        OGRequestPOST req = new OGRequestPOST();
        req.SetVariable("address", contractAddress);
        req.SetVariable("data", data);

        String resp = this.server.Post("query_contract", req);
        CallContractResp contractResp = JSON.parseObject(resp, CallContractResp.class);

        QueryContractResp queryContractResp = new QueryContractResp();
        queryContractResp.setErr(contractResp.getErr());
        if (contractResp.getErr().equals(EMPTY_ERROR)) {
            queryContractResp.setOutputs(FunctionReturnDecoder.decode(contractResp.getData(), func.getOutputParameters()));
        }
        return queryContractResp;
    }

    public QueryTokenResp QueryToken(Integer tokenID) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("id", tokenID.toString());

        String resp = this.server.Get("token", req);
        return JSON.parseObject(resp, QueryTokenResp.class);
    }

    /**
     * Query receipt of a transaction.
     * @param hash 32 length byte array formatted in hex.
     * @return
     * @throws IOException
     */
    public QueryReceiptResp QueryReceipt(String hash) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("hash", hash);

        String resp = this.server.Get("query_receipt", req);
        return JSON.parseObject(resp, QueryReceiptResp.class);
    }

    /**
     * Query balance of an address.
     * @param address   20 length byte array formatted in hex.
     * @param tokenID   the id of the token.
     * @return
     * @throws IOException
     */
    public QueryBalanceResp QueryBalance(String address, Integer tokenID) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        req.SetVariable("token_id", tokenID.toString());

        String resp = this.server.Get("query_balance", req);
        return JSON.parseObject(resp, QueryBalanceResp.class);
    }

    /**
     * Query nonce of an address.
     * @param address   20 length byte array formatted in hex.
     * @return
     * @throws IOException
     */
    public QueryNonceResp QueryNonce(String address) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        String resp = server.Get("query_nonce", req);
        return JSON.parseObject(resp, QueryNonceResp.class);
    }

    /**
     * Query transaction by transaction hash.
     * @param hash  32 length byte array formatted in hex.
     * @return
     * @throws IOException
     */
    public QueryTransactionResp QueryTransaction(String hash) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("hash", hash);
        String resp = server.Get("transaction", req);
        return JSON.parseObject(resp, QueryTransactionResp.class);
    }

    /**
     * Query all transactions that confirmed a same sequencer.
     * @param height    sequencer height.
     * @return
     * @throws IOException
     */
    public QueryTransactionListResp QueryTransactionsByHeight(Long height) throws IOException {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("height", height.toString());
        String resp = server.Get("transactions", req);
        return JSON.parseObject(resp, QueryTransactionListResp.class);
    }

    private void checkTransactionConfirmed(SendTransactionResp txResp, IConfirmCallback callback) throws IOException, InterruptedException {
        ConfirmCallbackResp resp = new ConfirmCallbackResp();
        resp.setHash(txResp.getHash());
        resp.setErr(EMPTY_ERROR);

        if (!txResp.getErr().equals(EMPTY_ERROR)) {
            resp.setErr("tx error: "+ txResp.getErr());
            callback.ConfirmEvent(resp);
            return;
        }
        QueryReceiptResp receipt = checkHelper(txResp, callback.IntervalTime(), callback.Timeout());
        if (receipt == null) {
            resp.setErr("confirm timeout");
            callback.ConfirmEvent(resp);
            return;
        }
        String result = receipt.getData().getResult();
        if (!receipt.getData().getStatus().equals(RECEIPT_STATUS_SUCCESS)) {
            resp.setErr(result);
            callback.ConfirmEvent(resp);
            return;
        }
        resp.setData(result);
        callback.ConfirmEvent(resp);
    }

    private void checkContractCall(SendTransactionResp txResp, List<TypeReference<Type>> outputParams, IContractCallback callback) throws IOException, InterruptedException  {
        ContractCallbackResp resp = new ContractCallbackResp();
        resp.setHash(txResp.getHash());
        if (!txResp.getErr().equals(EMPTY_ERROR)) {
            resp.setErr("tx error: "+ txResp.getErr());
            callback.ContractEvent(resp);
            return;
        }
        QueryReceiptResp receipt = checkHelper(txResp, callback.IntervalTime(), callback.Timeout());
        if (receipt == null) {
            resp.setErr("confirm timeout");
            callback.ContractEvent(resp);
            return;
        }
        String result = receipt.getData().getResult();
        if (!receipt.getData().getStatus().equals(RECEIPT_STATUS_SUCCESS)) {
            resp.setErr(result);
            callback.ContractEvent(resp);
            return;
        }
        List<Type> contractOutputs = FunctionReturnDecoder.decode(result, outputParams);
        resp.setData(contractOutputs);
        callback.ContractEvent(resp);
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
//
//        Address addr = new Address("cb88e47e1426149c4354474339b2e2ee13143ca3");
//        byte[] b1 = { 0x32 };
//        BytesType b = new Bytes1(b1);
////
//        List<Type> parameters = new ArrayList<>();
//        parameters.add(addr);
//        parameters.add(b);
//
//        String s = FunctionEncoder.encodeConstructor(parameters);
//        System.out.println("encoded: " + s);
////
//        List<TypeReference<?>> outputs = new ArrayList<>();
//        outputs.add(TypeReference.create(Address.class));
//
//        Function func = new Function("abcd", parameters, outputs);


//        String url = "http://localhost:8000";
//        OG og = new OG(url);

        // publish a token.
        Account account = new Account("0000000000000000000000000000000000000000000000000000000000000001");
        System.out.println(account.GetAddress());

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

//        // test query balance
//        QueryBalanceResp balance = og.QueryBalance(account.GetAddress(), 0);
//        System.out.println("balance is: " + balance.getData().getBalance());
//
//        // query nonce
//        QueryNonceResp nonce = og.QueryNonce(account.GetAddress());
//        System.out.println(nonce.getNonce());

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
