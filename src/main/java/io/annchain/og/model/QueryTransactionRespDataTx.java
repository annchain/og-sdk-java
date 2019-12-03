package io.annchain.og.model;

import lombok.Data;

@Data
public class QueryTransactionRespDataTx {

    String hash;

    String[] parents;

    String from;

    String to;

    Long nonce;

    String value;

    Long weight;
}
