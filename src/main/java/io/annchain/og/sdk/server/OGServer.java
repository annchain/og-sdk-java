package io.annchain.og.sdk.server;

import okhttp3.*;

import java.io.IOException;

public class OGServer {

    private String url;
    private OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public OGServer(String url) {
        this.url = url;
        this.client = new OkHttpClient();
    }

    public String Get(String rpcMethod, OGRequestGET req) throws IOException {
        Request request = new Request.Builder()
                .url(this.url + "/" + rpcMethod + "?" + req.ToString())
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String Post(String rpcMethod, OGRequestPOST req) throws IOException {
        RequestBody body = RequestBody.create(JSON, req.ToJSONString());
        Request request = new Request.Builder()
                .url(this.url + "/" + rpcMethod)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
