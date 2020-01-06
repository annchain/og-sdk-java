package io.annchain.og.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionListResp {

    QueryTransactionListRespData data;

    String err;
}
