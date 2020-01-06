import com.github.annchain.sdk.main.OG;

import com.github.annchain.sdk.model.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint64;
import com.github.annchain.sdk.types.Account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

//@Slf4j
public class OGApiTest {

    private static final String OG_URL = "http://172.28.152.101:30066";
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
        testToken();

    }

    @Test
    public void testQueryNonce() throws IOException {
        QueryNonceResp resp = og.QueryNonce(account.GetAddress());
        System.out.println("testQueryNonce: " + resp.getNonce());
    }

    @Test
    public void testQueryBalance() throws IOException {
        QueryBalanceResp balanceResp = og.QueryBalance(account.GetAddress(), 0);
        System.out.println("testQueryBalance: " + balanceResp.getData().getBalance());
    }

    @Test
    public void testSendTransaction() throws IOException, InterruptedException {
        Integer interval = 100;
        Integer timeout = 7000;
        ConfirmCallback confirmCallback = new ConfirmCallback(og, og.DEFAULT_TOKENID, interval, timeout);

        BigInteger value = new BigInteger("100");
        QueryBalanceResp queryBalanceResp = og.QueryBalance(account.GetAddress(), og.DEFAULT_TOKENID);
        BigInteger originBalance = new BigInteger(queryBalanceResp.getData().getBalance());
        BigInteger expectBalance = originBalance.subtract(value);

        new Thread(() -> {
            try {
                og.SendTransactionAndJoin(account, account2.GetAddress(), value, confirmCallback);
                QueryBalanceResp queryBalanceResp1 = og.QueryBalance(account.GetAddress(), og.DEFAULT_TOKENID);
                BigInteger newBalance = new BigInteger(queryBalanceResp1.getData().getBalance());

                assertEquals(expectBalance.toString(), newBalance.toString());
            } catch (IOException | InterruptedException e) {
                // do something
            }
        }).start();

        // wait for confirm
        Thread.sleep(10000);
    }

    @Test
    public void testToken() throws IOException, InterruptedException {
        BigInteger value = new BigInteger("1234567");
        String tokenName = "testT";
        SendTransactionResp sendTransactionResp = og.InitialTokenOffering(account, value, true, tokenName);
        assertEquals(og.EMPTY_ERROR, sendTransactionResp.getErr());
        String hash = sendTransactionResp.getHash();
        System.out.println("initial offering hash: " + hash);

        Long timeout = 30000L;
        Long interval = 100L;
        QueryReceiptResp receiptResp = queryReceiptUntilTimeout(hash, timeout, interval);
        assertNotNull(receiptResp);
        assertEquals(og.RECEIPT_STATUS_SUCCESS, receiptResp.getData().getStatus());

        Integer tokenID = Integer.parseInt(receiptResp.getData().getResult());
        assertNotNull(tokenID);

        // check initial balance
        QueryBalanceResp balanceResp = og.QueryBalance(account.GetAddress(), tokenID);
        assertEquals(og.EMPTY_ERROR, balanceResp.getErr());
        assertEquals(value.toString(), balanceResp.getData().getBalance());
        System.out.println("initial balance confirmed");

        // check token info
        QueryTokenResp tokenResp = og.QueryToken(tokenID);
        assertEquals(og.EMPTY_ERROR, tokenResp.getErr());
        assertEquals( tokenName, tokenResp.getData().getName());
        assertEquals(value.toString(), tokenResp.getData().getIssues()[0]);
        System.out.println("token info confirmed");

        // check token transfer
        BigInteger transferValue = new BigInteger("567");
        SendTransactionResp transferTokenResp = og.TransferToken(account, account2.GetAddress(), tokenID, transferValue);
        assertEquals(og.EMPTY_ERROR, tokenResp.getErr());
        hash = transferTokenResp.getHash();
        receiptResp = queryReceiptUntilTimeout(hash, timeout, interval);
        assertNotNull(receiptResp);
        assertEquals(og.RECEIPT_STATUS_SUCCESS, receiptResp.getData().getStatus());

        QueryBalanceResp balanceResp1 = og.QueryBalance(account.GetAddress(), tokenID);
        QueryBalanceResp balanceResp2 = og.QueryBalance(account2.GetAddress(), tokenID);
        assertEquals(value.subtract(transferValue).toString(), balanceResp1.getData().getBalance());
        assertEquals(transferValue.toString(), balanceResp2.getData().getBalance());
    }

    @Test
    public void testContract() throws IOException, InterruptedException {
        String contractBytecode = "608060405234801561001057600080fd5b5060405160208061026b8339810180604052810190808051906020019092919050505033600060086101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550806000806101000a81548167ffffffffffffffff021916908367ffffffffffffffff160217905550506101bf806100ac6000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680632986c0e51461005c5780632e4176cf1461009b578063a9eb91ec146100f2575b600080fd5b34801561006857600080fd5b50610071610129565b604051808267ffffffffffffffff1667ffffffffffffffff16815260200191505060405180910390f35b3480156100a757600080fd5b506100b0610142565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3480156100fe57600080fd5b50610127600480360381019080803567ffffffffffffffff169060200190929190505050610168565b005b6000809054906101000a900467ffffffffffffffff1681565b600060089054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b806000806101000a81548167ffffffffffffffff021916908367ffffffffffffffff160217905550505600a165627a7a72305820a5e8ed0687032762681e3e33598a02dcc24631ee04ae3feb01b1181bc355dc8f0029";
        BigInteger value = new BigInteger("0");
        long constructIndex = 8L;

        List<Type> constructParameters = new ArrayList<>();
        constructParameters.add(new Uint64(constructIndex));

        SendTransactionResp deployResp = og.DeployContract(account, value, contractBytecode, constructParameters);
        assertEquals(og.EMPTY_ERROR, deployResp.getErr());

        String hash = deployResp.getHash();
        System.out.println("deploy contract hash: " + hash);

        Long timeout = 30000L;
        Long interval = 100L;
        QueryReceiptResp receiptResp = queryReceiptUntilTimeout(hash, timeout, interval);
        assertNotNull(receiptResp);
        assertEquals(og.RECEIPT_STATUS_SUCCESS, receiptResp.getData().getStatus());

        String contractAddress = receiptResp.getData().getResult();
        System.out.println("contract deployed address: " + contractAddress);

        // check construction and variable
        List<TypeReference<?>> outputRef = new ArrayList<>();
        TypeReference<?> ref = TypeReference.create(Uint64.class);
        outputRef.add(ref);

        QueryContractResp queryContractResp = og.QueryContract(contractAddress, "index", new ArrayList<>(), outputRef);
        assertEquals(og.EMPTY_ERROR, queryContractResp.getErr());
        Type indexResult = queryContractResp.getOutputs().get(0);
        Uint64 index = (Uint64) indexResult;
        assertEquals(constructIndex, index.getValue().longValue());
        System.out.println("index confirmed, index: " + index.getValue().toString());

        // check index change
        long newIndex = 100L;
        String funcName = "setIndex";
        List<Type> inputs = new ArrayList<>();
        inputs.add(new Uint64(newIndex));
        List<TypeReference<?>> outputParams = new ArrayList<>();

        SendTransactionResp callContractResp = og.CallContract(account, contractAddress, value, funcName, inputs, outputParams);
        assertEquals(og.EMPTY_ERROR, callContractResp.getErr());
        hash = callContractResp.getHash();
        receiptResp = queryReceiptUntilTimeout(hash, timeout, interval);
        assertNotNull(receiptResp);

        queryContractResp = og.QueryContract(contractAddress, "index", new ArrayList<>(), outputRef);
        assertEquals(og.EMPTY_ERROR, queryContractResp.getErr());
        indexResult = queryContractResp.getOutputs().get(0);
        index = (Uint64) indexResult;
        assertEquals(newIndex, index.getValue().longValue());
        System.out.println("setIndex confirmed, index: " + index.getValue().toString());

    }

    private QueryReceiptResp queryReceiptUntilTimeout(String hash, Long timeout, Long interval) throws IOException, InterruptedException {
        Long timeoutTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < timeoutTime) {
            QueryReceiptResp queryReceiptResp = og.QueryReceipt(hash);
            if (queryReceiptResp.getData() != null) {
                return queryReceiptResp;
            }
            Thread.sleep(interval);
        }
        return null;
    }

}
