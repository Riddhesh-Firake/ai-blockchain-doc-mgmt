# SecureDoc AI Platform

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Blockchain](https://img.shields.io/badge/Blockchain-121D33?style=for-the-badge&logo=blockchain-dot-com&logoColor=white)
![MetaMask](https://img.shields.io/badge/MetaMask-E2761B?style=for-the-badge&logo=metamask&logoColor=white)
![IPFS](https://img.shields.io/badge/IPFS-65C2CB?style=for-the-badge&logo=ipfs&logoColor=white)

*Secure, AI-Powered Document Management with Blockchain Integration*

---

## 🚀 Overview

SecureDoc AI Platform is a comprehensive document management solution that combines cutting-edge artificial intelligence with blockchain technology to provide secure, intelligent document processing across multiple domains. The platform leverages specialized AI models (FinBERT for finance, BioBERT for healthcare, and LegalBERT for legal documents) to deliver industry-specific document summarization and analysis, while ensuring data integrity and security through blockchain-based storage and audit trails.

Built with a modern tech stack including Spring Boot, Python AI services, and React frontend, the platform offers seamless document upload, AI-powered analysis, secure blockchain storage via IPFS, and comprehensive audit tracking with MetaMask wallet integration.

## 📸 Screenshots

![Landing Page](https://github.com/user-attachments/assets/1c0b9121-1123-44cc-88a5-14759860daff)
*Modern landing page with service overview*

![Dashboard](https://github.com/user-attachments/assets/5f1934ed-76f2-4381-ade0-6694de97e2c2)
*Drag-and-drop document upload interface*

![Blockchain Storage](https://github.com/user-attachments/assets/fcfaf94a-9048-4b55-a4be-0ea716df2bc7)
*Blockchain document storage with IPFS integration*

![Document Sharing](https://github.com/user-attachments/assets/328d8857-099f-4b8f-9d62-945470cece80)
*Secure document sharing & management*

![Audit Trail](https://github.com/user-attachments/assets/ca38d86c-1fe0-40b8-b404-54373971753c)

*Comprehensive blockchain-based audit trail*

![AI Summarization](https://github.com/user-attachments/assets/5dc29799-b5d1-4417-b217-405b76732df8)
![AI Response](https://github.com/user-attachments/assets/cf5aa395-f7ef-4f51-b71e-0f537d74a9bf)

*AI-powered document analysis and summarization results*

![Consultation Interface](https://github.com/user-attachments/assets/5ff25062-3fc7-47dc-866e-358f554b3789)
*Real-time consultation and Q&A interface*

![News Feed](https://github.com/user-attachments/assets/c771c2a3-0f44-4c86-9779-ee9fb4ae8e20)
*Domain-specific news feed integration*


## ✨ Features & Services

### 🤖 AI-Powered Document Processing
- **Document Summarization**: Industry-specific AI models (FinBERT, BioBERT, LegalBERT) for accurate document analysis
- **OCR Integration**: Extract text from images and scanned documents
- **Multi-domain Support**: Specialized processing for Finance, Healthcare, and Legal documents

### 💬 Intelligent Consultation
- **API-based Q&A**: Real-time consultation without ML overhead
- **Domain Expertise**: Contextual responses based on document type
- **Interactive Chat**: Seamless communication interface

### 📰 Real-time News Integration
- **Live News Feeds**: Industry-specific news from reliable APIs
- **Contextual Updates**: News relevant to uploaded documents
- **Multi-source Aggregation**: Comprehensive coverage across domains

### 🔗 Blockchain Security
- **Decentralized Storage**: IPFS integration for distributed document storage
- **MetaMask Integration**: Wallet-based authentication and transaction signing
- **Smart Contracts**: Automated document sharing and access control
- **Audit Trails**: Immutable blockchain-based document history
- **Ganache Development**: Local blockchain testing environment

### 🔐 Authentication & Access Control
- **Multi-modal Authentication**: Traditional login + Web3 wallet integration
- **Role-based Access**: Granular permissions system
- **Secure Sessions**: JWT-based session management
- **Wallet Address Support**: Blockchain identity integration

### 📁 Document Management
- **Secure Upload**: Encrypted file transfer and storage
- **Version Control**: Track document changes and updates
- **Sharing Controls**: Granular document sharing permissions
- **Batch Processing**: Handle multiple documents simultaneously

## 🛠️ Tech Stack

### Backend Services
- **Spring Boot** (Java) - Core REST APIs and business logic
- **Python** - AI/ML model serving and OCR processing
- **PostgreSQL** - Primary data persistence
- **JWT** - Authentication and authorization

### AI & Machine Learning
- **FinBERT** - Financial document analysis
- **BioBERT** - Healthcare document processing  
- **LegalBERT** - Legal document understanding
- **OCR Libraries** - Text extraction from images

### Frontend
- **React** - Modern, responsive user interface
- **Material-UI / Tailwind CSS** - Component library and styling
- **Axios** - HTTP client for API communication
- **React Router** - Client-side routing

### Blockchain & Web3
- **MetaMask** - Wallet integration and transaction signing
- **Ganache** - Local blockchain development environment
- **Web3j** - Java blockchain interaction
- **Solidity** - Smart contract development
- **IPFS** - Decentralized file storage
- **Truffle** - Smart contract deployment and testing

## 📁 Project Structure

```
SecureDoc-AI-Platform/
├── backend/                     # Spring Boot Backend
│   ├── src/main/java/
│   │   └── com/duedilligence/documentanalyzer
│   │       ├── controller/      # REST Controllers
│   │       ├── service/         # Business Logic
│   │       ├── repository/      # Data Access Layer
│   │       ├── model/          # Entity Models
│   │       ├── config/         # Configuration
│   │       └── security/       # Security & JWT
│   ├── src/main/resources/
│   │   └── application.properties.example
│   └── pom.xml
│
├── python-service/         # AI/ML Python Backend
│   ├── main.py             # Flask/FastAPI server
│   ├── ocr_service.py      # OCR processing
│   ├── nlp_models.py       # OCR processing
│   ├── requirements.txt
│   └── config.py
│
├── frontend/                    # React Frontend
│   ├── public/
│   ├── src/
│   │   ├── components/         # Reusable components
│   │   ├── pages/              # Page components
│   │   ├── services/           # API service calls
│   │   ├── utils/              # Utility functions
│   │   ├── hooks/              # Custom React hooks
│   │   └── contexts/           # React contexts
│   ├── package.json
│   └── package-lock.json
│
├── blockchain/                  # Smart Contracts & Web3
│   ├── contracts/
│   │   ├── DocumentRegistry.sol  # Document storage contract
│   ├── migrations/
│   ├── test/
│   ├── truffle-config.js
│   └── package.json
│
└── README.md
```

## 🚀 Setup Instructions

### Prerequisites
- **Node.js** (v16+)
- **Java** (JDK 11+)
- **Python** (3.8+)
- **PostgreSQL** (12+)
- **MetaMask** browser extension
- **Ganache** CLI or GUI

### 1. Clone Repository
```bash
git clone https://github.com/your-username/SecureDoc-AI-Platform.git
cd SecureDoc-AI-Platform
```

### 2. Database Setup
```bash
# Create PostgreSQL database
createdb securedoc_db

# Run schema creation (from backend directory)
psql -d securedoc_db -f src/main/resources/schema.sql
```

### 3. Backend Setup (Spring Boot)
```bash
cd backend

# Copy and configure application properties
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edit application.properties with your database credentials:
# spring.datasource.url=jdbc:postgresql://localhost:5432/securedoc_db
# spring.datasource.username=your_username
# spring.datasource.password=your_password

# Build and run
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Python AI Service Setup
```bash
cd python-service

# Create virtual environment
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Download AI models (first run may take time)
python -c "from transformers import AutoModel, AutoTokenizer; 
           AutoModel.from_pretrained('ProsusAI/finbert');
           AutoModel.from_pretrained('dmis-lab/biobert-base-cased-v1.1');
           AutoTokenizer.from_pretrained('nlpaueb/legal-bert-base-uncased')"

# Run Python service
python main.py
```

### 5. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

### 6. Blockchain Setup
```bash
# Install Ganache CLI globally
npm install -g ganache-cli

# Start local blockchain
ganache-cli --deterministic --accounts 10 --host 0.0.0.0 --port 8545

# In new terminal, deploy smart contracts
cd blockchain
npm install
npx truffle migrate --network development

# Copy contract address from migration output and update in:
# - backend/src/main/resources/application.properties
# - frontend/src/utils/web3Config.js
```

### 7. MetaMask Configuration
1. Install MetaMask browser extension
2. Import account using private key from Ganache output
3. Add custom RPC network:
   - Network Name: `Ganache Local`
   - RPC URL: `http://127.0.0.1:8545`
   - Chain ID: `1337`
   - Currency Symbol: `ETH`

### 8. Verify Installation
- Backend API: http://localhost:8080/api/health
- Python Service: http://localhost:5000/health  
- Frontend: http://localhost:3000
- Blockchain: Check Ganache console for transactions

## 📖 Usage

### Getting Started
1. **Registration/Login**
   - Create account with email/password or connect MetaMask wallet
   - Complete profile setup with role selection

2. **Document Upload**
   - Navigate to Upload page
   - Drag & drop or select documents (PDF, DOC, images)
   - Choose document category (Finance/Healthcare/Legal)

3. **AI Analysis**
   - Documents automatically processed by domain-specific AI models
   - View summarization results and key insights
   - Extract important information and metrics

4. **Consultation & News**
   - Ask questions about uploaded documents
   - Get contextual answers without re-processing
   - Stay updated with relevant industry news

5. **Blockchain Storage**
   - Store documents securely on IPFS
   - Share with specific wallet addresses
   - Track all access and modifications via audit trail

6. **Document Management**
   - Organize documents in collections
   - Set permissions and sharing rules
   - Monitor document analytics and usage

### API Endpoints

#### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/wallet` - Wallet-based authentication

#### Documents
- `POST /api/documents/upload` - Upload document
- `GET /api/documents` - List user documents
- `GET /api/documents/{id}` - Get document details
- `DELETE /api/documents/{id}` - Delete document

#### AI Services
- `POST /api/ai/summarize` - Generate document summary
- `POST /api/ai/analyze` - Detailed document analysis
- `GET /api/consultation/{id}` - Get consultation history

#### Blockchain
- `POST /api/blockchain/store` - Store document on IPFS
- `POST /api/blockchain/share` - Share document access
- `GET /api/blockchain/audit/{id}` - Get audit trail

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Process
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

### Code Standards
- **Java**: Follow Google Java Style Guide
- **Python**: Use PEP 8 coding standards
- **React**: Use ESLint and Prettier configurations
- **Smart Contracts**: Follow Solidity best practices

### Testing
- Write unit tests for new features
- Ensure all tests pass: `npm test` / `./mvnw test`
- Test blockchain integration with Ganache

### Issue Reporting
- Use GitHub Issues for bugs and feature requests
- Include detailed reproduction steps
- Provide environment information

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 SecureDoc AI Platform

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🔮 Future Improvements

### Cloud Deployment
- **Backend**: Deploy Spring Boot service on Render or Heroku
- **Frontend**: Host React app on Vercel or Netlify  
- **Database**: Migrate to managed PostgreSQL (AWS RDS, Google Cloud SQL)
- **AI Services**: Containerize Python services with Docker

### Enhanced AI Capabilities
- **Multi-language Support**: Expand beyond English documents
- **Custom Model Training**: Domain-specific fine-tuning
- **Advanced OCR**: Handwriting recognition and table extraction
- **Sentiment Analysis**: Document emotion and tone detection

### Blockchain Expansion
- **Multi-chain Support**: Ethereum mainnet, Polygon, Binance Smart Chain
- **NFT Integration**: Document authenticity certificates
- **DAO Governance**: Community-driven platform decisions
- **Cross-chain Bridge**: Inter-blockchain document transfers

### Security Enhancements
- **End-to-end Encryption**: Client-side document encryption
- **Zero-knowledge Proofs**: Privacy-preserving document verification
- **Multi-signature Wallets**: Enhanced transaction security
- **Audit Compliance**: SOC 2, HIPAA, GDPR compliance features

### User Experience
- **Mobile Apps**: React Native iOS/Android applications
- **Offline Mode**: Local document processing capabilities
- **Advanced Search**: Full-text search across all documents
- **Integration APIs**: Third-party service connections

### Performance Optimization
- **Caching Layer**: Redis for improved response times
- **CDN Integration**: Global content delivery
- **Load Balancing**: High-availability architecture
- **Database Optimization**: Query performance improvements

---

⭐ **Star this repository if you find it helpful!**

---

*Built with ❤️ by the SecureDoc AI Team*
