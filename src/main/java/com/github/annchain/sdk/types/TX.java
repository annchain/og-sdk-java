package com.github.annchain.sdk.types;

import com.github.annchain.sdk.utils.Bytes;
import com.github.annchain.sdk.utils.Secp256k1_Signer;
import org.bouncycastle.util.encoders.Hex;
import com.github.annchain.sdk.server.OGRequestPOST;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class TX {
    private Account account;
    private Long nonce;
    private String fromAddress;
    private String toAddress;
    private BigInteger value;
    private String data;
    private Integer tokenID;
    private String signature;

    private final String cryptoType = "secp256k1";

    public TX(Account fromAccount, String toAddress, Long nonce, BigInteger value, String data, Integer tokenID) {
        this.account = fromAccount;
        this.nonce = nonce;
        this.fromAddress = fromAccount.GetAddress();
        this.toAddress = toAddress;
        this.value = value;
        this.data = data;
        this.tokenID = tokenID;
        this.signature = this.sign();
    }

    public String getFromAddress() {
        return this.fromAddress;
    }

    public String getToAddress() {
        return this.toAddress;
    }

    public Long getNonce() {
        return this.nonce;
    }

    public BigInteger getValue() {
        return this.value;
    }

    public String getData() {
        return this.data;
    }

    public byte[] signatureTargets() {
        byte[] nonceBytes = Bytes.LongToBytes(this.nonce);
        byte[] fromBytes = Bytes.DecodeFromHex(this.fromAddress);
        byte[] toBytes = Bytes.DecodeFromHex(this.toAddress);
        byte[] valueBytes = Bytes.fixedBitIntegerToByteArray(this.value);
        byte[] dataBytes = Bytes.DecodeFromHex(this.data);
        byte[] tokenIDBytes = Bytes.IntToBytes(this.tokenID);

        int msgLength = nonceBytes.length + fromBytes.length + toBytes.length + valueBytes.length + dataBytes.length + tokenIDBytes.length;
        byte[] msg = new byte[msgLength];
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);
        msgBuffer.put(nonceBytes);
        msgBuffer.put(fromBytes);
        msgBuffer.put(toBytes);
        msgBuffer.put(valueBytes);
        msgBuffer.put(dataBytes);
        msgBuffer.put(tokenIDBytes);

//        System.out.println("sig targets: " + Hex.toHexString(msg));
        return msgBuffer.array();
    }

    public String sign() {
        Secp256k1_Signer signer = new Secp256k1_Signer(this.account.GetPrivateKey());
        return signer.SignAsHex(this.signatureTargets());
    }

    public OGRequestPOST commit() {
        OGRequestPOST req = new OGRequestPOST();

        req.SetVariable("nonce", this.nonce);
        req.SetVariable("from", this.fromAddress);
        req.SetVariable("to", this.toAddress);
        req.SetVariable("value", this.value.toString());
        req.SetVariable("data", this.data);
        req.SetVariable("crypto_type", this.cryptoType);
        req.SetVariable("signature", this.signature);
        req.SetVariable("pubkey", Hex.toHexString(this.account.GetPublicKey()));
        req.SetVariable("token_id", this.tokenID);

        return req;
    }

    public static void main(String args[]) {
        String privHex = "476bd95a7b69e4ab20264f37a525255b0ede4e7af03b0014c70aebc4d450e34f";
        Account account = new Account(privHex);

        String toHex = "0xc1eb507017610d2134bc70146731d777a25c2889";
        Long nonce = 17320L;
        BigInteger value = new BigInteger("894385949183117216");
        String dataHex = "";
        Integer tokenID = 0;

        TX tx = new TX(account, toHex, nonce, value, dataHex, tokenID);
        System.out.println(tx.signature);

//        byte[] sigB = Hex.decode(tx.signature);
//        sigB[sigB.length-1] -= 27;
//        System.out.println(Hex.toHexString(sigB));

        System.out.println("sig targets: " + Hex.toHexString(tx.signatureTargets()));

    }

}
