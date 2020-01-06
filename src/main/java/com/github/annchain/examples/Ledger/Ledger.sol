pragma solidity >=0.4.20 <0.6.0;

contract Ledger {
    mapping(bytes32 => uint) public incomings;
    mapping(bytes32 => uint) public shares;
    
    uint totalIncoming;
    uint totalShare;
    
    string[] merchants;
    
    address chairperson;

    constructor() public {
        chairperson = msg.sender;
    }
    
    function setMerchant(string memory merchant, uint share) public {
        require(msg.sender == chairperson);
        bytes32 key = keccak256(abi.encodePacked(merchant));
        if (shares[key] == 0){
            shares[key] = share;
            totalShare += share;
            merchants.push(merchant);
        }
    }
    
    function pay(uint cash) public {
        totalIncoming += cash;
    }
    
    function clear() public {
        for (uint8 i = 0 ; i < merchants.length; i++){
            bytes32 key = keccak256(abi.encodePacked(merchants[i]));
            incomings[key] = totalIncoming * shares[key] / totalShare;
        }
        totalIncoming = 0;
    }
    
    function getIncoming(string memory merchant) public view returns (uint){
        bytes32 key = keccak256(abi.encodePacked(merchant));
        return incomings[key];
    }

}