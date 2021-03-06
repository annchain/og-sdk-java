package com.github.annchain.sdk.main;

import com.github.annchain.sdk.model.ConfirmCallbackResp;

public interface IConfirmCallback {
    /**
     * Return the frequency time to check if a tx is confirmed.
     * Time unit: Millisecond.
     * */
    Integer IntervalTime();

    /**
     * Return the longest confirm time to wait
     * */
    Integer Timeout();

    void ConfirmEvent(ConfirmCallbackResp result);
}
