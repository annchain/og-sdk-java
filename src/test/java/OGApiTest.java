import lombok.extern.slf4j.Slf4j;
import model.QueryBalanceResp;
import model.QueryNonceResp;
import model.SendTransactionResp;
import org.junit.BeforeClass;
import org.junit.Test;
import types.Account;

import java.io.IOException;
import java.math.BigInteger;

//@Slf4j
public class OGApiTest {

    private static final String OG_URL = "http://localhost:8000";
    private static OG og;
    private static Account account;
    private static Account account2;
    private static final String PRIVATE_KEY     = "0000000000000000000000000000000000000000000000000000000000000001";
    private static final String PRIVATE_KEY2    = "0000000000000000000000000000000000000000000000000000000000000002";


    @BeforeClass
    public static void init() {
        og = new OG(OG_URL);
        account = new Account(PRIVATE_KEY);
        account2 = new Account(PRIVATE_KEY2);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        testQueryNonce();
        testQueryBalance();
        testSendTransaction();
    }

    public void testQueryNonce() throws IOException {
        QueryNonceResp resp = og.QueryNonce(account.GetAddress());
        System.out.println("testQueryNonce: " + resp.getNonce());
    }

    public void testQueryBalance() throws IOException {
        QueryBalanceResp balanceResp = og.QueryBalance(account.GetAddress(), 0);
        System.out.println("testQueryBalance: " + balanceResp.getData().getBalance());
    }

    public void testSendTransaction() {
        Integer interval = 100;
        Integer timeout = 7000;
        ConfirmCallback confirmCallback = new ConfirmCallback(og, 0, interval, timeout);

        BigInteger value = new BigInteger("100");
        new Thread(() -> {
            try {
                og.SendTransactionAsync(account, account2.GetAddress(), value, confirmCallback);
            } catch (IOException | InterruptedException e) {
                // do something
            }
        }).start();
    }

    public void testToken() throws IOException, InterruptedException {
        BigInteger value = new BigInteger("1234567");
//        og.InitialTokenOfferingAsync(account, value, true, "testT");

    }

}
