import org.web3j.abi.datatypes.generated.Int32;
import server.OGServer;

import java.math.BigInteger;

public class OG {

    private String node_url;
    private OGServer server;

    public OG(String url) {
        this.node_url = url;
        this.server = new OGServer(url);
    }

    public void TransferToken(Account account, String target, Int32 tokenID, BigInteger value, byte[] data) {


    }

    public void PublishToken() {

    }

    public void DestroyToken() {

    }

    public void TokenList() {

    }

    private void transferToken() {

    }

}
