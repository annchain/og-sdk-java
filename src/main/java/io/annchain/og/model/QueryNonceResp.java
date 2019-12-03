package io.annchain.og.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryNonceResp {
    @JSONField(name = "data")
    Long nonce;
    String err;
}
