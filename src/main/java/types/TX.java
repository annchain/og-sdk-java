package types;

import com.alibaba.fastjson.JSONObject;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.generated.Int32;
import org.web3j.abi.datatypes.generated.Uint64;
import server.OGRequestPOST;
import utils.Secp256k1_Signer;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class TX {

    private Account account;

    private Uint64 nonce;
    private String fromAddress;
    private String toAddress;
    private BigInteger value;
    private byte[] data;
    private Integer tokenID;

    private final String cryptoType = "secp256k1";
    public final String rpcMethod = "new_transaction";

    public TX(Account fromAccount, String toAddress, Uint64 nonce, BigInteger value, byte[] data, Integer tokenID) {
        this.account = fromAccount;
        this.nonce = nonce;
        this.fromAddress = fromAccount.GetAddress();
        this.toAddress = toAddress;
        this.value = value;
        this.data = data;
        this.tokenID = tokenID;
    }

    public byte[] signatureTargets() {
        byte[] fromBytes = Hex.decode(this.fromAddress);
        byte[] toBytes = Hex.decode(this.toAddress);

        int msgLength = 8 + fromBytes.length + toBytes.length + this.value.toByteArray().length + this.data.length + 4;
        byte[] msg = new byte[msgLength];
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);

        String temp = Hex.toHexString(this.nonce.getValue().toByteArray());
        String nonceStr = String.format("%16s", temp).replace(' ', '0');

        msgBuffer.put(Hex.decode(nonceStr));
        msgBuffer.put(fromBytes);
        msgBuffer.put(toBytes);
        msgBuffer.put(this.value.toByteArray());
        msgBuffer.put(data);

        String tempTokenID = Integer.toHexString(this.tokenID);
        String tokenID = String.format("%8s", tempTokenID).replace(' ', '0');
        msgBuffer.put(Hex.decode(tokenID));

        System.out.println("sig targets: " + Hex.toHexString(msg));

        return msg;
    }

    public String sign() {
        Secp256k1_Signer signer = new Secp256k1_Signer(this.account.GetPrivateKey());
        return signer.SignAsHex(this.signatureTargets());
    }

    public OGRequestPOST commit() {
        OGRequestPOST req = new OGRequestPOST();

        req.SetVariable("nonce", this.nonce.getValue().toString());
        req.SetVariable("from", this.fromAddress);
        req.SetVariable("to", this.toAddress);
        req.SetVariable("value", this.value.toString());
        req.SetVariable("data", Hex.toHexString(this.data));
        req.SetVariable("crypto_type", this.cryptoType);
        req.SetVariable("signature", this.sign());
        req.SetVariable("pubkey", Hex.toHexString(this.account.GetPublicKey()));
        req.SetVariable("token_id", this.tokenID);

        return req;
    }
}
