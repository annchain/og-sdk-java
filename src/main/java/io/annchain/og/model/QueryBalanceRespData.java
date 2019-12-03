package io.annchain.og.model;

import lombok.Data;

@Data
public class QueryBalanceRespData {
    String address;
    String balance;
}
