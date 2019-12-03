package io.annchain.og.main;

import com.alibaba.fastjson.JSON;
import io.annchain.og.model.*;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import io.annchain.og.server.OGRequestGET;
import io.annchain.og.server.OGRequestPOST;
import io.annchain.og.server.OGServer;
import io.annchain.og.types.*;
import org.web3j.abi.datatypes.generated.Uint64;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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

    /**
     *
     * @param account
     * @param to
     * @param tokenID
     * @param value
     * @param data
     * @return
     * @throws IOException
     */
    private SendTransactionResp SendRawTransaction(Account account, String to, Integer tokenID, BigInteger value, String data) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX tx = new TX(account, to, nonce, value, data, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post("new_transaction", req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    /**
     *
     * @param account
     * @param to
     * @param value
     * @return
     * @throws IOException
     */
    public SendTransactionResp SendTransaction(Account account, String to, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, this.DEFAULT_TOKENID, value, EMPTY_DATA);
    }

    /**
     *
     * @param account
     * @param to
     * @param value
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp SendTransactionAsync(Account account, String to, BigInteger value, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.SendTransaction(account, to, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param to
     * @param tokenID
     * @param value
     * @return
     * @throws IOException
     */
    public SendTransactionResp TransferToken(Account account, String to, Integer tokenID, BigInteger value) throws IOException {
        return this.SendRawTransaction(account, to, tokenID, value, EMPTY_DATA);
    }

    /**
     *
     * @param account
     * @param to
     * @param tokenID
     * @param value
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp TransferTokenAsync(Account account, String to, Integer tokenID, BigInteger value, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.TransferToken(account, to, tokenID, value);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param value
     * @param additionalIssue
     * @param tokenName
     * @return
     * @throws IOException
     */
    public SendTransactionResp InitialTokenOffering(Account account, BigInteger value, Boolean additionalIssue, String tokenName) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_InitialTokenOffering tx = new TX_InitialTokenOffering(account, nonce, value, additionalIssue, tokenName);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    /**
     *
     * @param account
     * @param value
     * @param additionalIssue
     * @param tokenName
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp InitialTokenOfferingAsync(Account account, BigInteger value, Boolean additionalIssue, String tokenName, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.InitialTokenOffering(account, value, additionalIssue, tokenName);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param value
     * @param tokenID
     * @return
     * @throws IOException
     */
    public SendTransactionResp OfferMoreToken(Account account, BigInteger value, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_AdditionalTokenOffering tx = new TX_AdditionalTokenOffering(account, nonce, value, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    /**
     *
     * @param account
     * @param value
     * @param tokenID
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp OfferMoreTokenAsync(Account account, BigInteger value, Integer tokenID, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.OfferMoreToken(account, value, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param tokenID
     * @return
     * @throws IOException
     */
    public SendTransactionResp DestroyToken(Account account, Integer tokenID) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce() + 1;

        TX_DestroyToken tx = new TX_DestroyToken(account, nonce, tokenID);
        OGRequestPOST req = tx.commit();

        String resp = this.server.Post(tx.rpcMethod, req);
        return JSON.parseObject(resp, SendTransactionResp.class);
    }

    /**
     *
     * @param account
     * @param tokenID
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp DestroyTokenAsync(Account account, Integer tokenID, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DestroyToken(account, tokenID);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param value
     * @param bytecode
     * @param constructorParameters
     * @return
     * @throws IOException
     */
    public SendTransactionResp DeployContract(Account account, BigInteger value, String bytecode, List<Type> constructorParameters) throws IOException {
        if (constructorParameters == null) {
            constructorParameters = new ArrayList<>();
        }
        String data = bytecode + FunctionEncoder.encodeConstructor(constructorParameters);
        return this.SendRawTransaction(account, EMPTY_ADDRESS, this.DEFAULT_TOKENID, value, data);
    }

    /**
     *
     * @param account
     * @param value
     * @param bytecode
     * @param constructorParameters
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp DeployContractAsync(Account account, BigInteger value, String bytecode, List<Type> constructorParameters, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DeployContract(account, value, bytecode, constructorParameters);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    /**
     *
     * @param account
     * @param contractAddress
     * @param value
     * @param funcName
     * @param inputs
     * @param outputParams
     * @return
     * @throws IOException
     */
    public SendTransactionResp CallContract(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams) throws IOException {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        if (outputParams == null) {
            outputParams = new ArrayList<>();
        }
        Function func = new Function(funcName, inputs, outputParams);
        String data = FunctionEncoder.encode(func);
//        System.out.println("callContract data: " + data);
        return this.SendRawTransaction(account, contractAddress, this.DEFAULT_TOKENID, value, data);
    }

    /**
     *
     * @param account
     * @param contractAddress
     * @param value
     * @param funcName
     * @param inputs
     * @param outputParams
     * @param callback
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public SendTransactionResp CallContractAsync(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams, IContractCallback callback) throws IOException, InterruptedException {
        Function func = new Function(funcName, inputs, outputParams);
        SendTransactionResp resp = this.CallContract(account, contractAddress, value, funcName, inputs, outputParams);
        checkContractCall(resp, func.getOutputParameters(), callback);
        return resp;
    }

    /**
     *
     * @param contractAddress
     * @param funcName
     * @param inputs
     * @param outputParams
     * @return
     * @throws IOException
     */
    public QueryContractResp QueryContract(String contractAddress, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams) throws IOException {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        if (outputParams == null) {
            outputParams = new ArrayList<>();
        }
        Function func = new Function(funcName, inputs, outputParams);
        String data = FunctionEncoder.encode(func);

        OGRequestPOST req = new OGRequestPOST();
        req.SetVariable("address", contractAddress);
        req.SetVariable("data", data);

        String resp = this.server.Post("query_contract", req);
        QueryContractRawResp contractResp = JSON.parseObject(resp, QueryContractRawResp.class);

        QueryContractResp queryContractResp = new QueryContractResp();
        queryContractResp.setErr(contractResp.getErr());
        if (contractResp.getErr().equals(EMPTY_ERROR)) {
//            System.out.println("data: " + contractResp.getData());
            queryContractResp.setOutputs(FunctionReturnDecoder.decode(contractResp.getData(), func.getOutputParameters()));
        }
        return queryContractResp;
    }

    /**
     *
     * @param tokenID
     * @return
     * @throws IOException
     */
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

    public QueryReceiptResp QueryReceiptUntilTimeout(String hash, Long timeout, Long interval) throws IOException, InterruptedException {
        Long timeoutTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < timeoutTime) {
            QueryReceiptResp queryReceiptResp = this.QueryReceipt(hash);
            if (queryReceiptResp.getData() != null) {
                return queryReceiptResp;
            }
            Thread.sleep(interval);
        }
        return null;
    }

    public static void main(String args[]) {

    }

}
