package model;

import lombok.Data;

@Data
public class ConfirmCallbackResp {
    String hash;
    String data;
    String err;
}