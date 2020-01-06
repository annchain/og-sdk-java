package io.annchain.og.sdk.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class QueryTransactionRespDataSeq {
    String hash;

    String[] parents;

    String issuer;

    Long nonce;

    Long Height;

    Long weight;

    @JSONField(name = "bls_joint_sig")
    String blsSignature;

    @JSONField(name = "bls_joint_pub_key")
    String blsPublicKey;
}
