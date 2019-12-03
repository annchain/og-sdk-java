package io.annchain.og.utils;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;

import java.math.BigInteger;

public class Secp256k1_KeyGenerator {

    public static BigInteger GeneratePublicKey(ECKeyPair keyPair) {

        byte[] publicKeyBytes = keyPair.getPublicKey().toByteArray();
        if (publicKeyBytes.length == 64) {
            byte[] c = new byte[65];
            System.arraycopy(Hex.decode("04"), 0, c, 0, 1);
            System.arraycopy(publicKeyBytes, 0, c, 1, publicKeyBytes.length);
            publicKeyBytes = c;
        } else if (publicKeyBytes.length == 65) {
            publicKeyBytes[0] = Hex.decode("04")[0];
        }
        return new BigInteger(publicKeyBytes);
    }

    public static void main(String args[]) {
        ECKeyPair keyPair = ECKeyPair.create(Hex.decode("2e93161cabdc5cd4657af836a4e89cd1d9ccb5b45cd601809a3ba26082d35077"));

        BigInteger pubBig = GeneratePublicKey(keyPair);

        System.out.println(Hex.toHexString(pubBig.toByteArray()));

    }
}
