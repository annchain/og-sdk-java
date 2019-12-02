package main;

import model.ContractCallbackResp;

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
