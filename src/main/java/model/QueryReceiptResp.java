package model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryReceiptResp {
    QueryReceiptRespData data;
    String err;
}
