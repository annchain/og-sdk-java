package com.github.annchain.sdk.types;

import java.math.BigInteger;

public class TX_Action {
    public Account account;

    public Long nonce;
    public String fromAddress;
    public BigInteger value;
    public Integer tokenID;

    public final String cryptoType = "secp256k1";

    public Short actionInitialOffering = 0;
    public Short actionDestroy = 1;
    public Short actionSecondOffering = 2;

    public Boolean additionalIssueOn = true;
    public Boolean additionalIssueOff = false;

    public Short additionalIssuaToShort(Boolean a) {
        if (a) {
            return 1;
        }
        return 0;
    }
}
