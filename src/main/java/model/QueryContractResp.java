package model;

import lombok.Data;
import org.web3j.abi.datatypes.Type;

import java.util.List;

@Data
public class QueryContractResp {
    List<Type> outputs;
    String err;
}
