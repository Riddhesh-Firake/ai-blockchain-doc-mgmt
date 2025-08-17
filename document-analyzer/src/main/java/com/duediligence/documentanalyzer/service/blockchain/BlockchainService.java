// BlockchainService.java
package com.duediligence.documentanalyzer.service.blockchain;

import com.duediligence.documentanalyzer.blockchain.utils.ContractLoader;
import com.duediligence.documentanalyzer.model.blockchain.*;
import com.duediligence.documentanalyzer.repository.blockchain.AuditLogRepository;
import com.duediligence.documentanalyzer.repository.blockchain.BlockchainDocumentRepository;
import com.duediligence.documentanalyzer.repository.blockchain.ShareRecordRepository;
import com.duediligence.documentanalyzer.repository.blockchain.DocumentVersionRepository;
import com.duediligence.documentanalyzer.blockchain.contracts.DocumentRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling blockchain operations and document storage
 * Integrates with Ethereum blockchain via Web3j for document tracking
 * Enhanced with IPFS storage and encryption capabilities
 */
@Service
@Transactional
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);

    @Value("${app.blockchain.ganache.url:http://127.0.0.1:7545}")
    private String ganacheUrl;

    @Value("${app.blockchain.contract.address}")
    private String contractAddress;

    @Value("${app.blockchain.private-key}")
    private String privateKey;

    @Value("${app.file-storage.blockchain-dir:./blockchain-storage}")
    private String storageDirectory;

    @Value("${app.file-storage.use-ipfs:true}")
    private boolean useIPFS;

    @Value("${app.file-storage.encryption.enabled:true}")
    private boolean encryptionEnabled;

    private final BlockchainDocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final ShareRecordRepository shareRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;
    private final IPFSService ipfsService;
    private final EncryptionService encryptionService;

    private Web3j web3j;
    private DocumentRegistry contract;

    @Autowired
    private ContractLoader contractLoader;

    @Autowired
    public BlockchainService(
            BlockchainDocumentRepository documentRepository,
            DocumentVersionRepository versionRepository,
            ShareRecordRepository shareRecordRepository,
            AuditLogRepository auditLogRepository,
            NotificationService notificationService,
            IPFSService ipfsService,
            EncryptionService encryptionService) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.shareRecordRepository = shareRecordRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationService = notificationService;
        this.ipfsService = ipfsService;
        this.encryptionService = encryptionService;
    }

    /**
     * Initialize blockchain connection and smart contract
     */
    @PostConstruct
    public void initializeBlockchain() {
        try {
            // Connect to Ganache
            web3j = Web3j.build(new HttpService(ganacheUrl));

            // Test connection
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            logger.info("Connected to Ethereum client: {}", clientVersion);

            // Load credentials
            ReadonlyTransactionManager readonlyTxManager = new ReadonlyTransactionManager(web3j, contractAddress);
            // logger.info("Loaded wallet address: {}", credentials.getAddress());

            // Load smart contract with file-based ABI and bytecode
            ContractGasProvider gasProvider = new ContractGasProvider() {
                @Override
                public BigInteger getGasPrice(String contractFunc) {
                    return BigInteger.valueOf(20_000_000_000L); // 20 gwei
                }

                @Override
                public BigInteger getGasPrice() {
                    return BigInteger.valueOf(20_000_000_000L);
                }

                @Override
                public BigInteger getGasLimit(String contractFunc) {
                    // Reduce gas limit for different functions
                    switch (contractFunc != null ? contractFunc : "") {
                        case "uploadDocument":
                            return BigInteger.valueOf(3_000_000L); // 3M for upload
                        case "updateDocument":
                            return BigInteger.valueOf(2_000_000L); // 2M for update
                        case "shareDocument":
                            return BigInteger.valueOf(1_500_000L); // 1.5M for share
                        case "revokeAccess":
                            return BigInteger.valueOf(1_000_000L); // 1M for revoke
                        default:
                            return BigInteger.valueOf(2_000_000L); // 2M default
                    }
                }

                @Override
                public BigInteger getGasLimit() {
                    return BigInteger.valueOf(2_000_000L);
                }
            };

            contract = DocumentRegistry.load(
                    contractAddress,
                    web3j,
                    readonlyTxManager,
                    gasProvider,
                    contractLoader
            );

            logger.info("Smart contract loaded at address: {}", contractAddress);
            logger.info("IPFS storage enabled: {}", useIPFS);
            logger.info("File encryption enabled: {}", encryptionEnabled);

            // Create storage directory if not using IPFS
            if (!useIPFS) {
                createStorageDirectory();
            }

        } catch (Exception e) {
            logger.error("Failed to initialize blockchain connection", e);
            throw new RuntimeException("Blockchain initialization failed", e);
        }
    }

    /**
     * Upload document to blockchain with encryption and IPFS storage
     */
    public BlockchainResponse<BlockchainDocument> uploadDocument(
            MultipartFile file,
            DocumentUploadRequest request) {

        try {
            logger.info("Starting document upload for wallet: {}", request.getWalletAddress());

            // Validate file
            if (file.isEmpty()) {
                return BlockchainResponse.error("File is empty");
            }

            // Generate unique document ID
            String documentId = generateDocumentId();
            byte[] originalFileData = file.getBytes();

            // Calculate file hash of original file
            String fileHash = calculateFileHash(originalFileData);
            logger.info("Calculated file hash: {}", fileHash);

            String ipfsHash = "";
            String filePath = "";

            if (useIPFS) {
                // Handle IPFS storage with optional encryption
                byte[] dataToStore = originalFileData;

                if (encryptionEnabled) {
                    // Encrypt file before storing
                    EncryptionService.EncryptionResult encryptionResult =
                            encryptionService.encryptFile(originalFileData, documentId);
                    dataToStore = encryptionResult.getEncryptedData();
                    logger.info("File encrypted for document: {}", documentId);
                }

                // Upload to IPFS
                IPFSService.IPFSUploadResult ipfsResult = ipfsService.uploadFile(
                        dataToStore,
                        file.getOriginalFilename(),
                        documentId
                );

                if (!ipfsResult.isSuccess()) {
                    return BlockchainResponse.error("IPFS upload failed");
                }

                ipfsHash = ipfsResult.getIpfsHash();
                filePath = ipfsResult.getGatewayUrl();
                logger.info("File uploaded to IPFS: {}", ipfsHash);

            } else {
                // Save file to local storage
                filePath = saveFileToStorage(file, documentId);
                logger.info("File saved to: {}", filePath);
            }

            // Upload to blockchain (store original file hash, not encrypted)
            String transactionHash = uploadToBlockchain(
                    documentId,
                    file.getOriginalFilename(),
                    fileHash,
                    request.getDomain(),
                    ipfsHash
            );

            // Save to database
            BlockchainDocument document = new BlockchainDocument(
                    documentId,
                    file.getOriginalFilename(),
                    fileHash,
                    request.getDomain(),
                    request.getWalletAddress(),
                    filePath,
                    transactionHash
            );

            // Set IPFS hash if using IPFS
            if (useIPFS) {
                document.setIpfsHash(ipfsHash);
            }

            document = documentRepository.save(document);

            // Create audit log
            createAuditLog(documentId, request.getWalletAddress(), "upload",
                    "Document uploaded successfully" +
                            (encryptionEnabled ? " with encryption" : "") +
                            (useIPFS ? " to IPFS" : " locally"), transactionHash);

            logger.info("Document upload completed successfully. Document ID: {}", documentId);

            return BlockchainResponse.success(
                    "Document uploaded successfully",
                    document,
                    transactionHash
            );

        } catch (Exception e) {
            logger.error("Document upload failed", e);
            return BlockchainResponse.error("Upload failed: " + e.getMessage());
        }
    }

    /**
     * Download and decrypt document
     */
    public BlockchainResponse<byte[]> downloadDocument(String documentId, String requesterAddress) {
        try {
            logger.info("Starting document download for document: {}", documentId);

            // Find document
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                return BlockchainResponse.error("Document not found");
            }

            BlockchainDocument document = optionalDoc.get();

            // Check access permissions
            if (!hasDocumentAccess(documentId, requesterAddress)) {
                return BlockchainResponse.error("Access denied");
            }

            byte[] fileData;

            if (useIPFS && document.getIpfsHash() != null && !document.getIpfsHash().isEmpty()) {
                // Download from IPFS
                fileData = ipfsService.downloadFile(document.getIpfsHash());
                logger.info("File downloaded from IPFS: {}", document.getIpfsHash());

                // Decrypt if encryption is enabled
                if (encryptionEnabled) {
                    fileData = encryptionService.decryptFile(fileData, documentId);
                    logger.info("File decrypted for document: {}", documentId);
                }
            } else {
                // Load from local storage
                Path filePath = Paths.get(document.getFilePath());
                if (!Files.exists(filePath)) {
                    return BlockchainResponse.error("Physical file not found");
                }
                fileData = Files.readAllBytes(filePath);
            }

            // Verify file integrity
            String currentHash = calculateFileHash(fileData);
            if (!document.getFileHash().equals(currentHash)) {
                logger.warn("File integrity check failed for document: {}", documentId);

                createAuditLog(documentId, "SYSTEM", "integrity_violation",
                        "File integrity check failed during download", null);

                notificationService.notifyIntegrityViolation(
                        document.getOwnerAddress(),
                        documentId,
                        document.getFileName()
                );
            }

            // Create audit log for download
            createAuditLog(documentId, requesterAddress, "download",
                    "Document downloaded successfully", null);

            return BlockchainResponse.success(
                    "Document downloaded successfully",
                    fileData,
                    null
            );

        } catch (Exception e) {
            logger.error("Document download failed", e);
            return BlockchainResponse.error("Download failed: " + e.getMessage());
        }
    }

    /**
     * Update existing document
     */
    public BlockchainResponse<DocumentVersion> updateDocument(
            String documentId,
            MultipartFile newFile,
            DocumentUpdateRequest request) {

        try {
            logger.info("Starting document update for document: {}", documentId);

            // Find existing document
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                return BlockchainResponse.error("Document not found");
            }

            BlockchainDocument document = optionalDoc.get();

            // Verify ownership
            if (!document.getOwnerAddress().equalsIgnoreCase(request.getWalletAddress())) {
                return BlockchainResponse.error("Access denied: Not document owner");
            }

            byte[] originalFileData = newFile.getBytes();
            // Calculate new file hash
            String newFileHash = calculateFileHash(originalFileData);

            // Check if file actually changed
            if (document.getFileHash().equals(newFileHash)) {
                return BlockchainResponse.error("No changes detected in file");
            }

            // Get next version number
            int nextVersion = versionRepository.countByDocumentId(documentId) + 1;

            String newIpfsHash = "";
            String newFilePath = "";

            if (useIPFS) {
                // Handle IPFS storage with optional encryption
                byte[] dataToStore = originalFileData;

                if (encryptionEnabled) {
                    // Encrypt new file version
                    EncryptionService.EncryptionResult encryptionResult =
                            encryptionService.encryptFile(originalFileData, documentId);
                    dataToStore = encryptionResult.getEncryptedData();
                    logger.info("New file version encrypted for document: {}", documentId);
                }

                // Upload new version to IPFS
                IPFSService.IPFSUploadResult ipfsResult = ipfsService.uploadFile(
                        dataToStore,
                        newFile.getOriginalFilename(),
                        documentId + "_v" + nextVersion
                );

                if (!ipfsResult.isSuccess()) {
                    return BlockchainResponse.error("IPFS upload failed for new version");
                }

                newIpfsHash = ipfsResult.getIpfsHash();
                newFilePath = ipfsResult.getGatewayUrl();
                logger.info("New file version uploaded to IPFS: {}", newIpfsHash);

                // Optionally delete old version from IPFS
                if (document.getIpfsHash() != null && !document.getIpfsHash().isEmpty()) {
                    boolean deleted = ipfsService.deleteFile(document.getIpfsHash());
                    if (deleted) {
                        logger.info("Old version removed from IPFS: {}", document.getIpfsHash());
                    }
                }

            } else {
                // Replace file in local storage
                newFilePath = replaceFileInStorage(newFile, documentId);
            }

            // Update blockchain
            String transactionHash = updateDocumentOnBlockchain(
                    documentId,
                    newFileHash,
                    request.getUpdateReason()
            );

            // Update document record
            document.setFileHash(newFileHash);
            document.setFilePath(newFilePath);
            document.setUpdatedAt(LocalDateTime.now());
            if (useIPFS) {
                document.setIpfsHash(newIpfsHash);
            }
            documentRepository.save(document);

            // Create version record
            DocumentVersion version = new DocumentVersion(
                    documentId,
                    newFileHash,
                    nextVersion,
                    request.getWalletAddress(),
                    request.getUpdateReason(),
                    transactionHash
            );
            version = versionRepository.save(version);

            // Create audit log
            createAuditLog(documentId, request.getWalletAddress(), "update",
                    "Document updated to version " + nextVersion +
                            (encryptionEnabled ? " with encryption" : "") +
                            (useIPFS ? " on IPFS" : ""), transactionHash);

            // Notify users with access about the update
            notifyDocumentUpdate(documentId, nextVersion);

            logger.info("Document update completed successfully. New version: {}", nextVersion);

            return BlockchainResponse.success(
                    "Document updated successfully",
                    version,
                    transactionHash
            );

        } catch (Exception e) {
            logger.error("Document update failed", e);
            return BlockchainResponse.error("Update failed: " + e.getMessage());
        }
    }

    /**
     * Share document with another user
     */
    public BlockchainResponse<ShareRecord> shareDocument(
            String documentId,
            DocumentShareRequest request) {

        try {
            logger.info("Starting document share for document: {}", documentId);

            // Find document
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                return BlockchainResponse.error("Document not found");
            }

            BlockchainDocument document = optionalDoc.get();

            // Verify ownership
            if (!document.getOwnerAddress().equalsIgnoreCase(request.getOwnerAddress())) {
                return BlockchainResponse.error("Access denied: Not document owner");
            }

            // Check if already shared with this user
            Optional<ShareRecord> existingShare = shareRecordRepository
                    .findActiveShareRecord(documentId, request.getRecipientAddress());
            if (existingShare.isPresent()) {
                return BlockchainResponse.error("Document already shared with this user");
            }

            // Share on blockchain
            String transactionHash = shareDocumentOnBlockchain(
                    documentId,
                    request.getRecipientAddress(),
                    request.getAccessLevel()
            );

            // Create share record
            ShareRecord shareRecord = new ShareRecord(
                    documentId,
                    request.getOwnerAddress(),
                    request.getRecipientAddress(),
                    request.getAccessLevel(),
                    transactionHash
            );
            shareRecord = shareRecordRepository.save(shareRecord);

            // Create audit log
            createAuditLog(documentId, request.getOwnerAddress(), "share",
                    "Document shared with " + request.getRecipientAddress() +
                            " (Access: " + request.getAccessLevel() + ")", transactionHash);

            // Notify recipient
            notificationService.notifyDocumentShared(
                    request.getRecipientAddress(),
                    documentId,
                    document.getFileName()
            );

            logger.info("Document shared successfully");

            return BlockchainResponse.success(
                    "Document shared successfully",
                    shareRecord,
                    transactionHash
            );

        } catch (Exception e) {
            logger.error("Document sharing failed", e);
            return BlockchainResponse.error("Sharing failed: " + e.getMessage());
        }
    }

    /**
     * Revoke access to a document
     */
    public BlockchainResponse<String> revokeAccess(
            String documentId,
            String ownerAddress,
            String userAddress) {

        try {
            logger.info("Revoking access for user {} from document {}", userAddress, documentId);

            // Find document
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                return BlockchainResponse.error("Document not found");
            }

            BlockchainDocument document = optionalDoc.get();

            // Verify ownership
            if (!document.getOwnerAddress().equalsIgnoreCase(ownerAddress)) {
                return BlockchainResponse.error("Access denied: Not document owner");
            }

            // Find active share record
            Optional<ShareRecord> optionalShare = shareRecordRepository
                    .findActiveShareRecord(documentId, userAddress);
            if (optionalShare.isEmpty()) {
                return BlockchainResponse.error("No active share found for this user");
            }

            // Revoke on blockchain
            String transactionHash = revokeAccessOnBlockchain(documentId, userAddress);

            // Update share record
            ShareRecord shareRecord = optionalShare.get();
            shareRecord.setActive(false);
            shareRecordRepository.save(shareRecord);

            // Create audit log
            createAuditLog(documentId, ownerAddress, "revoke",
                    "Access revoked for " + userAddress, transactionHash);

            // Notify user
            notificationService.notifyAccessRevoked(userAddress, documentId, document.getFileName());

            logger.info("Access revoked successfully");

            return BlockchainResponse.success(
                    "Access revoked successfully",
                    "Access revoked for " + userAddress,
                    transactionHash
            );

        } catch (Exception e) {
            logger.error("Access revocation failed", e);
            return BlockchainResponse.error("Revocation failed: " + e.getMessage());
        }
    }

    /**
     * Verify document integrity
     */
    public BlockchainResponse<Boolean> verifyDocumentIntegrity(String documentId) {
        try {
            logger.info("Verifying integrity for document: {}", documentId);

            // Find document
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                return BlockchainResponse.error("Document not found");
            }

            BlockchainDocument document = optionalDoc.get();
            boolean isValid = false;

            if (useIPFS && document.getIpfsHash() != null && !document.getIpfsHash().isEmpty()) {
                // Download from IPFS and verify
                byte[] encryptedData = ipfsService.downloadFile(document.getIpfsHash());

                if (encryptionEnabled) {
                    // Decrypt and verify against original file hash
                    byte[] decryptedData = encryptionService.decryptFile(encryptedData, documentId);
                    String currentHash = calculateFileHash(decryptedData);
                    isValid = document.getFileHash().equals(currentHash);
                } else {
                    // Verify unencrypted data hash
                    String currentHash = calculateFileHash(encryptedData);
                    isValid = document.getFileHash().equals(currentHash);
                }
            } else {
                // Read current file and calculate hash
                Path filePath = Paths.get(document.getFilePath());
                if (!Files.exists(filePath)) {
                    return BlockchainResponse.error("Physical file not found");
                }

                byte[] fileBytes = Files.readAllBytes(filePath);
                String currentHash = calculateFileHash(fileBytes);
                isValid = document.getFileHash().equals(currentHash);
            }

            if (!isValid) {
                logger.warn("Document integrity violation detected for document: {}", documentId);

                // Create audit log for integrity violation
                createAuditLog(documentId, "SYSTEM", "integrity_violation",
                        "Hash mismatch detected", null);

                // Notify owner
                notificationService.notifyIntegrityViolation(
                        document.getOwnerAddress(),
                        documentId,
                        document.getFileName()
                );
            }

            return BlockchainResponse.success(
                    isValid ? "Document integrity verified" : "Document integrity compromised",
                    isValid,
                    null
            );

        } catch (Exception e) {
            logger.error("Document verification failed", e);
            return BlockchainResponse.error("Verification failed: " + e.getMessage());
        }
    }

    /**
     * Get user's documents
     */
    public BlockchainResponse<List<BlockchainDocument>> getUserDocuments(String walletAddress) {
        try {
            List<BlockchainDocument> documents = documentRepository.findByOwnerAddressAndIsActive(walletAddress, true);
            return BlockchainResponse.success("Documents retrieved successfully", documents, null);
        } catch (Exception e) {
            logger.error("Failed to get user documents", e);
            return BlockchainResponse.error("Failed to retrieve documents: " + e.getMessage());
        }
    }

    /**
     * Get documents shared with user
     */
    public BlockchainResponse<List<BlockchainDocument>> getSharedDocuments(String walletAddress) {
        try {
            List<String> sharedDocumentIds = shareRecordRepository.findActiveSharedDocumentIds(walletAddress);
            List<BlockchainDocument> documents = documentRepository.findAllById(sharedDocumentIds);
            return BlockchainResponse.success("Shared documents retrieved successfully", documents, null);
        } catch (Exception e) {
            logger.error("Failed to get shared documents", e);
            return BlockchainResponse.error("Failed to retrieve shared documents: " + e.getMessage());
        }
    }

    /**
     * Get document audit trail
     */
    public BlockchainResponse<List<AuditLog>> getDocumentAuditTrail(String documentId) {
        try {
            List<AuditLog> auditLogs = auditLogRepository.findByDocumentIdOrderByTimestampDesc(documentId);
            return BlockchainResponse.success("Audit trail retrieved successfully", auditLogs, null);
        } catch (Exception e) {
            logger.error("Failed to get audit trail", e);
            return BlockchainResponse.error("Failed to retrieve audit trail: " + e.getMessage());
        }
    }

    // Private helper methods

    private String generateDocumentId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String calculateFileHash(byte[] fileBytes) throws Exception {
        if (encryptionEnabled) {
            return encryptionService.calculateFileHash(fileBytes);
        } else {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
    }

    private boolean hasDocumentAccess(String documentId, String userAddress) {
        try {
            // Check if user is owner
            Optional<BlockchainDocument> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isPresent() &&
                    optionalDoc.get().getOwnerAddress().equalsIgnoreCase(userAddress)) {
                return true;
            }

            // Check if document is shared with user
            Optional<ShareRecord> activeShare = shareRecordRepository
                    .findActiveShareRecord(documentId, userAddress);
            return activeShare.isPresent();

        } catch (Exception e) {
            logger.error("Failed to check document access", e);
            return false;
        }
    }

    private void createStorageDirectory() throws IOException {
        Path storagePath = Paths.get(storageDirectory);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            logger.info("Created storage directory: {}", storageDirectory);
        }
    }

    private String saveFileToStorage(MultipartFile file, String documentId) throws IOException {
        String fileName = documentId + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(storageDirectory, fileName);
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }

    private String replaceFileInStorage(MultipartFile newFile, String documentId) throws IOException {
        // Find existing file and delete it
        Optional<BlockchainDocument> doc = documentRepository.findById(documentId);
        if (doc.isPresent()) {
            Path oldFilePath = Paths.get(doc.get().getFilePath());
            Files.deleteIfExists(oldFilePath);
        }

        // Save new file
        return saveFileToStorage(newFile, documentId);
    }

    private void createAuditLog(String documentId, String actor, String action,
                                String details, String transactionHash) {
        try {
            AuditLog auditLog = new AuditLog(documentId, actor, action, details, transactionHash);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    private void notifyDocumentUpdate(String documentId, int version) {
        try {
            // Get all users with access to this document
            List<ShareRecord> activeShares = shareRecordRepository.findActiveSharesByDocumentId(documentId);

            for (ShareRecord share : activeShares) {
                notificationService.notifyDocumentUpdate(
                        share.getSharedWith(),
                        documentId,
                        version
                );
            }
        } catch (Exception e) {
            logger.error("Failed to notify document update", e);
        }
    }

    // Blockchain interaction methods

    private String uploadToBlockchain(String documentId, String fileName,
                                      String fileHash, String domain, String ipfsHash) throws Exception {

        return "pending_frontend_transaction";
    }

    private String updateDocumentOnBlockchain(String documentId, String newFileHash,
                                              String updateReason) throws Exception {

        return "pending_frontend_transaction";
    }

    private String shareDocumentOnBlockchain(String documentId, String recipientAddress,
                                             String accessLevel) throws Exception {
        return "pending_frontend_transaction";
    }

    private String revokeAccessOnBlockchain(String documentId, String userAddress) throws Exception {

        return "pending_frontend_transaction";
    }
}