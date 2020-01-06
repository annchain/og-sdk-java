package com.github.annchain.sdk.model;

import lombok.Data;
import org.web3j.abi.datatypes.Type;
import java.util.List;

@Data
public class ContractCallbackResp {
    String hash;
    List<Type> data;
    String err;
}
