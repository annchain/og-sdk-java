package types;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import server.OGRequestPOST;
import utils.Bytes;
import utils.Secp256k1_Signer;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class TX_DestroyToken extends TX_Action {
    private final Short action = this.actionDestroy;
    private final Boolean additionalIssue = this.additionalIssueOff;
    public final String rpcMethod = "token/destroy";

    public TX_DestroyToken(Account fromAccount, Long nonce, Integer tokenID) {
        this.account = fromAccount;
        this.nonce = nonce;
        this.fromAddress = fromAccount.GetAddress();
        this.value = new BigInteger("0");
        this.tokenID = tokenID;
    }

    public byte[] signatureTargets() {
        byte[] nonceBytes = Bytes.LongToBytes(this.nonce);
        byte[] actionBytes = Bytes.ShortToBytes(this.action);
        byte[] fromBytes = Hex.decode(this.fromAddress);
        byte[] valueBytes = this.value.toByteArray();
        byte[] additionIssueBytes = Bytes.ShortToBytes(this.additionalIssuaToShort(this.additionalIssue));
        byte[] tokenIDBytes = Bytes.IntToBytes(this.tokenID);

        // 8 bytes nonce + 1 byte action + from length + value length + additionalIssue + tokenID length
        int msgLength = nonceBytes.length + actionBytes.length + fromBytes.length + valueBytes.length + additionIssueBytes.length + tokenIDBytes.length;
        byte[] msg = new byte[msgLength];
        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);

        msgBuffer.put(nonceBytes);
        msgBuffer.put(actionBytes);
        msgBuffer.put(fromBytes);
        msgBuffer.put(valueBytes);
        msgBuffer.put(additionIssueBytes);
        msgBuffer.put(tokenIDBytes);

        System.out.println("sig targets: " + Hex.toHexString(msg));

        return msg;
    }

    public String sign() {
        Secp256k1_Signer signer = new Secp256k1_Signer(this.account.GetPrivateKey());
        return signer.SignAsHex(this.signatureTargets());
    }

    public OGRequestPOST commit() {

        OGRequestPOST req = new OGRequestPOST();

        req.SetVariable("nonce", this.nonce);
        req.SetVariable("from", this.fromAddress);
        req.SetVariable("value", this.value.toString());
        req.SetVariable("action", this.action);
        req.SetVariable("enable_spo", this.additionalIssue);
        req.SetVariable("crypto_type", this.cryptoType);
        req.SetVariable("signature", this.sign());
        req.SetVariable("pubkey", Hex.toHexString(this.account.GetPublicKey()));
        req.SetVariable("token_id", this.tokenID);

        return req;
    }
}
