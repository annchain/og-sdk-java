import org.web3j.crypto.ECKeyPair;
import org.web3j.protocol.admin.Admin;
import server.OGRequestGET;
import server.OGRequestPOST;
import server.OGServer;

import java.io.IOException;

public class Account {

    private static Admin admin;
//    private static OGServer server;

    public Account() {

    }

    public Account(byte[] priv) {

    }

    public static Account GenerateAccount() {


        return new Account();
    }

    public static ECKeyPair GenerateECKeyPair() {

        return null;
    }

    public static void main(String args[]) throws IOException {

        String url = "http://127.0.0.1:8000";
        OGServer server = new OGServer(url);

        OGRequestPOST req = new OGRequestPOST();
        req.SetVariable("algorithm", "secp256k1");
        String resp = server.Post("new_account", req);

        System.out.println(resp);
    }


}
