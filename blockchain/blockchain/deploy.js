const { Web3 } = require("web3");
const solc = require("solc");
const fs = require("fs");

const web3 = new Web3("http://127.0.0.1:7545");

const source = fs.readFileSync("CarbonCredit.sol", "utf8");

const input = {
  language: "Solidity",
  sources: {
    "CarbonCredit.sol": {
      content: source,
    },
  },
  settings: {
    outputSelection: {
      "*": {
        "*": ["*"],
      },
    },
  },
};

const output = JSON.parse(solc.compile(JSON.stringify(input)));

const contractFile = output.contracts["CarbonCredit.sol"]["CarbonCredit"];

const abi = contractFile.abi;
const bytecode = contractFile.evm.bytecode.object;

async function deploy() {
  const accounts = await web3.eth.getAccounts();

  const contract = new web3.eth.Contract(abi);

  const deployed = await contract
    .deploy({ data: bytecode })
    .send({ from: accounts[0], gas: 3000000 });

  console.log("Contract deployed at:", deployed.options.address);
}

deploy();