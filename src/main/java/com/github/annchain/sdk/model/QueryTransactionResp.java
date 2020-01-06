package com.github.annchain.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionResp {
    QueryTransactionRespData data;
    String err;
}
