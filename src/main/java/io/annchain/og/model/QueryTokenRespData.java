package io.annchain.og.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryTokenRespData {
    @JSONField(name = "token_id")
    Integer tokenID;

    @JSONField(name = "name")
    String name;

    @JSONField(name = "symbol")
    String symbol;

    @JSONField(name = "issuer")
    String issuer;

    @JSONField(name = "re_issuable")
    Boolean reIssuable;

    @JSONField(name = "issues")
    String[] issues;

    @JSONField(name = "destroyed")
    Boolean destroyed;
}
