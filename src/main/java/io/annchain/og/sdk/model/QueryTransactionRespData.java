package io.annchain.og.sdk.model;

import lombok.Data;

@Data
public class QueryTransactionRespData {

    Short type;

    QueryTransactionRespDataTx transaction;

    QueryTransactionRespDataSeq sequencer;
}
