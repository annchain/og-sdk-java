package utils;

import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;

import java.math.BigInteger;
import java.util.List;

public class Web3jSolType {

    public static Bool Bool(Boolean bool) {
        return new Bool(bool);
    }

    public static Address Address(String hex) {
        return new Address(hex);
    }

    public static NumericType Number(String type, BigInteger value) throws UnsupportedOperationException {
        if (type.equals("int")) {
            return new Int(value);
        }
        if (type.equals("uint")) {
            return new Uint(value);
        }
        if (type.equals("int8")) {
            return new Int8(value);
        }
        if (type.equals("uint8")) {
            return new Uint8(value);
        }
        if (type.equals("int16")) {
            return new Int16(value);
        }
        if (type.equals("uint16")) {
            return new Uint16(value);
        }
        if (type.equals("int24")) {
            return new Int24(value);
        }
        if (type.equals("uint24")) {
            return new Uint24(value);
        }
        if (type.equals("int32")) {
            return new Int32(value);
        }
        if (type.equals("uint32")) {
            return new Uint32(value);
        }
        if (type.equals("int40")) {
            return new Int40(value);
        }
        if (type.equals("uint40")) {
            return new Uint40(value);
        }
        if (type.equals("int48")) {
            return new Int48(value);
        }
        if (type.equals("uint48")) {
            return new Uint48(value);
        }
        if (type.equals("int56")) {
            return new Int56(value);
        }
        if (type.equals("uint56")) {
            return new Uint56(value);
        }
        if (type.equals("int64")) {
            return new Int64(value);
        }
        if (type.equals("uint64")) {
            return new Uint64(value);
        }
        if (type.equals("int72")) {
            return new Int72(value);
        }
        if (type.equals("uint72")) {
            return new Uint72(value);
        }
        if (type.equals("int80")) {
            return new Int80(value);
        }
        if (type.equals("uint80")) {
            return new Uint80(value);
        }
        if (type.equals("int88")) {
            return new Int88(value);
        }
        if (type.equals("uint88")) {
            return new Uint88(value);
        }
        if (type.equals("int96")) {
            return new Int96(value);
        }
        if (type.equals("uint96")) {
            return new Uint96(value);
        }
        if (type.equals("int104")) {
            return new Int104(value);
        }
        if (type.equals("uint104")) {
            return new Uint104(value);
        }
        if (type.equals("int112")) {
            return new Int112(value);
        }
        if (type.equals("uint112")) {
            return new Uint112(value);
        }
        if (type.equals("int120")) {
            return new Int120(value);
        }
        if (type.equals("uint120")) {
            return new Uint120(value);
        }
        if (type.equals("int128")) {
            return new Int128(value);
        }
        if (type.equals("uint128")) {
            return new Uint128(value);
        }
        if (type.equals("int136")) {
            return new Int136(value);
        }
        if (type.equals("uint136")) {
            return new Uint136(value);
        }
        if (type.equals("int144")) {
            return new Int144(value);
        }
        if (type.equals("uint144")) {
            return new Uint144(value);
        }
        if (type.equals("int152")) {
            return new Int152(value);
        }
        if (type.equals("uint152")) {
            return new Uint152(value);
        }
        if (type.equals("int160")) {
            return new Int160(value);
        }
        if (type.equals("uint160")) {
            return new Uint160(value);
        }
        if (type.equals("int168")) {
            return new Int168(value);
        }
        if (type.equals("uint168")) {
            return new Uint168(value);
        }
        if (type.equals("int176")) {
            return new Int176(value);
        }
        if (type.equals("uint176")) {
            return new Uint176(value);
        }
        if (type.equals("int184")) {
            return new Int184(value);
        }
        if (type.equals("uint184")) {
            return new Uint184(value);
        }
        if (type.equals("int192")) {
            return new Int192(value);
        }
        if (type.equals("uint192")) {
            return new Uint192(value);
        }
        if (type.equals("int200")) {
            return new Int200(value);
        }
        if (type.equals("uint200")) {
            return new Uint200(value);
        }
        if (type.equals("int208")) {
            return new Int208(value);
        }
        if (type.equals("uint208")) {
            return new Uint208(value);
        }
        if (type.equals("int216")) {
            return new Int216(value);
        }
        if (type.equals("uint216")) {
            return new Uint216(value);
        }
        if (type.equals("int224")) {
            return new Int224(value);
        }
        if (type.equals("uint224")) {
            return new Uint224(value);
        }
        if (type.equals("int232")) {
            return new Int232(value);
        }
        if (type.equals("uint232")) {
            return new Uint232(value);
        }
        if (type.equals("int240")) {
            return new Int240(value);
        }
        if (type.equals("uint240")) {
            return new Uint240(value);
        }
        if (type.equals("int248")) {
            return new Int248(value);
        }
        if (type.equals("uint248")) {
            return new Uint248(value);
        }
        if (type.equals("int256")) {
            return new Int256(value);
        }
        if (type.equals("uint256")) {
            return new Uint256(value);
        }

        throw new UnsupportedOperationException("unknown type: " + type);
    }

