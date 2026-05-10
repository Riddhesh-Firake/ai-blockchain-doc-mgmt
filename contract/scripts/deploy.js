const fs = require('fs');
const { ethers } = require('ethers');

async function main() {
  try {
    // Connect to the local Ganache network
    const provider = new ethers.JsonRpcProvider('http://127.0.0.1:8545');
    
    // Use the first account from Ganache
    const signer = await provider.getSigner(0);
    console.log('Deploying contract with account:', await signer.getAddress());

    // Load contract artifacts
    const abi = JSON.parse(fs.readFileSync('./build/contracts/DocumentRegistry.json', 'utf8')).abi;
    const bytecode = '0x' + fs.readFileSync('./build/contracts/DocumentRegistry.json', 'utf8').bytecode;

    // Create a contract factory
    const factory = new ethers.ContractFactory(abi, bytecode, signer);

    // Deploy the contract
    console.log('Deploying DocumentRegistry contract...');
    const contract = await factory.deploy();
    
    // Wait for the deployment to be mined
    await contract.waitForDeployment();

    const contractAddress = await contract.getAddress();
    console.log('Contract deployed to address:', contractAddress);

    // Save the contract address to a file for the frontend to use
    const addressData = JSON.stringify({ address: contractAddress });
    fs.writeFileSync('../frontend/src/contract-address.json', addressData);
    console.log('Contract address saved to ../frontend/src/contract-address.json');

  } catch (error) {
    console.error('Failed to deploy contract:', error);
    process.exit(1);
  }
}

main();
