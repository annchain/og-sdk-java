package types;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import server.OGRequestPOST;
import utils.Secp256k1_Signer;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class TX_DestroyToken {
    private Account account;

    private Uint64 nonce;
    private String fromAddress;
    private BigInteger value;
    private final Boolean additionalIssue = false;
    private Integer tokenID;

    private final String cryptoType = "secp256k1";
    private final Uint8 action = new Uint8(1);
    public final String rpcMethod = "token/destroy";

    public TX_DestroyToken(Account fromAccount, Uint64 nonce, Integer tokenID) {
        this.account = fromAccount;
        this.nonce = nonce;
        this.fromAddress = fromAccount.GetAddress();
        this.value = new BigInteger("0");
        this.tokenID = tokenID;
    }

    public byte[] signatureTargets() {

        byte[] fromBytes = Hex.decode(this.fromAddress);

        // 8 bytes nonce + 1 byte action + from length + value length + additionalIssue + tokenID length
        int msgLength = 8 + 1 + fromBytes.length + this.value.toByteArray().length + 1 + 4;
        byte[] msg = new byte[msgLength];
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);

        String temp = Hex.toHexString(this.nonce.getValue().toByteArray());
        String nonceStr = String.format("%16s", temp).replace(' ', '0');

        msgBuffer.put(Hex.decode(nonceStr));
        msgBuffer.put(this.action.getValue().toByteArray());
        msgBuffer.put(fromBytes);
        msgBuffer.put(this.value.toByteArray());
        if (this.additionalIssue) {
            msgBuffer.put((byte)1);
        } else {
            msgBuffer.put((byte)0);
        }

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

        req.SetVariable("nonce", this.nonce.getValue().intValue());
        req.SetVariable("from", this.fromAddress);

        req.SetVariable("value", this.value.toString());

        req.SetVariable("action", this.action.getValue().intValue());

        req.SetVariable("enable_spo", this.additionalIssue);
        req.SetVariable("crypto_type", this.cryptoType);
        req.SetVariable("signature", this.sign());
        req.SetVariable("pubkey", Hex.toHexString(this.account.GetPublicKey()));

        req.SetVariable("token_id", this.tokenID);

        return req;
    }
}