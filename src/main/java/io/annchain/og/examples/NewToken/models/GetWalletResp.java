package io.annchain.og.examples.NewToken.models;

import lombok.Data;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.util.List;

@Data
public class GetWalletResp {
    List<Bytes32> hashes;
}
