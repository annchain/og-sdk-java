package io.annchain.og.examples.Ledger;

import io.annchain.og.examples.Ledger.models.GetIncomingResp;
import io.annchain.og.sdk.main.OG;
import io.annchain.og.sdk.model.QueryContractResp;
import io.annchain.og.sdk.model.QueryReceiptResp;
import io.annchain.og.sdk.model.SendTransactionResp;
import io.annchain.og.sdk.types.Account;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Ledger {

    private static Long timeout = 5000L;
    private static Long interval = 100L;

    private OG og;
    private String contractAddress;

    Ledger(OG og) {
        this.og = og;
    }

    public List<Account> createAccounts(Integer num) {
        List<Account> accounts = new ArrayList(num);
        for (int i = 0; i < num; i++) {
            System.out.println("000000000000000000000000000000000000000000000000000000000000000" + (i+1));
            accounts.add(new Account("000000000000000000000000000000000000000000000000000000000000000" + (i+1)));
        }
        return accounts;
    }

    public String deployContract(Account issuer, String bytecode, BigInteger value, List<Type> constructorPrams) throws IOException, InterruptedException {
        SendTransactionResp deployResp = og.DeployContract(issuer, value, bytecode,constructorPrams);
        if (!deployResp.getErr().equals("")) {
            System.out.println("deploy err: " + deployResp.getErr());
            return null;
        }
        System.out.println("deployed: " + deployResp.getHash());
        QueryReceiptResp receiptResp = og.QueryReceiptUntilTimeout(deployResp.getHash(), timeout, interval);
        if (receiptResp == null) {
            System.out.println("contract null");
            return null;
        }
        System.out.println("contract address: " + receiptResp.getData().getResult());

        String contractAddress = receiptResp.getData().getResult();
        this.contractAddress = contractAddress;
        return contractAddress;
    }

    private String callContract(Account issuer, BigInteger value, String funcName, List<Type> inputs, List<TypeReference<?>> outputParams) throws IOException, InterruptedException {
        SendTransactionResp callContractResp = og.CallContract(issuer, this.contractAddress, value, funcName, inputs, outputParams);
        if (!callContractResp.getErr().equals("")) {
            System.out.println("call contract err: " + callContractResp.getErr());
            return null;
        }

        QueryReceiptResp receiptResp = og.QueryReceiptUntilTimeout(callContractResp.getHash(), timeout, interval);
        if (!receiptResp.getErr().equals("")) {
            System.out.println("query receipt err: " + receiptResp.getErr());
            return null;
        }
        return receiptResp.getData().getResult();
    }

    public void setMerchant(Account issuer, String merchant, Integer share) throws IOException, InterruptedException {
        String funcName = "setMerchant";

        List<Type> inputs = new ArrayList<>();
        inputs.add(new Utf8String(merchant));
        inputs.add(new Uint(new BigInteger(share.toString())));

        List<TypeReference<?>> outputParams = new ArrayList<>();

        callContract(issuer, new BigInteger("0"), funcName, inputs, outputParams);
    }

    public void pay(Account issuer, Integer cash) throws IOException, InterruptedException {
        String funcName = "pay";

        List<Type> inputs = new ArrayList<>();
        inputs.add(new Uint(new BigInteger(cash.toString())));

        List<TypeReference<?>> outputParams = new ArrayList<>();

        callContract(issuer, new BigInteger("0"), funcName, inputs, outputParams);
    }

    public void clear(Account issuer) throws IOException, InterruptedException {
        String funcName = "clear";
        List<Type> inputs = new ArrayList<>();
        List<TypeReference<?>> outputParams = new ArrayList<>();

        callContract(issuer, new BigInteger("0"), funcName, inputs, outputParams);
    }

    public GetIncomingResp getIncoming(String merchant) throws IOException, InterruptedException {
        String funcName = "getIncoming";

        List<Type> inputs = new ArrayList<>();
        inputs.add(new Utf8String(merchant));

        List<TypeReference<?>> outputParams = new ArrayList<>();
        outputParams.add(TypeReference.create(Uint.class));

        QueryContractResp queryContractResp = og.QueryContract(contractAddress, funcName, inputs, outputParams);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("contract error: " + queryContractResp.getErr());
            return null;
        }

        List<Type> outputs = queryContractResp.getOutputs();
        Uint incoming = (Uint) outputs.get(0);

        GetIncomingResp resp = new GetIncomingResp();
        resp.setIncoming(incoming);
        return resp;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        String url = "http://localhost:8000";
        OG og = new OG(url);
        Ledger demo = new Ledger(og);

        // init basic accounts set
        List<Account> accounts = demo.createAccounts(5);
        Account issuer = accounts.get(0);
        Account account2 = accounts.get(1);
        Account account3 = accounts.get(2);

        Thread.sleep(2000);     // wait for accounts initialize been confirmed.

        String bytecode = "608060405234801561001057600080fd5b5033600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550610723806100616000396000f3fe608060405260043610610078576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806352efea6e1461007d578063668fdb8b14610094578063707aef57146100e3578063989d0cd4146101bf578063c290d69114610291578063de963ff1146102cc575b600080fd5b34801561008957600080fd5b5061009261031b565b005b3480156100a057600080fd5b506100cd600480360360208110156100b757600080fd5b810190808035906020019092919050505061041e565b6040518082815260200191505060405180910390f35b3480156100ef57600080fd5b506101a96004803603602081101561010657600080fd5b810190808035906020019064010000000081111561012357600080fd5b82018360208201111561013557600080fd5b8035906020019184600183028401116401000000008311171561015757600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610436565b6040518082815260200191505060405180910390f35b3480156101cb57600080fd5b5061028f600480360360408110156101e257600080fd5b81019080803590602001906401000000008111156101ff57600080fd5b82018360208201111561021157600080fd5b8035906020019184600183028401116401000000008311171561023357600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290803590602001909291905050506104ca565b005b34801561029d57600080fd5b506102ca600480360360208110156102b457600080fd5b8101908080359060200190929190505050610627565b005b3480156102d857600080fd5b50610305600480360360208110156102ef57600080fd5b810190808035906020019092919050505061063a565b6040518082815260200191505060405180910390f35b60008090505b6004805490508160ff16101561041357600060048260ff1681548110151561034557fe5b9060005260206000200160405160200180828054600181600116156101000203166002900480156103ad5780601f1061038b5761010080835404028352918201916103ad565b820191906000526020600020905b815481529060010190602001808311610399575b50509150506040516020818303038152906040528051906020012090506003546001600083815260200190815260200160002054600254028115156103ee57fe5b0460008083815260200190815260200160002081905550508080600101915050610321565b506000600281905550565b60006020528060005260406000206000915090505481565b600080826040516020018082805190602001908083835b602083101515610472578051825260208201915060208101905060208303925061044d565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405160208183030381529060405280519060200120905060008082815260200190815260200160002054915050919050565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561052657600080fd5b6000826040516020018082805190602001908083835b602083101515610561578051825260208201915060208101905060208303925061053c565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040516020818303038152906040528051906020012090506000600160008381526020019081526020016000205414156106225781600160008381526020019081526020016000208190555081600360008282540192505081905550600483908060018154018082558091505090600182039060005260206000200160009091929091909150908051906020019061061f929190610652565b50505b505050565b8060026000828254019250508190555050565b60016020528060005260406000206000915090505481565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061069357805160ff19168380011785556106c1565b828001600101855582156106c1579182015b828111156106c05782518255916020019190600101906106a5565b5b5090506106ce91906106d2565b5090565b6106f491905b808211156106f05760008160009055506001016106d8565b5090565b9056fea165627a7a7230582063f754c90b69a682c52e6dc59954214a9618da9fb58bee9ee6bd2b77d4422e1f0029";
        BigInteger value = new BigInteger("0");
        String contractAddress = demo.deployContract(issuer, bytecode, value, null);

        // setMerchant
        demo.setMerchant(issuer, account2.GetAddress(), 75);
        demo.setMerchant(issuer, account3.GetAddress(), 25);

        // add pay
        demo.pay(issuer, 100);
        demo.clear(issuer);

        // get incoming
        GetIncomingResp incomingResp2 = demo.getIncoming(account2.GetAddress());
        GetIncomingResp incomingResp3 = demo.getIncoming(account3.GetAddress());

        System.out.println("account2 incoming: " + incomingResp2.getIncoming().getValue().toString());
        System.out.println("account3 incoming: " + incomingResp3.getIncoming().getValue().toString());

        // add pay
        demo.pay(issuer, 200);
        demo.pay(issuer, 100);
        demo.clear(issuer);

        // retry incoming
        incomingResp2 = demo.getIncoming(account2.GetAddress());
        incomingResp3 = demo.getIncoming(account3.GetAddress());

        System.out.println("account2 new incoming: " + incomingResp2.getIncoming().getValue().toString());
        System.out.println("account3 new incoming: " + incomingResp3.getIncoming().getValue().toString());

    }

}
