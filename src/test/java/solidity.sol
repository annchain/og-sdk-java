pragma solidity ^0.4.22;

/// @title Voting with delegation.
contract Sample {
    
    uint64 public index;
    address public chairperson;

    constructor(bytes32[] proposalNames, uint64 i) public {
        chairperson = msg.sender;
        index = i;
    }

    function setIndex(uint64 i) public {
        index = i;
    }

}