package io.annchain.og.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class SendTransactionResp {
    @JSONField(name = "data")
    String hash;

    String err;
}
