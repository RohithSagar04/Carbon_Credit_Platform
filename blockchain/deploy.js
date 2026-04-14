const Web3 = require("web3").default;
const solc = require("solc");
const fs = require("fs");
const path = require("path");

const web3 = new Web3("http://127.0.0.1:7545");

// Read contract file properly
const contractPath = path.resolve(__dirname, "CarbonCredit.sol");
const source = fs.readFileSync(contractPath, "utf8");

// Compile input
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
        "*": ["abi", "evm.bytecode"],
      },
    },
  },
};

const output = JSON.parse(solc.compile(JSON.stringify(input)));

// 🔥 SHOW ERRORS (IMPORTANT)
if (output.errors) {
  console.log(output.errors);
}

// 🔥 SAFE ACCESS (prevents crash)
if (!output.contracts || !output.contracts["CarbonCredit.sol"]) {
  throw new Error("Compilation failed. Check errors above.");
}

const contractFile = output.contracts["CarbonCredit.sol"]["CarbonCredit"];

const abi = contractFile.abi;
const bytecode = contractFile.evm.bytecode.object;

async function deploy() {
  const accounts = await web3.eth.getAccounts();

  const contract = new web3.eth.Contract(abi);

  const deployed = await contract
    .deploy({ data: bytecode })
    .send({
      from: accounts[0],
      gas: 5000000,
    });

  console.log("✅ Contract deployed at:", deployed.options.address);
}

deploy();