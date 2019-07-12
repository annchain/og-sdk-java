
import org.bouncycastle.util.encoders.Hex;
//import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;

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
        return Sign.signMessage(msg, this.keyPair);
    }

    public byte[] SignAsBytes(byte[] msg) {
        msg = Hash.sha256(msg);

        Sign.SignatureData sig = Sign.signMessage(msg, this.keyPair);
        return this.SignatureToBytes(sig);
    }

    public String SignAsHex(byte[] msg) {
        msg = Hash.sha256(msg);

        Sign.SignatureData sig = Sign.signMessage(msg, this.keyPair);
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

//    public static void main(String[] args) {
//        // generate tx data
//        byte[] from = Hex.decode("8c444f64c59ba1bdca8df453062a51068ecb84c1");
//        byte[] to = Hex.decode("96f4ac2f3215b80ea3a6466ebc1f268f6f1d5406");
//        BigInteger value = new BigInteger("123456");
//        byte[] data = Hex.decode("7613abcdef");
//        Uint64 nonce = new Uint64(1);
//
//        String temp = Hex.toHexString(nonce.getValue().toByteArray());
//        String nonceStr = String.format("%16s", temp).replace(' ', '0');
//
//        // create sign message
//        int msgLength = 8 + from.length + to.length + value.toByteArray().length + data.length;
//        byte[] msg = new byte[msgLength];
//        ByteBuffer msgBuffer = ByteBuffer.wrap(msg);
//        msgBuffer.put(Hex.decode(nonceStr));
//        msgBuffer.put(from);
//        msgBuffer.put(to);
//        msgBuffer.put(value.toByteArray());
//        msgBuffer.put(data);
//
//        System.out.println(Hex.toHexString(msg));
//
//        // generate key pair
//        byte[] privBytes = Hex.decode("7b2102f7662a35682fedb1a35979de06397eef7c177a18f1877a17a4ade8ddfa");
//        BigInteger priv = new BigInteger(privBytes);
//        BigInteger pub = Sign.publicKeyFromPrivate(priv);
//        ECKeyPair eckp = new ECKeyPair(priv, pub);
//
//        // secp256k1 sign
//        msg = Hash.sha256(msg);
//        Sign.SignatureData sigData = Sign.signMessage(msg, eckp, false);
//
//        // convert signature to byte[]
//        byte[] r = sigData.getR();
//        byte[] s = sigData.getS();
//        byte v = sigData.getV();
//
//        byte[] sig = new byte[r.length + s.length + 1];
//        ByteBuffer sigBuffer = ByteBuffer.wrap(sig);
//        sigBuffer.put(r);
//        sigBuffer.put(s);
//        sigBuffer.put(v);
//
//        System.out.println(Hex.toHexString(sig));
//
//    }

}
