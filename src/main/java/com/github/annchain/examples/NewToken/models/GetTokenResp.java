package com.github.annchain.examples.NewToken.models;

import lombok.Data;
import org.web3j.abi.datatypes.DynamicBytes;

@Data
public class GetTokenResp {
    DynamicBytes data;
}
