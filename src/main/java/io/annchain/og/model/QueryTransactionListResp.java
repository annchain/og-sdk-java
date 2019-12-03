package io.annchain.og.model;

import lombok.Data;

@Data
public class QueryTransactionListResp {

    QueryTransactionListRespData data;

    String err;
}
