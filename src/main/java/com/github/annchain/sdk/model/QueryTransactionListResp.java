package com.github.annchain.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionListResp {

    QueryTransactionListRespData data;

    String err;
}
