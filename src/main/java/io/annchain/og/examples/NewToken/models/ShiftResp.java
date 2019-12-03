package io.annchain.og.examples.NewToken.models;

import lombok.Data;
import org.web3j.abi.datatypes.generated.Bytes32;

@Data
public class ShiftResp {
    Bytes32 newHash;
}
