package com.github.annchain.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryBalanceResp {
    @JSONField(name = "data")
    QueryBalanceRespData data;
    String err;
}
