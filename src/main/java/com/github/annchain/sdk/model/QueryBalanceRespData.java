package com.github.annchain.sdk.model;

import lombok.Data;

@Data
public class QueryBalanceRespData {
    String address;
    String balance;
}
