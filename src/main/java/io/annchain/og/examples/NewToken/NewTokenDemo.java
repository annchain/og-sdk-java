package io.annchain.og.examples.NewToken;

import io.annchain.og.examples.NewToken.models.GetTokenResp;
import io.annchain.og.examples.NewToken.models.GetWalletResp;
import io.annchain.og.examples.NewToken.models.IssueTokenResp;
import io.annchain.og.examples.NewToken.models.ShiftResp;
import io.annchain.og.main.OG;
import io.annchain.og.model.QueryContractResp;
import io.annchain.og.model.QueryReceiptResp;
import io.annchain.og.model.SendTransactionResp;
import io.annchain.og.types.Account;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NewTokenDemo {

    private static Long timeout = 5000L;
    private static Long interval = 100L;

    private OG og;
    private String contractAddress;

    NewTokenDemo(OG og) {
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

    public void tranferBasicValue(List<Account> accounts, BigInteger value) throws IOException {
        for (int i = 1; i < accounts.size(); i++) {
            og.SendTransaction(accounts.get(0), accounts.get(i).GetAddress(), value);
        }
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

    public IssueTokenResp issueToken(Account issuer, String toUser, byte[] data) throws IOException, InterruptedException {
        String funcName = "issueToken";

        Address rawToUser = new Address(toUser);
        DynamicBytes rawData = new DynamicBytes(data);

        List<Type> inputs = new ArrayList<Type>();
        inputs.add(rawToUser);
        inputs.add(rawData);

        List<TypeReference<?>> outputParams = new ArrayList<>();
        outputParams.add(TypeReference.create(Bytes32.class));

        Function func = new Function(funcName, inputs, outputParams);
        String rawOutputs = this.callContract(issuer, new BigInteger("0"), funcName, inputs, outputParams);

        List<Type> outputs = FunctionReturnDecoder.decode(rawOutputs, func.getOutputParameters());

        IssueTokenResp resp = new IssueTokenResp();
        resp.setHash((Bytes32) outputs.get(0));
        return resp;
    }

    public GetWalletResp getWallet(String user) throws IOException {
        String funcName = "getWallet";

        Address rawUser = new Address(user);

        List<Type> inputs = new ArrayList<Type>();
        inputs.add(rawUser);

        List<TypeReference<?>> outputParams = new ArrayList<>();
        outputParams.add(new TypeReference<DynamicArray<Bytes32>>() {});

        QueryContractResp queryContractResp = og.QueryContract(contractAddress, funcName, inputs, outputParams);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("contract error: " + queryContractResp.getErr());
            return null;
        }

        List<Type> respOutputs = queryContractResp.getOutputs();
        DynamicArray<Bytes32> dynamicArray = (DynamicArray<Bytes32>) respOutputs.get(0);
        List<Bytes32> bts = dynamicArray.getValue();
        for (Bytes32 bt: bts) {
            System.out.println("--- bt: " + Hex.toHexString(bt.getValue()));
        }

        GetWalletResp resp = new GetWalletResp();
        resp.setHashes(bts);
        return resp;
    }

    public GetTokenResp getToken(String user, byte[] hash) throws IOException {
        String funName = "getToken";

        List<Type> inputs = new ArrayList<Type>();
        inputs.add(new Address(user));
        inputs.add(new Bytes32(hash));

        List<TypeReference<?>> outputParams = new ArrayList<TypeReference<?>>();
        outputParams.add(new TypeReference<DynamicBytes>() {});

        QueryContractResp queryContractResp = og.QueryContract(contractAddress, funName, inputs, outputParams);
        if (!queryContractResp.getErr().equals("")) {
            System.out.println("query contract getToken error: " + queryContractResp.getErr());
            return null;
        }
        List<Type> outputs = queryContractResp.getOutputs();
        DynamicBytes o = (DynamicBytes) outputs.get(0);
        System.out.println("o: " + Hex.toHexString(o.getValue()));

        GetTokenResp resp = new GetTokenResp();
        resp.setData(o);
        return resp;
    }

    public ShiftResp shift(Account issuer, String fromUser, String toUser, byte[] hash) throws IOException, InterruptedException {
        String funcName = "shift";

        List<Type> inputs = new ArrayList<Type>();
        inputs.add(new Address(fromUser));
        inputs.add(new Address(toUser));
        inputs.add(new Bytes32(hash));

        List<TypeReference<?>> outputParams = new ArrayList<TypeReference<?>>();
        outputParams.add(new TypeReference<Bytes32>() {});

        String rawOutputs = this.callContract(issuer, new BigInteger("0"), funcName, inputs, outputParams);
        Function func = new Function(funcName, inputs, outputParams);

        List<Type> outputs = FunctionReturnDecoder.decode(rawOutputs, func.getOutputParameters());
        Bytes32 newHash = (Bytes32) outputs.get(0);

        ShiftResp resp = new ShiftResp();
        resp.setNewHash(newHash);
        return resp;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        String url = "http://localhost:8000";
        OG og = new OG(url);
        NewTokenDemo demo = new NewTokenDemo(og);

        // init basic accounts set
        List<Account> accounts = demo.createAccounts(5);
        BigInteger value = new BigInteger("10000");
        demo.tranferBasicValue(accounts, value);

        Thread.sleep(2000);     // wait for accounts initialize been confirmed.

        Account issuer = accounts.get(0);
        String account2 = accounts.get(1).GetAddress();
        String account3 = accounts.get(2).GetAddress();

        // deploy contract
        String bytecode = "608060405234801561001057600080fd5b5033600160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506110e9806100616000396000f3fe6080604052600436106100a4576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806304d0a647146100a957806309f98da11461014f578063155bf4e2146101de5780632dc779b91461029257806392bd8f43146103665780639eba96c4146103d5578063a87430ba14610430578063e2f0e58a14610495578063f1a3b94314610591578063f540350a146105cc575b600080fd5b3480156100b557600080fd5b506100f8600480360360208110156100cc57600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610638565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b8381101561013b578082015181840152602081019050610120565b505050509050019250505060405180910390f35b34801561015b57600080fd5b506101c86004803603606081101561017257600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506106d1565b6040518082815260200191505060405180910390f35b3480156101ea57600080fd5b506102176004803603602081101561020157600080fd5b8101908080359060200190929190505050610791565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561025757808201518184015260208101905061023c565b50505050905090810190601f1680156102845780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561029e57600080fd5b506102eb600480360360408110156102b557600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506107a4565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561032b578082015181840152602081019050610310565b50505050905090810190601f1680156103585780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561037257600080fd5b506103bf6004803603604081101561038957600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061090a565b6040518082815260200191505060405180910390f35b3480156103e157600080fd5b5061042e600480360360408110156103f857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061091f565b005b34801561043c57600080fd5b5061047f6004803603602081101561045357600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610aec565b6040518082815260200191505060405180910390f35b3480156104a157600080fd5b5061057b600480360360408110156104b857600080fd5b81019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001906401000000008111156104f557600080fd5b82018360208201111561050757600080fd5b8035906020019184600183028401116401000000008311171561052957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600081840152601f19601f820116905080830192505050505050509192919290505050610b0a565b6040518082815260200191505060405180910390f35b34801561059d57600080fd5b506105ca600480360360208110156105b457600080fd5b8101908080359060200190929190505050610dff565b005b3480156105d857600080fd5b506105e1610f39565b6040518080602001828103825283818151815260200191508051906020019060200280838360005b83811015610624578082015181840152602081019050610609565b505050509050019250505060405180910390f35b60606000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002018054806020026020016040519081016040528092919081815260200182805480156106c557602002820191906000526020600020905b8154815260200190600101908083116106b1575b50505050509050919050565b6000600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16148061075a57508373ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b151561076557600080fd5b606061077185846107a4565b905061077d858461091f565b6107878482610b0a565b9150509392505050565b606061079d33836107a4565b9050919050565b60606000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600083815260200190815260200160002060020160009054906101000a900460ff16151561081457600080fd5b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160008381526020019081526020016000206001018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156108fd5780601f106108d2576101008083540402835291602001916108fd565b820191906000526020600020905b8154815290600101906020018083116108e057829003601f168201915b5050505050905092915050565b60006109173384846106d1565b905092915050565b600160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614806109a657508173ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16145b15156109b157600080fd5b60006001026000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002016000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000154815481101515610a5957fe5b90600052602060002001819055506000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016000828152602001908152602001600020600080820160009055600182016000610ad29190610fd0565b6002820160006101000a81549060ff021916905550505050565b60006020528060005260406000206000915090508060000154905081565b6000806000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001549050838184604051602001808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166c0100000000000000000000000002815260140183815260200182805190602001908083835b602083101515610bd35780518252602082019150602081019050602083039250610bae565b6001836020036101000a03801982511681845116808217855250505050505090500193505050506040516020818303038152906040528051906020012091506000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600201829080600181540180825580915050906001820390600052602060002001600090919290919091505550806000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000181905550826000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160008481526020019081526020016000206001019080519060200190610d40929190611018565b5060016000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060020160006101000a81548160ff021916908315150217905550600181016000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001819055505092915050565b60006001026000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206002016000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600101600084815260200190815260200160002060000154815481101515610ea757fe5b90600052602060002001819055506000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206001016000828152602001908152602001600020600080820160009055600182016000610f209190610fd0565b6002820160006101000a81549060ff0219169055505050565b60606000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600201805480602002602001604051908101604052809291908181526020018280548015610fc657602002820191906000526020600020905b815481526020019060010190808311610fb2575b5050505050905090565b50805460018160011615610100020316600290046000825580601f10610ff65750611015565b601f0160209004906000526020600020908101906110149190611098565b5b50565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061105957805160ff1916838001178555611087565b82800160010185558215611087579182015b8281111561108657825182559160200191906001019061106b565b5b5090506110949190611098565b5090565b6110ba91905b808211156110b657600081600090555060010161109e565b5090565b9056fea165627a7a72305820d3266e7c7c7eac4ba3a63200f8fd1278d15114aea89229143858181fb324155f0029";
        value = new BigInteger("0");
        String contractAddress = demo.deployContract(issuer, bytecode, value, null);

        // issue token
        byte[] data = {0x22};
        IssueTokenResp issueTokenResp = demo.issueToken(issuer, account2, data);

        // get my wallet
        GetWalletResp walletResp = demo.getWallet(account2);

        // query token
        byte[] hash = walletResp.getHashes().get(0).getValue();
        demo.getToken(account2, hash);

        // shift account2's token to account3
        ShiftResp shiftResp = demo.shift(issuer, account2, account3, hash);
        byte[] newHash = shiftResp.getNewHash().getValue();

        // check account3 token
        demo.getToken(account3, newHash);
    }
}
