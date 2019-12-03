package io.annchain.og.main;

import io.annchain.og.model.ContractCallbackResp;

public interface IContractCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ContractEvent(ContractCallbackResp result);
}
