package io.annchain.og.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionResp {
    QueryTransactionRespData data;
    String err;
}