    public static BytesType BytesFixed(byte[] b) {
        if (b.length <= 0 || b.length > 32) {
            throw new UnsupportedOperationException("bytes length M must in range 0 < M <= 32");
        }
        return new BytesType(b, "bytes" + b.length);
    }

    public static DynamicBytes BytesDynamic(byte[] b) {
        return new DynamicBytes(b);
    }

    public static Utf8String String(String s) {
        return new Utf8String(s);
    }

    public static StaticArray StaticArray(List<Type> array) {
        Integer length = array.size();
        if (length <= 0 || length > 32) {
            throw new UnsupportedOperationException("array length M must in range 0 < M <= 32");
        }
        if (length == 1) {
            return new StaticArray1(array);
        }
        if (length == 2) {
            return new StaticArray2(array);
        }
        if (length == 3) {
            return new StaticArray3(array);
        }
        if (length == 4) {
            return new StaticArray4(array);
        }
        if (length == 5) {
            return new StaticArray5(array);
        }
        if (length == 6) {
            return new StaticArray6(array);
        }
        if (length == 7) {
            return new StaticArray7(array);
        }
        if (length == 8) {
            return new StaticArray8(array);
        }
        if (length == 9) {
            return new StaticArray9(array);
        }
        if (length == 10) {
            return new StaticArray10(array);
        }
        if (length == 11) {
            return new StaticArray11(array);
        }
        if (length == 12) {
            return new StaticArray12(array);
        }
        if (length == 13) {
            return new StaticArray13(array);
        }
        if (length == 14) {
            return new StaticArray14(array);
        }
        if (length == 15) {
            return new StaticArray15(array);
        }
        if (length == 16) {
            return new StaticArray16(array);
        }
        if (length == 17) {
            return new StaticArray17(array);
        }
        if (length == 18) {
            return new StaticArray18(array);
        }
        if (length == 19) {
            return new StaticArray19(array);
        }
        if (length == 20) {
            return new StaticArray20(array);
        }
        if (length == 21) {
            return new StaticArray21(array);
        }
        if (length == 22) {
            return new StaticArray22(array);
        }
        if (length == 23) {
            return new StaticArray23(array);
        }
        if (length == 24) {
            return new StaticArray24(array);
        }
        if (length == 25) {
            return new StaticArray25(array);
        }
        if (length == 26) {
            return new StaticArray26(array);
        }
        if (length == 27) {
            return new StaticArray27(array);
        }
        if (length == 28) {
            return new StaticArray28(array);
        }
        if (length == 29) {
            return new StaticArray29(array);
        }
        if (length == 30) {
            return new StaticArray30(array);
        }
        if (length == 31) {
            return new StaticArray31(array);
        }
        if (length == 32) {
            return new StaticArray32(array);
        }

        throw new UnsupportedOperationException("array length M must in range 0 < M <= 32");
    }

    public static DynamicArray DynamicArray(List<Type> array) {
        return new DynamicArray(array);
    }

}
