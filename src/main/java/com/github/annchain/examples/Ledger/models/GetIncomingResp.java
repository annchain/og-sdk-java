package com.github.annchain.examples.Ledger.models;

import lombok.Data;
import org.web3j.abi.datatypes.Uint;

@Data
public class GetIncomingResp {
    Uint incoming;
}
