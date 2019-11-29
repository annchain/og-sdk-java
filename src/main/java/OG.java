import com.alibaba.fastjson.JSON;
import model.*;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
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
        if (constructorParameters == null) {
            constructorParameters = new ArrayList<>();
        }
        String data = bytecode + FunctionEncoder.encodeConstructor(constructorParameters);
        return this.SendRawTransaction(account, EMPTY_ADDRESS, this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp DeployContractAsync(Account account, BigInteger value, String bytecode, List<Type> constructorParameters, IConfirmCallback callback) throws IOException, InterruptedException {
        SendTransactionResp resp = this.DeployContract(account, value, bytecode, constructorParameters);
        checkTransactionConfirmed(resp, callback);
        return resp;
    }

    public SendTransactionResp CallContract(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams) throws IOException {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        if (outputParams == null) {
            outputParams = new ArrayList<>();
        }
        Function func = new Function(funcName, inputs, outputParams);
        String data = FunctionEncoder.encode(func);
        System.out.println("callContract data: " + data);
        return this.SendRawTransaction(account, contractAddress, this.DEFAULT_TOKENID, value, data);
    }

    public SendTransactionResp CallContractAsync(Account account, String contractAddress, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams, IContractCallback callback) throws IOException, InterruptedException {
        Function func = new Function(funcName, inputs, outputParams);
        SendTransactionResp resp = this.CallContract(account, contractAddress, value, funcName, inputs, outputParams);
        checkContractCall(resp, func.getOutputParameters(), callback);
        return resp;
    }

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
            System.out.println("data: " + contractResp.getData());
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

    public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException {

        String url = "http://localhost:8000";
//        String url = "http://172.28.152.102:30066";
        OG og = new OG(url);
        Account account = new Account("0000000000000000000000000000000000000000000000000000000000000001");
        System.out.println(account.GetAddress());

        Account account2 = new Account("0000000000000000000000000000000000000000000000000000000000000002");
        Account account3 = new Account("0000000000000000000000000000000000000000000000000000000000000003");
        Account account4 = new Account("0000000000000000000000000000000000000000000000000000000000000004");
        Account account5 = new Account("0000000000000000000000000000000000000000000000000000000000000005");
        Account account6 = new Account("0000000000000000000000000000000000000000000000000000000000000006");
        Account account7 = new Account("0000000000000000000000000000000000000000000000000000000000000007");
        Account account8 = new Account("0000000000000000000000000000000000000000000000000000000000000008");
        Account account9 = new Account("0000000000000000000000000000000000000000000000000000000000000009");

        BigInteger value = new BigInteger("10000");
        og.SendTransaction(account, account2.GetAddress(), value);
        og.SendTransaction(account, account3.GetAddress(), value);
        og.SendTransaction(account, account4.GetAddress(), value);
        og.SendTransaction(account, account5.GetAddress(), value);
        og.SendTransaction(account, account6.GetAddress(), value);
        og.SendTransaction(account, account7.GetAddress(), value);
        og.SendTransaction(account, account8.GetAddress(), value);
        og.SendTransaction(account, account9.GetAddress(), value);

        Thread.sleep(3000);
        QueryBalanceResp balanceResp = og.QueryBalance(account9.GetAddress(), og.DEFAULT_TOKENID);
        System.out.println(balanceResp.getData().getBalance());

        String bytecode = "608060405234801561001057600080fd5b5033600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506110e9806100616000396000f3fe6080604052600436106100a4576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806304d0a647146100a957806309f98da11461014f578063155bf4e2146101de5780632dc779b91461029257806392bd8f43146103665780639eba96c4146103d5578063a87430ba14610430578063e2f0e58a14610495578063f1a3b94314610591578063f540350a146105cc575b600080fd5b3480156100b557600080fd5b506100f8600480360360208110156100cc57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610638565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561013b578082015181840152602081019050610120565b505050509050019250505060405180910390f35b34801561015b57600080fd5b506101c86004803603606081101561017257600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506106d1565b6040518082815260200191505060405180910390f35b3480156101ea57600080fd5b506102176004803603602081101561020157600080fd5b8101908080359060200190929190505050610791565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561025757808201518184015260208101905061023c565b50505050905090810190601f1680156102845780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561029e57600080fd5b506102eb600480360360408110156102b557600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506107a4565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561032b578082015181840152602081019050610310565b50505050905090810190601f1680156103585780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561037257600080fd5b506103bf6004803603604081101561038957600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061090a565b6040518082815260200191505060405180910390f35b3480156103e157600080fd5b5061042e600480360360408110156103f857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061091f565b005b34801561043c57600080fd5b5061047f6004803603602081101561045357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610aec565b6040518082815260200191505060405180910390f35b3480156104a157600080fd5b5061057b600480360360408110156104b857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001906401000000008111156104f557600080fd5b82018360208201111561050757600080fd5b8035906020019184600183028401116401000000008311171561052957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610b0a565b6040518082815260200191505060405180910390f35b34801561059d57600080fd5b506105ca600480360360208110156105b457600080fd5b8101908080359060200190929190505050610dff565b005b3480156105d857600080fd5b506105e1610f39565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b83811015610624578082015181840152602081019050610609565b505050509050019250505060405180910390f35b60606000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002018054806020026020016040519081016040528092919081815260200182805480156106c557602002820191906000526020600020905b8154815260200190600101908083116106b1575b50505050509050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16148061075a57508373ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b151561076557600080fd5b606061077185846107a4565b905061077d858461091f565b6107878482610b0a565b9150509392505050565b606061079d33836107a4565b9050919050565b60606000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600083815260200190815260200160002060020160009054906101000a900460ff16151561081457600080fd5b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160008381526020019081526020016000206001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108fd5780601f106108d2576101008083540402835291602001916108fd565b820191906000526020600020905b8154815290600101906020018083116108e057829003601f168201915b5050505050905092915050565b60006109173384846106d1565b905092915050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806109a657508173ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b15156109b157600080fd5b60006001026000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002016000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000154815481101515610a5957fe5b90600052602060002001819055506000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016000828152602001908152602001600020600080820160009055600182016000610ad29190610fd0565b6002820160006101000a81549060ff021916905550505050565b60006020528060005260406000206000915090508060000154905081565b6000806000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001549050838184604051602001808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166c0100000000000000000000000002815260140183815260200182805190602001908083835b602083101515610bd35780518252602082019150602081019050602083039250610bae565b6001836020036101000a03801982511681845116808217855250505050505090500193505050506040516020818303038152906040528051906020012091506000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600201829080600181540180825580915050906001820390600052602060002001600090919290919091505550806000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000181905550826000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160008481526020019081526020016000206001019080519060200190610d40929190611018565b5060016000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060020160006101000a81548160ff021916908315150217905550600181016000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001819055505092915050565b60006001026000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002016000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000154815481101515610ea757fe5b90600052602060002001819055506000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016000828152602001908152602001600020600080820160009055600182016000610f209190610fd0565b6002820160006101000a81549060ff0219169055505050565b60606000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600201805480602002602001604051908101604052809291908181526020018280548015610fc657602002820191906000526020600020905b815481526020019060010190808311610fb2575b5050505050905090565b50805460018160011615610100020316600290046000825580601f10610ff65750611015565b601f0160209004906000526020600020908101906110149190611098565b5b50565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061105957805160ff1916838001178555611087565b82800160010185558215611087579182015b8281111561108657825182559160200191906001019061106b565b5b5090506110949190611098565b5090565b6110ba91905b808211156110b657600081600090555060010161109e565b5090565b9056fea165627a7a72305820d3266e7c7c7eac4ba3a63200f8fd1278d15114aea89229143858181fb324155f0029";
        SendTransactionResp deployResp = og.DeployContract(account, new BigInteger("0"), bytecode,null);
        if (!deployResp.getErr().equals("")) {
            System.out.println("deploy err: " + deployResp.getErr());
            return;
        }
        System.out.println("deployed: " + deployResp.getHash());

        Long timeout = 30000L;
        Long interval = 100L;
        QueryReceiptResp receiptResp = og.QueryReceiptUntilTimeout(deployResp.getHash(), timeout, interval);
        if (receiptResp == null) {
            System.out.println("contract null");
            return;
        }
        System.out.println("contract address: " + receiptResp.getData().getResult());

        String contractAddress = receiptResp.getData().getResult();
        String funcName = "issueToken";

        // inputs
        List<Type> inputs = new ArrayList<>();
        inputs.add(new Address(account2.GetAddress()));
        byte[] b = {0x22};
        inputs.add(new DynamicBytes(b));

        // outputs
        List<TypeReference<?>> outputParams = new ArrayList<>();
        outputParams.add(TypeReference.create(Bytes32.class));

        // init func
        Function func = new Function(funcName, inputs, outputParams);

        // call contract
        SendTransactionResp callContractResp = og.CallContract(account, contractAddress, new BigInteger("0"), funcName, inputs, outputParams);
        if (!callContractResp.getErr().equals("")) {
            System.out.println("call contract err: " + callContractResp.getErr());
            return;
        }

        // get receipt
        System.out.println("query callContract receipt, hash: " + callContractResp.getHash());
        receiptResp = og.QueryReceiptUntilTimeout(callContractResp.getHash(), timeout, interval);
        if (!receiptResp.getErr().equals("")) {
            System.out.println("query receipt err: " + receiptResp.getErr());
            return;
        }
        String rawOutput = receiptResp.getData().getResult();
        System.out.println("rawOutput: " + rawOutput);
        List<Type> outputs = FunctionReturnDecoder.decode(rawOutput, func.getOutputParameters());
        Bytes32 outputHash = (Bytes32) (outputs.get(0));
        System.out.println("call contract hash: " + Hex.toHexString(outputHash.getValue()));




        // get my wallet
        String myWalletFuncName = "getWallet";

        inputs = new ArrayList<>();
        inputs.add(new Address(account2.GetAddress()));

        List<TypeReference<?>> myWalletOutputsRef = new ArrayList<>();
        myWalletOutputsRef.add(new TypeReference<DynamicArray<Bytes32>>() {});

        QueryContractResp queryContractResp = og.QueryContract(contractAddress, myWalletFuncName, inputs, myWalletOutputsRef);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("contract error: " + queryContractResp.getErr());
            return;
        }

        List<Type> contractRespOutputs = queryContractResp.getOutputs();
        DynamicArray<Bytes32> dynamicArray = (DynamicArray<Bytes32>) contractRespOutputs.get(0);
        List<Bytes32> bts = dynamicArray.getValue();
        for (Bytes32 bt: bts) {
            System.out.println("--- bt: " + Hex.toHexString(bt.getValue()));
        }

        // query token
        String queryTokenFuncName = "getToken";

        inputs = new ArrayList<>();
        inputs.add(new Address(account2.GetAddress()));
        inputs.add(new Bytes32(bts.get(0).getValue()));

        outputParams = new ArrayList<>();
        outputParams.add(new TypeReference<DynamicBytes>() {});

        queryContractResp = og.QueryContract(contractAddress, queryTokenFuncName, inputs, outputParams);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("query contract getToken error: " + queryContractResp.getErr());
            return;
        }
        contractRespOutputs = queryContractResp.getOutputs();
        DynamicBytes o = (DynamicBytes) contractRespOutputs.get(0);
        System.out.println("o: " + Hex.toHexString(o.getValue()));

        // shift account2's token to account3
        funcName = "shift";

        inputs = new ArrayList<>();
        inputs.add(new Address(account2.GetAddress()));
        inputs.add(new Address(account3.GetAddress()));
        inputs.add(new Bytes32(bts.get(0).getValue()));

        outputParams = new ArrayList<>();
        outputParams.add(new TypeReference<Bytes32>() {});

        callContractResp = og.CallContract(account, contractAddress, new BigInteger("0"), funcName, inputs, outputParams);
        if (!callContractResp.getErr().equals("")) {
            System.out.println("call contract err: " + callContractResp.getErr());
            return;
        }

        // get shift result hash
        System.out.println("query callContract receipt, hash: " + callContractResp.getHash());
        receiptResp = og.QueryReceiptUntilTimeout(callContractResp.getHash(), timeout, interval);
        if (!receiptResp.getErr().equals("")) {
            System.out.println("query receipt err: " + receiptResp.getErr());
            return;
        }
        rawOutput = receiptResp.getData().getResult();
        System.out.println("rawOutput: " + rawOutput);
        outputs = FunctionReturnDecoder.decode(rawOutput, func.getOutputParameters());
        outputHash = (Bytes32) (outputs.get(0));
        System.out.println("call contract hash: " + Hex.toHexString(outputHash.getValue()));

        // check account3 token
        funcName = "getToken";

        inputs = new ArrayList<>();
        inputs.add(new Address(account3.GetAddress()));
        inputs.add(new Bytes32(outputHash.getValue()));

        outputParams = new ArrayList<>();
        outputParams.add(new TypeReference<DynamicBytes>() {});

        queryContractResp = og.QueryContract(contractAddress, funcName, inputs, outputParams);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("query contract getToken error: " + queryContractResp.getErr());
            return;
        }
        contractRespOutputs = queryContractResp.getOutputs();
        o = (DynamicBytes) contractRespOutputs.get(0);
        System.out.println("account 3 o: " + Hex.toHexString(o.getValue()));

    }

}
