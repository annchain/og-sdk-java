package io.annchain.og.sdk.server;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

public class OGRequestGET {

    private String method;
    private JSONObject variables;

    public OGRequestGET() {
        this.variables = new JSONObject();
    }

    public void SetVariable(String key, String value) {
        this.variables.put(key, value);
    }

    public String ToString() {
        ArrayList<String> urlSet = new ArrayList<String>();
        for (String key : this.variables.keySet()) {
            Object value = this.variables.get(key);
            urlSet.add(key + "=" + value.toString());
        }

        String url = String.join("&", urlSet);
        return url;
    }


}
