import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.generated.Int32;
import org.web3j.abi.datatypes.generated.Uint64;
import server.OGRequestGET;
import server.OGRequestPOST;
import server.OGServer;
import sun.jvm.hotspot.debugger.SymbolLookup;
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

    public String TransferToken(Account account, String to, Integer tokenID, BigInteger value, byte[] data) {
        Long nonceLong = this.GetNonce(account.GetAddress());
        Uint64 nonce = new Uint64(nonceLong+1);

        TX tx = new TX(account, to, nonce, value, data, new Int32(tokenID));
        OGRequestPOST req = tx.commit();

        String resp;
        try {
            resp = this.server.Post("new_transaction", req);
        } catch (IOException e) {
            System.out.println("post new transaction error: " + e.toString());
            return "post new transaction error: " + e.toString();
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("transfer token error: " + json.getString("message"));
            return null;
        }
        String hash = json.getString("data");
        return hash;
    }

    public String InitialTokenOffering(Account account, BigInteger value, Boolean additionalIssue, String tokenName) {
        Long nonceLong = this.GetNonce(account.GetAddress());
        Uint64 nonce = new Uint64(nonceLong+1);

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

    public String OfferMoreToken(Account account, BigInteger value, Integer tokenID) {

        Long nonceLong = this.GetNonce(account.GetAddress());
        Uint64 nonce = new Uint64(nonceLong+1);

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

    public String DestroyToken(Account account, BigInteger value, Integer tokenID) {

        Long nonceLong = this.GetNonce(account.GetAddress());
        Uint64 nonce = new Uint64(nonceLong+1);

        TX_DestroyToken tx = new TX_DestroyToken(account, nonce, value, tokenID);
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

    public String QueryToken(Integer tokenID) {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("id", tokenID.toString());

        String resp;
        try {
            resp = this.server.Get("token", req);
        } catch (IOException e) {
            System.out.println("query token error: " + e.toString());
            return null;
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("query token error: " + json.getString("message"));
            return null;
        }
        JSONObject jsonobj = json.getJSONObject("data");
        return jsonobj.toJSONString();
    }

    public String QueryReceipt(String hash) {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("hash", hash);

        String resp;
        try {
            resp = this.server.Get("query_receipt", req);
        } catch (IOException e) {
            System.out.println("query receipt error: " + e.toString());
            return null;
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("query receipt error: " + json.getString("message"));
            return null;
        }
        JSONObject jsonobj = json.getJSONObject("data");
        return jsonobj.toJSONString();
    }

    public BigInteger QueryBalance(String address, Integer tokenID) {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        req.SetVariable("token_id", tokenID.toString());

        String resp;
        try {
            resp = this.server.Get("query_balance", req);
        } catch (IOException e) {
            System.out.println("post new transaction error: " + e.toString());
            return new BigInteger("0");
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("query balance error: " + json.getString("message"));
            return null;
        }
        JSONObject jsonobj = json.getJSONObject("data");
        Long balanceLong = jsonobj.getLong("balance");
        return new BigInteger(balanceLong.toString());
    }

    public Long GetNonce(String address) {
        OGRequestGET req = new OGRequestGET();
        req.SetVariable("address", address);
        String resp = "";
        try {
            resp = server.Get("query_nonce", req);
        } catch (IOException e) {
            System.out.println("query nonce error: " + e.toString());
        }
        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            System.out.println("query nonce error: " + json.getString("message"));
            return null;
        }

        Long nonceInteger = json.getLong("data");
        return nonceInteger;
    }

    public static void main(String args[]) {
        String url = "http://localhost:8000";
        OG og = new OG(url);

        Account account = new Account();
        String hash = og.InitialTokenOffering(account, new BigInteger("10000"), true, "uni");

        System.out.println("account private key: " + Hex.toHexString(account.GetPrivateKey()));
        System.out.println("pub key: " + Hex.toHexString(account.GetPublicKey()));
        System.out.println(hash);

        

//        Account account = new Account("00daa94713dc3883d240d093f605431f2756b9d2be0616e889c56c20f5822b28b3");
    }

}
