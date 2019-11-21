import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import model.QueryBalanceResp;
import model.QueryNonceResp;
import model.QueryReceiptResp;
import model.QueryTokenResp;
import org.web3j.abi.datatypes.generated.Uint64;
import server.OGRequestGET;
import server.OGRequestPOST;
import server.OGServer;
import types.*;

import java.io.IOException;
import java.math.BigInteger;

public class OG {

    private String node_url;
    private OGServer server;

    public OG(String url) {
        this.node_url = url;
        this.server = new OGServer(url);
    }

    public String TransferToken(Account account, String to, Integer tokenID, BigInteger value) throws IOException {
        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();
        TX tx = new TX(account, to, nonce, value, "", tokenID);
        OGRequestPOST req = tx.commit();
        String resp = this.server.Post("new_transaction", req);
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("transfer token error: " + json.getString("message"));
            return null;
        }
        String hash = json.getString("data");
        return hash;
    }

    public String InitialTokenOffering(Account account, BigInteger value, Boolean additionalIssue, String tokenName) throws IOException {

        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_InitialTokenOffering tx = new TX_InitialTokenOffering(account, nonce, value, additionalIssue, tokenName);
        OGRequestPOST req = tx.commit();

        String resp;
        try {
            resp = this.server.Post(tx.rpcMethod, req);
        } catch (IOException e) {
            System.out.println("post new initail offering error: " + e.toString());
            return "post new initail offering error: " + e.toString();
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("post new initail offering error: " + json.getString("message"));
            return null;
        }
        String hash = json.getString("data");
        return hash;
    }

    public String OfferMoreToken(Account account, BigInteger value, Integer tokenID) throws IOException {

        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_AdditionalTokenOffering tx = new TX_AdditionalTokenOffering(account, nonce, value, tokenID);
        OGRequestPOST req = tx.commit();

        String resp;
        try {
            resp = this.server.Post(tx.rpcMethod, req);
        } catch (IOException e) {
            System.out.println("post new additional offering error: " + e.toString());
            return "post new additional offering error: " + e.toString();
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("post new additional offering error: " + json.getString("message"));
            return null;
        }
        String hash = json.getString("data");
        return hash;
    }

    public String DestroyToken(Account account, Integer tokenID) throws IOException {

        QueryNonceResp nonceResp = this.QueryNonce(account.GetAddress());
        Long nonce = nonceResp.getNonce();

        TX_DestroyToken tx = new TX_DestroyToken(account, nonce, tokenID);
        OGRequestPOST req = tx.commit();

        String resp;
        try {
            resp = this.server.Post(tx.rpcMethod, req);
        } catch (IOException e) {
            System.out.println("post new token destroy error: " + e.toString());
            return "post new token destroy offering error: " + e.toString();
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("post new token destroy offering error: " + json.getString("message"));
            return null;
        }
        String hash = json.getString("data");
        return hash;
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

    public static void main(String args[]) throws IOException {
        String url = "http://localhost:8000";
        OG og = new OG(url);

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

        // test query balance
        QueryBalanceResp balance = og.QueryBalance("c6b6e9949e1e4a4c4df8b80a9a07c95008b563ea", 0);
        System.out.println("balance is: " + balance.getData().getBalance());
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

        Account toAccount = new Account();
        String transferToken = og.TransferToken(account, toAccount.GetAddress(), 0, new BigInteger("100"));
//
//        System.out.println("transfer token tx hash: " + transferToken);

    }

}
