package io.annchain.og.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryReceiptRespData {
    @JSONField(name = "tx_hash")
    String hash;

    @JSONField(name = "status")
    Integer status;

    @JSONField(name = "result")
    String result;

    @JSONField(name = "contract_address")
    String contractAddr;
}
