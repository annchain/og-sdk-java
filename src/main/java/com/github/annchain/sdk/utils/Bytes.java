package com.github.annchain.sdk.utils;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Bytes {
    public static byte[] LongToBytes(Long i) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(i);
        return buffer.array();
    }

    public static Long BytesToLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(b);
        return buffer.getLong();
    }

    public static byte[] IntToBytes(Integer i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        return buffer.array();
    }

    public static Integer BytesToInt(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(b);
        return buffer.getInt();
    }

    public static byte[] ShortToBytes(Short i) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(i);
        return buffer.array();
    }

    public static Short BytesToShort(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(b);
        return buffer.getShort();
    }

    public static byte[] DecodeFromHex(String s) {
        if (s.startsWith("0x")) {
            s = s.substring(2);
        }
        return Hex.decode(s);
    }

    public static byte[] fixedBitIntegerToByteArray(BigInteger i) {
        byte[] original = i.toByteArray();
        if (original[0] == 0 && !i.equals(BigInteger.ZERO)) {
            // extra byte because of two's complement
            return Arrays.copyOfRange(original, 1, original.length);
        }
        return original;
    }

    public static void main(String arg[]) {
        Integer i = 10000;

        byte[] b = IntToBytes(i);
        System.out.println(Hex.toHexString(b));
    }
}
