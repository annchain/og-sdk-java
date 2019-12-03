package io.annchain.og.types;

import org.bouncycastle.util.encoders.Hex;
import io.annchain.og.server.OGRequestPOST;
import io.annchain.og.utils.Bytes;
import io.annchain.og.utils.Secp256k1_Signer;

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
        byte[] valueBytes = this.value.toByteArray();
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

}
