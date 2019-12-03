pragma solidity >=0.5.0 <0.6.0;

contract NewToken {
    struct MyToken {
        uint index;
        bytes data;
        bool alive;
    }

    struct Wallet {
        uint length;
        mapping(bytes32 => MyToken) tokens;
        bytes32[] keys;
    }

    mapping(address => Wallet) public users;

    address chairperson;

    constructor() public {
        chairperson = msg.sender;
    }

    function issueToken(address toUser, bytes memory data) public returns (bytes32 hash){
        uint nonce = users[toUser].length;
        hash = keccak256(abi.encodePacked(toUser, nonce, data));
        users[toUser].keys.push(hash);
        users[toUser].tokens[hash].index = nonce;
        users[toUser].tokens[hash].data = data;
        users[toUser].tokens[hash].alive = true;
        users[toUser].length  = nonce + 1;
    }
    
    function deleteToken(bytes32 hash) public{
        users[msg.sender].keys[users[msg.sender].tokens[hash].index] = 0;
        delete users[msg.sender].tokens[hash];
    }
    
    function deleteToken(address user, bytes32 hash) public{
        require(msg.sender == chairperson || msg.sender == user);
        users[user].keys[users[user].tokens[hash].index] = 0;
        delete users[user].tokens[hash];
    }
    
    
    function getMyWallet() public view returns (bytes32[] memory hashes){
        return users[msg.sender].keys;
    }
    
    function getToken(address user, bytes32 hash) public view returns (bytes memory data){
        require(users[user].tokens[hash].alive);
        return users[user].tokens[hash].data;
    }
    
    function getToken(bytes32 hash) public view returns (bytes memory data){
        return getToken(msg.sender, hash);
    }
    
    function shift(address fromUser, address toUser, bytes32 hash) public returns (bytes32 newHash){
        require(msg.sender == chairperson || msg.sender == fromUser);
        bytes memory data = getToken(fromUser, hash);
        deleteToken(fromUser, hash);
        return issueToken(toUser, data);
    }
    
    function shift(address toUser, bytes32 hash) public returns (bytes32 newHash){
        return shift(msg.sender, toUser, hash);
    }
}