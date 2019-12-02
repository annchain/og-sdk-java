import main.IConfirmCallback;
import main.OG;
import model.ConfirmCallbackResp;
import model.QueryBalanceResp;
import model.QueryTransactionResp;
import model.QueryTransactionRespDataTx;

import java.io.IOException;

//@Slf4j
public class ConfirmCallback implements IConfirmCallback {

    private OG og;

    private Integer tokenID;
    private Integer interval;
    private Integer timeout;

    public ConfirmCallback(OG og, Integer tokenID, Integer interval, Integer timeout) {
        this.og = og;

        this.tokenID = tokenID;
        this.interval = interval;
        this.timeout = timeout;
    }

    public Integer IntervalTime() {
        return this.interval;
    }

    public Integer Timeout() {
        return this.timeout;
    }

    public void ConfirmEvent(ConfirmCallbackResp result) {
        System.out.println("confirm hash: " + result.getHash());
        System.out.println("confirm data: " + result.getData());
        System.out.println("confirm err: " + result.getErr());

        if (!result.getErr().equals("")) {
            return;
        }
        String hash = result.getHash();
        try {
            QueryTransactionResp txResp = og.QueryTransaction(hash);
            QueryTransactionRespDataTx tx = txResp.getData().getTransaction();

            QueryBalanceResp balanceResp = og.QueryBalance(tx.getFrom(), this.tokenID);
            System.out.println("confirm balance: " + balanceResp.getData().getBalance());
        } catch (IOException e) {
            System.out.println("throw IOException");
        }
    }

}
