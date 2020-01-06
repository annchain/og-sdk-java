package com.github.annchain.examples.NewToken.models;

import lombok.Data;
import org.web3j.abi.datatypes.generated.Bytes32;

@Data
public class IssueTokenResp {
    Bytes32 hash;
}
