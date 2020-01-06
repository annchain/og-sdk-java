package com.github.annchain.sdk.main;

import com.github.annchain.sdk.model.ContractCallbackResp;

public interface IContractCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTimeMillis();

    /**
     * Return the longest confirm time to wait
     * */
    Integer TimeoutMillis();

    void ContractEvent(ContractCallbackResp result);
}
