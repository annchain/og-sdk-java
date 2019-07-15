package utils;

import org.bouncycastle.util.encoders.Hex;
//import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Secp256k1_Signer {

    private BigInteger privateKey;
    private BigInteger publicKey;

    private ECKeyPair keyPair;

    public Secp256k1_Signer(BigInteger privBigInt) {
        this.privateKey = privBigInt;
        this.publicKey = Sign.publicKeyFromPrivate(this.privateKey);
        this.keyPair = new ECKeyPair(this.privateKey, this.publicKey);
    }

    public Secp256k1_Signer(byte[] privBytes) {
        if (privBytes.length == 0) {
            throw new IllegalArgumentException("private key bytes should longer than zero");
        }
        this.privateKey = new BigInteger(privBytes);
        this.publicKey = Sign.publicKeyFromPrivate(this.privateKey);
        this.keyPair = new ECKeyPair(this.privateKey, this.publicKey);
    }

    public Secp256k1_Signer(String privHex) {
        if (privHex.length() == 0) {
            throw new IllegalArgumentException("private key in hex should longer than zero");
        }
        byte[] privBytes = Hex.decode(privHex);
        this.privateKey = new BigInteger(privBytes);
        this.publicKey = Sign.publicKeyFromPrivate(this.privateKey);
        this.keyPair = new ECKeyPair(this.privateKey, this.publicKey);
    }

    public Sign.SignatureData Sign(byte[] msg) {
        msg = Hash.sha256(msg);

        return Sign.signMessage(msg, this.keyPair, false);
    }

    public byte[] SignAsBytes(byte[] msg) {
        msg = Hash.sha256(msg);

        Sign.SignatureData sig = Sign.signMessage(msg, this.keyPair, false);
        return this.SignatureToBytes(sig);
    }

    public String SignAsHex(byte[] msg) {
        msg = Hash.sha256(msg);

        Sign.SignatureData sig = Sign.signMessage(msg, this.keyPair, false);
        return this.SignatureToHex(sig);
    }

    public byte[] SignatureToBytes(Sign.SignatureData sig) {
        byte[] r = sig.getR();
        byte[] s = sig.getS();
        byte v = sig.getV();

        byte[] sigBytes = new byte[r.length + s.length + 1];
        ByteBuffer sigBuffer = ByteBuffer.wrap(sigBytes);
        sigBuffer.put(r);
        sigBuffer.put(s);
        sigBuffer.put(v);

        return sigBytes;
    }

    public String SignatureToHex(Sign.SignatureData sig) {
        byte[] b = this.SignatureToBytes(sig);
        return Hex.toHexString(b);
    }

    public static void main(String[] args) {
        String msg = "000000000000000100f481a42b547852dce4a3cf51e981ffe8417ee088271001756e69";
        String priv = "4d5b41bfcb6a7d741df26067b437a31f39b1da41117b922e791277b48f8a57a9";

        Secp256k1_Signer signer = new Secp256k1_Signer(priv);
        Sign.SignatureData sig = signer.Sign(Hex.decode(msg));

        System.out.println(signer.SignatureToHex(sig));
    }

}
