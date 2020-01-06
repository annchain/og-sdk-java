package io.annchain.og.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionListRespData {

    Integer total;

    QueryTransactionRespData[] txs;
}
