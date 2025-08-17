// src/services/Web3Service.js
// Fixed implementation with the correct ABI

import { ethers } from 'ethers';

class Web3Service {
  constructor() {
    this.account = null;
    this.contractAddress = '0x42bE6c38c84Dbf7E427EF5f80ECCBB2C4E31dfD4';
    this.provider = null;
    this.signer = null;
    this.contract = null;

    // Complete ABI from your deployed contract
    this.contractABI = [
      {
        "inputs": [],
        "stateMutability": "nonpayable",
        "type": "constructor"
      },
      {
        "anonymous": false,
        "inputs": [
          {
            "indexed": true,
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "owner",
            "type": "address"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "revokedFrom",
            "type": "address"
          }
        ],
        "name": "DocumentAccessRevoked",
        "type": "event"
      },
      {
        "anonymous": false,
        "inputs": [
          {
            "indexed": true,
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "sharedBy",
            "type": "address"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "sharedWith",
            "type": "address"
          }
        ],
        "name": "DocumentShared",
        "type": "event"
      },
      {
        "anonymous": false,
        "inputs": [
          {
            "indexed": true,
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "updatedBy",
            "type": "address"
          },
          {
            "indexed": false,
            "internalType": "uint256",
            "name": "version",
            "type": "uint256"
          }
        ],
        "name": "DocumentUpdated",
        "type": "event"
      },
      {
        "anonymous": false,
        "inputs": [
          {
            "indexed": true,
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "indexed": true,
            "internalType": "address",
            "name": "owner",
            "type": "address"
          },
          {
            "indexed": false,
            "internalType": "string",
            "name": "fileHash",
            "type": "string"
          }
        ],
        "name": "DocumentUploaded",
        "type": "event"
      },
      {
        "anonymous": false,
        "inputs": [
          {
            "indexed": true,
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "indexed": false,
            "internalType": "string",
            "name": "expectedHash",
            "type": "string"
          },
          {
            "indexed": false,
            "internalType": "string",
            "name": "actualHash",
            "type": "string"
          }
        ],
        "name": "IntegrityViolation",
        "type": "event"
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "name": "auditLogs",
        "outputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "actor",
            "type": "address"
          },
          {
            "internalType": "string",
            "name": "action",
            "type": "string"
          },
          {
            "internalType": "uint256",
            "name": "timestamp",
            "type": "uint256"
          },
          {
            "internalType": "string",
            "name": "details",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [],
        "name": "contractOwner",
        "outputs": [
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "name": "documentVersions",
        "outputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "fileHash",
            "type": "string"
          },
          {
            "internalType": "uint256",
            "name": "version",
            "type": "uint256"
          },
          {
            "internalType": "address",
            "name": "updatedBy",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "timestamp",
            "type": "uint256"
          },
          {
            "internalType": "string",
            "name": "updateReason",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          }
        ],
        "name": "documents",
        "outputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "fileName",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "fileHash",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "domain",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "owner",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "timestamp",
            "type": "uint256"
          },
          {
            "internalType": "bool",
            "name": "isActive",
            "type": "bool"
          },
          {
            "internalType": "string",
            "name": "ipfsHash",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          }
        ],
        "name": "hasAccess",
        "outputs": [
          {
            "internalType": "bool",
            "name": "",
            "type": "bool"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "name": "shareRecords",
        "outputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "sharedBy",
            "type": "address"
          },
          {
            "internalType": "address",
            "name": "sharedWith",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "timestamp",
            "type": "uint256"
          },
          {
            "internalType": "string",
            "name": "accessLevel",
            "type": "string"
          },
          {
            "internalType": "bool",
            "name": "isActive",
            "type": "bool"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "name": "sharedWithUser",
        "outputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "name": "userDocuments",
        "outputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "fileName",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "fileHash",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "domain",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "ipfsHash",
            "type": "string"
          }
        ],
        "name": "uploadDocument",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "newFileHash",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "updateReason",
            "type": "string"
          }
        ],
        "name": "updateDocument",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "recipient",
            "type": "address"
          },
          {
            "internalType": "string",
            "name": "accessLevel",
            "type": "string"
          }
        ],
        "name": "shareDocument",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "user",
            "type": "address"
          }
        ],
        "name": "revokeAccess",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "string",
            "name": "currentFileHash",
            "type": "string"
          }
        ],
        "name": "verifyDocument",
        "outputs": [
          {
            "internalType": "bool",
            "name": "isValid",
            "type": "bool"
          },
          {
            "internalType": "string",
            "name": "storedHash",
            "type": "string"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          }
        ],
        "name": "getDocument",
        "outputs": [
          {
            "components": [
              {
                "internalType": "string",
                "name": "documentId",
                "type": "string"
              },
              {
                "internalType": "string",
                "name": "fileName",
                "type": "string"
              },
              {
                "internalType": "string",
                "name": "fileHash",
                "type": "string"
              },
              {
                "internalType": "string",
                "name": "domain",
                "type": "string"
              },
              {
                "internalType": "address",
                "name": "owner",
                "type": "address"
              },
              {
                "internalType": "uint256",
                "name": "timestamp",
                "type": "uint256"
              },
              {
                "internalType": "bool",
                "name": "isActive",
                "type": "bool"
              },
              {
                "internalType": "string",
                "name": "ipfsHash",
                "type": "string"
              }
            ],
            "internalType": "struct DocumentRegistry.Document",
            "name": "",
            "type": "tuple"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          }
        ],
        "name": "getDocumentVersions",
        "outputs": [
          {
            "components": [
              {
                "internalType": "string",
                "name": "documentId",
                "type": "string"
              },
              {
                "internalType": "string",
                "name": "fileHash",
                "type": "string"
              },
              {
                "internalType": "uint256",
                "name": "version",
                "type": "uint256"
              },
              {
                "internalType": "address",
                "name": "updatedBy",
                "type": "address"
              },
              {
                "internalType": "uint256",
                "name": "timestamp",
                "type": "uint256"
              },
              {
                "internalType": "string",
                "name": "updateReason",
                "type": "string"
              }
            ],
            "internalType": "struct DocumentRegistry.DocumentVersion[]",
            "name": "",
            "type": "tuple[]"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          }
        ],
        "name": "getShareRecords",
        "outputs": [
          {
            "components": [
              {
                "internalType": "string",
                "name": "documentId",
                "type": "string"
              },
              {
                "internalType": "address",
                "name": "sharedBy",
                "type": "address"
              },
              {
                "internalType": "address",
                "name": "sharedWith",
                "type": "address"
              },
              {
                "internalType": "uint256",
                "name": "timestamp",
                "type": "uint256"
              },
              {
                "internalType": "string",
                "name": "accessLevel",
                "type": "string"
              },
              {
                "internalType": "bool",
                "name": "isActive",
                "type": "bool"
              }
            ],
            "internalType": "struct DocumentRegistry.ShareRecord[]",
            "name": "",
            "type": "tuple[]"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          }
        ],
        "name": "getAuditLogs",
        "outputs": [
          {
            "components": [
              {
                "internalType": "string",
                "name": "documentId",
                "type": "string"
              },
              {
                "internalType": "address",
                "name": "actor",
                "type": "address"
              },
              {
                "internalType": "string",
                "name": "action",
                "type": "string"
              },
              {
                "internalType": "uint256",
                "name": "timestamp",
                "type": "uint256"
              },
              {
                "internalType": "string",
                "name": "details",
                "type": "string"
              }
            ],
            "internalType": "struct DocumentRegistry.AuditLog[]",
            "name": "",
            "type": "tuple[]"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "address",
            "name": "user",
            "type": "address"
          }
        ],
        "name": "getUserDocuments",
        "outputs": [
          {
            "internalType": "string[]",
            "name": "",
            "type": "string[]"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "address",
            "name": "user",
            "type": "address"
          }
        ],
        "name": "getSharedDocuments",
        "outputs": [
          {
            "internalType": "string[]",
            "name": "",
            "type": "string[]"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      },
      {
        "inputs": [
          {
            "internalType": "string",
            "name": "documentId",
            "type": "string"
          },
          {
            "internalType": "address",
            "name": "user",
            "type": "address"
          }
        ],
        "name": "checkAccess",
        "outputs": [
          {
            "internalType": "bool",
            "name": "",
            "type": "bool"
          }
        ],
        "stateMutability": "view",
        "type": "function",
        "constant": true
      }
    ];
  }

  async initializeProvider() {
    if (typeof window.ethereum === 'undefined') {
      throw new Error('MetaMask not found. Please install MetaMask.');
    }

    try {
      // Create provider
      this.provider = new ethers.providers.Web3Provider(window.ethereum);

      // Get signer  
      this.signer = this.provider.getSigner();

      // Create contract instance with the correct ABI
      this.contract = new ethers.Contract(this.contractAddress, this.contractABI, this.signer);

      return true;
    } catch (error) {
      console.error('Provider initialization failed:', error);
      throw error;
    }
  }

  async connectWallet() {
    if (typeof window.ethereum === 'undefined') {
      throw new Error('MetaMask not found. Please install MetaMask.');
    }

    try {
      console.log('🔌 Connecting to wallet...');

      // Initialize provider
      await this.initializeProvider();

      // Check network first
      const network = await this.provider.getNetwork();
      console.log('Current network:', network);

      if (network.chainId !== 1337) {
        throw new Error(`Wrong network. Current: ${network.chainId}, Expected: 1337. Please switch to Ganache network.`);
      }

      // Request account access
      const accounts = await window.ethereum.request({
        method: 'eth_requestAccounts',
      });

      this.account = accounts[0];
      console.log('✅ Connected account:', this.account);

      // Check balance
      const balance = await this.provider.getBalance(this.account);
      const balanceInEth = ethers.utils.formatEther(balance);
      console.log('💰 Account balance:', balanceInEth, 'ETH');

      if (parseFloat(balanceInEth) < 0.1) {
        console.warn('⚠️  Low balance detected. Make sure you have enough ETH for transactions.');
      }

      return this.account;
    } catch (error) {
      console.error('❌ Wallet connection failed:', error);
      throw error;
    }
  }

  async checkContractDeployment() {
    try {
      const code = await this.provider.getCode(this.contractAddress);
      console.log('Contract code length:', code.length);
      const isDeployed = code !== '0x' && code.length > 2;
      console.log('Contract deployed:', isDeployed);
      return isDeployed;
    } catch (error) {
      console.error('Error checking contract deployment:', error);
      return false;
    }
  }

  async uploadDocumentToBlockchain(documentId, fileName, fileHash, domain, ipfsHash = '') {
    try {
      if (!this.account) {
        await this.connectWallet();
      }

      if (!this.contract) {
        await this.initializeProvider();
      }

      const isDeployed = await this.checkContractDeployment();
      if (!isDeployed) {
        throw new Error('Smart contract is not deployed at the specified address');
      }

      console.log('🚀 Attempting to upload document:', {
        documentId,
        fileName,
        fileHash,
        domain,
        ipfsHash
      });

      // Use ethers contract method directly - this handles ABI encoding properly
      const tx = await this.contract.uploadDocument(
        documentId,
        fileName,
        fileHash,
        domain,
        ipfsHash || ""
      );

      console.log('📝 Transaction sent:', tx.hash);

      // Wait for confirmation
      console.log('⏳ Waiting for transaction confirmation...');
      const receipt = await tx.wait();

      console.log('✅ Transaction confirmed:', receipt);

      if (receipt.status === 0) {
        throw new Error('Transaction failed during execution');
      }

      return tx.hash;

    } catch (error) {
      console.error('❌ Blockchain upload failed:', error);

      // Better error handling
      if (error.code === 'UNPREDICTABLE_GAS_LIMIT') {
        throw new Error('Transaction would fail. Check contract state and parameters.');
      } else if (error.code === 'INSUFFICIENT_FUNDS') {
        throw new Error('Insufficient ETH for gas fees.');
      } else if (error.code === 4001) {
        throw new Error('Transaction rejected by user.');
      } else if (error.message && error.message.includes('revert')) {
        // Extract revert reason if available
        const reason = error.message.match(/revert (.+)/)?.[1] || 'Transaction reverted';
        throw new Error(`Contract error: ${reason}`);
      } else {
        throw new Error(`Transaction failed: ${error.message || 'Unknown error'}`);
      }
    }
  }

  async updateDocumentOnBlockchain(documentId, newFileHash, updateReason) {
    try {
      if (!this.account) {
        await this.connectWallet();
      }

      if (!this.contract) {
        await this.initializeProvider();
      }

      console.log('🔄 Updating document:', { documentId, newFileHash, updateReason });

      const tx = await this.contract.updateDocument(
        documentId,
        newFileHash,
        updateReason || ""
      );

      console.log('📝 Update transaction sent:', tx.hash);
      const receipt = await tx.wait();
      console.log('✅ Update transaction confirmed:', receipt);

      return tx.hash;
    } catch (error) {
      console.error('❌ Document update failed:', error);
      throw this._handleContractError(error);
    }
  }

  async shareDocumentOnBlockchain(documentId, recipientAddress, accessLevel) {
    try {
      if (!this.account) {
        await this.connectWallet();
      }

      if (!this.contract) {
        await this.initializeProvider();
      }

      console.log('🤝 Sharing document:', { documentId, recipientAddress, accessLevel });

      const tx = await this.contract.shareDocument(
        documentId,
        recipientAddress,
        accessLevel
      );

      console.log('📝 Share transaction sent:', tx.hash);
      const receipt = await tx.wait();
      console.log('✅ Share transaction confirmed:', receipt);

      return tx.hash;
    } catch (error) {
      console.error('❌ Document sharing failed:', error);
      throw this._handleContractError(error);
    }
  }

  async revokeAccessOnBlockchain(documentId, userAddress) {
    try {
      if (!this.account) {
        await this.connectWallet();
      }

      if (!this.contract) {
        await this.initializeProvider();
      }

      console.log('🚫 Revoking access:', { documentId, userAddress });

      const tx = await this.contract.revokeAccess(documentId, userAddress);

      console.log('📝 Revoke transaction sent:', tx.hash);
      const receipt = await tx.wait();
      console.log('✅ Revoke transaction confirmed:', receipt);

      return tx.hash;
    } catch (error) {
      console.error('❌ Access revocation failed:', error);
      throw this._handleContractError(error);
    }
  }

  // Helper method for consistent error handling
  _handleContractError(error) {
    if (error.code === 'UNPREDICTABLE_GAS_LIMIT') {
      return new Error('Transaction would fail. Check contract state and parameters.');
    } else if (error.code === 'INSUFFICIENT_FUNDS') {
      return new Error('Insufficient ETH for gas fees.');
    } else if (error.code === 4001) {
      return new Error('Transaction rejected by user.');
    } else if (error.message && error.message.includes('revert')) {
      const reason = error.message.match(/revert (.+)/)?.[1] || 'Transaction reverted';
      return new Error(`Contract error: ${reason}`);
    } else {
      return new Error(`Transaction failed: ${error.message || 'Unknown error'}`);
    }
  }

  getCurrentAccount() {
    return this.account;
  }

  async calculateFileHash(file) {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = async (e) => {
        try {
          const arrayBuffer = e.target.result;
          const hashBuffer = await crypto.subtle.digest('SHA-256', arrayBuffer);
          const hashArray = Array.from(new Uint8Array(hashBuffer));
          const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
          resolve(hashHex);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = reject;
      reader.readAsArrayBuffer(file);
    });
  }

  generateDocumentId() {
    return Array.from(crypto.getRandomValues(new Uint8Array(16)))
      .map(b => b.toString(16).padStart(2, '0')).join('');
  }

  // Test connection method
  async testConnection() {
    try {
      console.log('=== Testing Connection ===');

      if (typeof window.ethereum === 'undefined') {
        throw new Error('MetaMask not available');
      }
      console.log('✅ MetaMask available');

      await this.initializeProvider();
      console.log('✅ Provider initialized');

      const network = await this.provider.getNetwork();
      if (network.chainId !== 1337) {
        throw new Error(`Wrong network: ${network.chainId}`);
      }
      console.log('✅ Correct network (1337)');

      const accounts = await window.ethereum.request({ method: 'eth_accounts' });
      if (accounts.length === 0) {
        throw new Error('No accounts connected');
      }
      console.log('✅ Account connected:', accounts[0]);

      const balance = await this.provider.getBalance(accounts[0]);
      const ethBalance = parseFloat(ethers.utils.formatEther(balance));
      if (ethBalance < 0.01) {
        throw new Error('Insufficient balance');
      }
      console.log('✅ Sufficient balance:', ethBalance, 'ETH');

      const isDeployed = await this.checkContractDeployment();
      if (!isDeployed) {
        throw new Error('Contract not found');
      }
      console.log('✅ Contract deployed');

      console.log('=== All tests passed ===');
      return true;
    } catch (error) {
      console.error('❌ Test failed:', error.message);
      return false;
    }
  }
}

export default new Web3Service(); 