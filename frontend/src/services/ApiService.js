import AuthService from './AuthService';
import Web3Service from './Web3Service';

class ApiService {
  static BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

  // Replace the request method in ApiService.js (around line 15-35)
  static async request(endpoint, options = {}) {
    const url = `${this.BASE_URL}${endpoint}`;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...AuthService.getAuthHeaders(),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        if (response.status === 401) {
          AuthService.logout();
          window.location.href = '/auth';
          return;
        }

        // Enhanced error handling to capture backend error details
        let errorData;
        try {
          errorData = await response.json();
        } catch (e) {
          errorData = { message: 'Request failed' };
        }

        console.error('API Error Details:', {
          status: response.status,
          statusText: response.statusText,
          url: url,
          method: config.method || 'GET',
          body: config.body,
          errorData: errorData
        });

        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  static async formDataRequest(endpoint, formData, options = {}) {
    const url = `${this.BASE_URL}${endpoint}`;
    const config = {
      method: 'POST',
      headers: {
        ...AuthService.getAuthHeaders(),
        ...options.headers,
      },
      body: formData,
      ...options,
    };

    try {
      const response = await fetch(url, config);

      if (!response.ok) {
        if (response.status === 401) {
          AuthService.logout();
          window.location.href = '/auth';
          return;
        }

        const errorData = await response.json().catch(() => ({ message: 'Request failed' }));
        throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Auth endpoints
  static async login(credentials) {
    const response = await this.request('/api/auth/signin', {
      method: 'POST',
      body: JSON.stringify(credentials),
    });

    if (response.token) {
      AuthService.setToken(response.token);
    }

    return response;
  }

  static async signup(userData) {
    const response = await this.request('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(userData),
    });

    if (response.token) {
      AuthService.setToken(response.token);
    }

    return response;
  }

  static async getCurrentUser() {
    return await this.request('/api/auth/me');
  }

  static async validateWallet(walletAddress) {
    return await this.request('/api/auth/validate-wallet', {
      method: 'POST',
      body: JSON.stringify(walletAddress),
    });
  }

  // Blockchain endpoints
  static async uploadDocument(file, domain, ipfsHash) {
    try {
      // First, upload to backend to get document metadata
      const formData = new FormData();
      formData.append('file', file);
      formData.append('domain', domain);
      if (ipfsHash) {
        formData.append('ipfsHash', ipfsHash);
      }

      const backendResponse = await this.formDataRequest('/api/blockchain/documents/upload', formData);

      if (backendResponse.success) {
        // Now sign the blockchain transaction
        try {
          const documentData = backendResponse.data;
          const fileHash = await Web3Service.calculateFileHash(file);

          const txHash = await Web3Service.uploadDocumentToBlockchain(
            documentData.documentId,
            file.name,
            fileHash,
            domain,
            ipfsHash || ""
          );

          console.log('Blockchain transaction completed:', txHash);

          // // Optionally update the backend with the transaction hash
          // await this.request('/api/blockchain/documents/update-tx-hash', {
          //   method: 'PATCH',
          //   body: JSON.stringify({
          //     documentId: documentData.documentId,
          //     transactionHash: txHash
          //   })
          // });

          return {
            ...backendResponse,
            transactionHash: txHash
          };
        } catch (blockchainError) {
          console.error('Blockchain transaction failed:', blockchainError);
          throw new Error(`Blockchain transaction failed: ${blockchainError.message}`);
        }
      }

      return backendResponse;
    } catch (error) {
      console.error('Upload process failed:', error);
      throw error;
    }
  }

  static async updateDocument(documentId, file, updateReason) {
    try {
      // First, update on backend
      const formData = new FormData();
      formData.append('file', file);
      if (updateReason) {
        formData.append('updateReason', updateReason);
      }

      const backendResponse = await this.formDataRequest(`/api/blockchain/documents/${documentId}`, formData, {
        method: 'PUT',
      });

      if (backendResponse.success) {
        // Sign blockchain transaction
        try {
          const newFileHash = await Web3Service.calculateFileHash(file);

          const txHash = await Web3Service.updateDocumentOnBlockchain(
            documentId,
            newFileHash,
            updateReason || ""
          );

          console.log('Update transaction completed:', txHash);

          return {
            ...backendResponse,
            transactionHash: txHash
          };
        } catch (blockchainError) {
          console.error('Blockchain update failed:', blockchainError);
          throw new Error(`Blockchain transaction failed: ${blockchainError.message}`);
        }
      }

      return backendResponse;
    } catch (error) {
      console.error('Update process failed:', error);
      throw error;
    }
  }

  static async shareDocument(documentId, shareData) {
    try {
      // First, create share record on backend
      const backendResponse = await this.request(`/api/blockchain/documents/${documentId}/share`, {
        method: 'POST',
        body: JSON.stringify({
          ownerAddress: shareData.ownerAddress,
          recipientAddress: shareData.recipientAddress,
          accessLevel: shareData.accessLevel
        }),
      });

      if (backendResponse.success) {
        // Sign blockchain transaction
        try {
          const txHash = await Web3Service.shareDocumentOnBlockchain(
            documentId,
            shareData.recipientAddress,
            shareData.accessLevel
          );

          console.log('Share transaction completed:', txHash);

          return {
            ...backendResponse,
            transactionHash: txHash
          };
        } catch (blockchainError) {
          console.error('Blockchain share failed:', blockchainError);
          throw new Error(`Blockchain transaction failed: ${blockchainError.message}`);
        }
      }

      return backendResponse;
    } catch (error) {
      console.error('Share process failed:', error);
      throw error;
    }
  }

  static async revokeAccess(documentId, userAddress) {
    try {
      // First, revoke on backend
      const backendResponse = await this.request(`/api/blockchain/documents/${documentId}/share/${userAddress}`, {
        method: 'DELETE',
      });

      if (backendResponse.success) {
        // Sign blockchain transaction
        try {
          const txHash = await Web3Service.revokeAccessOnBlockchain(
            documentId,
            userAddress
          );

          console.log('Revoke transaction completed:', txHash);

          return {
            ...backendResponse,
            transactionHash: txHash
          };
        } catch (blockchainError) {
          console.error('Blockchain revoke failed:', blockchainError);
          throw new Error(`Blockchain transaction failed: ${blockchainError.message}`);
        }
      }

      return backendResponse;
    } catch (error) {
      console.error('Revoke process failed:', error);
      throw error;
    }
  }

  static async connectWallet() {
    try {
      const walletAddress = await Web3Service.connectWallet();

      // Validate wallet with backend
      const response = await this.request('/api/auth/validate-wallet', {
        method: 'POST',
        body: JSON.stringify({ walletAddress }),
      });

      return {
        walletAddress,
        validated: response.success
      };
    } catch (error) {
      console.error('Wallet connection failed:', error);
      throw error;
    }
  }

  static async verifyDocument(documentId) {
    return await this.request(`/api/blockchain/documents/${documentId}/verify`);
  }

  static async getMyDocuments() {
    return await this.request('/api/blockchain/documents/my-documents');
  }

  static async getSharedDocuments() {
    return await this.request('/api/blockchain/documents/shared-with-me');
  }

  static async downloadDocument(documentId) {
    const url = `${this.BASE_URL}/api/blockchain/documents/${documentId}/download`;
    const config = {
      headers: {
        ...AuthService.getAuthHeaders(),
      },
    };

    const response = await fetch(url, config);

    if (!response.ok) {
      throw new Error('Download failed');
    }

    return response.blob();
  }

  static async getDocumentAuditTrail(documentId) {
    return await this.request(`/api/blockchain/documents/${documentId}/audit`);
  }

  static async getDocumentMetadata(documentId) {
    return await this.request(`/api/blockchain/documents/${documentId}/metadata`);
  }

  static async getUserStats() {
    return await this.request('/api/blockchain/documents/user-stats');
  }

  static async getSupportedDomains() {
    return await this.request('/api/blockchain/documents/domains');
  }

  // Document Analysis endpoints
  static async analyzeDocuments(files, domain) {
    try {
      const formData = new FormData();

      // Append files with the correct parameter name expected by your backend
      files.forEach(file => {
        formData.append('files', file);
      });

      // Append domain
      formData.append('domain', domain);

      const response = await this.formDataRequest('/api/documents/analyze', formData);

      return response;
    } catch (error) {
      console.error('Document analysis failed:', error);
      throw error;
    }
  }

  static async getDocumentAnalysisDomains() {
    try {
      return await this.request('/api/documents/domains');
    } catch (error) {
      console.error('Failed to fetch supported domains:', error);
      throw error;
    }
  }

  static async checkDocumentAnalysisHealth() {
    try {
      return await this.request('/api/documents/health');
    } catch (error) {
      console.error('Document analysis health check failed:', error);
      throw error;
    }
  }

  // Consultation Service endpoints
  static async submitConsultation(consultationData) {
    try {
      const response = await this.request('/api/consult', {
        method: 'POST',
        body: JSON.stringify(consultationData),
      });

      return response;
    } catch (error) {
      console.error('Consultation submission failed:', error);
      throw error;
    }
  }

  static async getConsultationDomains() {
    try {
      return await this.request('/api/consult/domains');
    } catch (error) {
      console.error('Failed to fetch consultation domains:', error);
      throw error;
    }
  }

  static async checkConsultationHealth() {
    try {
      return await this.request('/api/consult/health');
    } catch (error) {
      console.error('Consultation health check failed:', error);
      throw error;
    }
  }

  // Optional: Get consultation history (if you want to implement this in backend)
  static async getConsultationHistory(page = 0, size = 10) {
    try {
      return await this.request(`/api/consult/history?page=${page}&size=${size}`);
    } catch (error) {
      console.error('Failed to fetch consultation history:', error);
      throw error;
    }
  }

  // Optional: Save consultation to user's history (if you want to implement this in backend)
  static async saveConsultationToHistory(consultationData) {
    try {
      const response = await this.request('/api/consult/history', {
        method: 'POST',
        body: JSON.stringify(consultationData),
      });

      return response;
    } catch (error) {
      console.error('Failed to save consultation to history:', error);
      throw error;
    }
  }

  // News Service endpoints
  static async getLatestNews(domain) {
    try {
      const response = await this.request(`/api/news/${domain}`);
      return response;
    } catch (error) {
      console.error('Failed to fetch latest news:', error);
      throw error;
    }
  }

  static async getNewsDomains() {
    try {
      const response = await this.request('/api/news/domains');
      return response;
    } catch (error) {
      console.error('Failed to fetch news domains:', error);
      throw error;
    }
  }

  static async checkNewsHealth() {
    try {
      const response = await this.request('/api/news/health');
      return response;
    } catch (error) {
      console.error('News service health check failed:', error);
      throw error;
    }
  }

  // Get detailed article content (for expanded cards)
  static async getArticleDetails(articleUrl) {
    try {
      const response = await this.request('/api/news/article-details', {
        method: 'POST',
        body: JSON.stringify({ url: articleUrl }),
      });
      return response;
    } catch (error) {
      console.error('Failed to fetch article details:', error);
      throw error;
    }
  }
}

export default ApiService;