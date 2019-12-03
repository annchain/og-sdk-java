package io.annchain.og.model;

import lombok.Data;

@Data
public class QueryTransactionResp {
    QueryTransactionRespData data;
    String err;
}
