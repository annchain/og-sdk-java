package types;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.*;
import server.OGRequestPOST;
import server.OGServer;
import utils.Bytes;
import utils.Secp256k1_KeyGenerator;
import utils.Secp256k1_Signer;

import java.io.IOException;
import java.math.BigInteger;

public class Account {

    private final String seed = "";
    private BigInteger privateKey;
    private BigInteger publicKey;
    private String address;

    public Account() {
        ECKeyPair keyPair = this.GenerateECKeyPair();
        this.privateKey = keyPair.getPrivateKey();
        this.publicKey = Secp256k1_KeyGenerator.GeneratePublicKey(keyPair);
        try {
            this.address = this.ECKeyPairToAddress(keyPair);
        } catch (CipherException e) {
            System.out.println("generate address error: " + e.toString());
        }
    }

    public Account(String privString) {
        byte[] privBytes = Bytes.DecodeFromHex(privString);

        ECKeyPair keyPair = ECKeyPair.create(privBytes);
        this.privateKey = keyPair.getPrivateKey();
        this.publicKey = Secp256k1_KeyGenerator.GeneratePublicKey(keyPair);
        try {
            this.address = this.ECKeyPairToAddress(keyPair);
        } catch (CipherException e) {
            System.out.println("generate address error: " + e.toString());
        }
    }

    public Account(byte[] priv) {
        ECKeyPair keyPair = ECKeyPair.create(priv);
        this.privateKey = keyPair.getPrivateKey();
        this.publicKey = Secp256k1_KeyGenerator.GeneratePublicKey(keyPair);
        try {
            this.address = this.ECKeyPairToAddress(keyPair);
        } catch (CipherException e) {
            System.out.println("generate address error: " + e.toString());
        }
    }

    public Account(BigInteger priv) {
        ECKeyPair keyPair = ECKeyPair.create(priv);
        this.privateKey = keyPair.getPrivateKey();
        this.publicKey = Secp256k1_KeyGenerator.GeneratePublicKey(keyPair);
        try {
            this.address = this.ECKeyPairToAddress(keyPair);
        } catch (CipherException e) {
            System.out.println("generate address error: " + e.toString());
        }
    }

    public byte[] GetPrivateKey() {
        return this.privateKey.toByteArray();
    }

    public byte[] GetPublicKey() {
        return this.publicKey.toByteArray();
    }

    public String GetAddress() {
        return this.address;
    }

    public String ECKeyPairToAddress(ECKeyPair pair) throws CipherException {
        WalletFile aWallet = Wallet.createLight(this.seed, pair);
        return aWallet.getAddress();
    }

    public ECKeyPair GenerateECKeyPair() {
        ECKeyPair keyPair;
        try {
            keyPair = Keys.createEcKeyPair();
        } catch (Exception e) {
            System.out.println("create eckey pair error: " + e.toString());
            return null;
        }
        return keyPair;
    }

    public ECKeyPair GenerateECKeyPair(String url) throws IOException {
        OGServer server = new OGServer(url);

        OGRequestPOST req = new OGRequestPOST();
        req.SetVariable("algorithm", "secp256k1");
        String resp = server.Post("new_account", req);

        JSONObject json = JSON.parseObject(resp);
        if (!json.getString("message").equals("")) {
            return null;
        }

        String privHex = json.getJSONObject("data").getString("privkey");
        String pubHex = json.getJSONObject("data").getString("pubkey");

        byte[] privBytes = Bytes.DecodeFromHex(privHex);
        BigInteger priv = new BigInteger(privBytes);
        byte[] pubBytes = Bytes.DecodeFromHex(pubHex);
        BigInteger pub = new BigInteger(pubBytes);

        return new ECKeyPair(priv, pub);
    }

    public static void main(String args[]) {
        Account a = new Account("1bea8601f99f786bff1fa81c7ba8910c79567e23a38f99942ae03be1a1b2c217");

        byte[] priv = a.GetPrivateKey();
        byte[] pub = a.GetPublicKey();
        String addr = a.GetAddress();

        System.out.println(Hex.toHexString(priv));
        System.out.println(Hex.toHexString(pub));
        System.out.println(addr);

    }


}
