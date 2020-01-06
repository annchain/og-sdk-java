package com.github.annchain.sdk.server;

import com.alibaba.fastjson.JSONObject;

public class OGRequestPOST {

    private JSONObject variables;

    public OGRequestPOST() {
        this.variables = new JSONObject();
    }

    public void SetVariable(String key, Object value) {
        this.variables.put(key, value);
    }

    public String ToJSONString() {
        return this.variables.toJSONString();
    }
}
