package server;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OGRequestPOST {

    private Map<String, Object> variables;

    public OGRequestPOST() {
        this.variables = new HashMap();
    }

    public void SetVariable(String key, String value) {
        this.variables.put(key, value);
    }

    public String ToJSONString() {
        JSONObject json = new JSONObject(this.variables);
        return json.toJSONString();
    }
}
