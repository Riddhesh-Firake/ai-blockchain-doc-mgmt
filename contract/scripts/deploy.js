const fs = require('fs');
const { ethers } = require('ethers');

async function main() {
  try {
    // Check for required environment variables
    const privateKey = process.env.PRIVATE_KEY;
    const rpcUrl = process.env.SEPOLIA_RPC_URL;

    if (!privateKey || !rpcUrl) {
      throw new Error('PRIVATE_KEY and SEPOLIA_RPC_URL environment variables are required.');
    }

    // Connect to the Sepolia testnet
    const provider = new ethers.JsonRpcProvider(rpcUrl);
    
    // Create a wallet instance from the private key
    const wallet = new ethers.Wallet(privateKey, provider);
    console.log('Deploying contract with account:', wallet.address);

    // Load contract artifacts
    const contractJson = JSON.parse(fs.readFileSync('./build/contracts/DocumentRegistry.json', 'utf8'));
    const abi = contractJson.abi;
    const bytecode = contractJson.bytecode;

    // Create a contract factory
    const factory = new ethers.ContractFactory(abi, bytecode, wallet);

    // Deploy the contract
    console.log('Deploying DocumentRegistry contract to Sepolia...');
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
