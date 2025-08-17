package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.model.blockchain.*;
import com.duediligence.documentanalyzer.service.auth.AuthService;
import com.duediligence.documentanalyzer.service.blockchain.BlockchainService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for blockchain-based document storage and sharing
 * Now with complete JWT authentication integration
 */
@RestController
@RequestMapping("/api/blockchain/documents")
@CrossOrigin(origins = "*")
public class BlockchainDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainDocumentController.class);

    private final BlockchainService blockchainService;
    private final AuthService authService;

    @Autowired
    public BlockchainDocumentController(BlockchainService blockchainService, AuthService authService) {
        this.blockchainService = blockchainService;
        this.authService = authService;
    }

    /**
     * POST /api/blockchain/documents/upload
     * Upload document to blockchain storage (authenticated users only)
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<BlockchainDocument>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("domain") String domain,
            @RequestParam(value = "ipfsHash", required = false) String ipfsHash) {

        logger.info("Received authenticated blockchain document upload request");

        try {
            // Get current user's wallet address from JWT token
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();
            logger.info("Upload request from authenticated wallet: {}", walletAddress);

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        BlockchainResponse.error("File cannot be empty")
                );
            }

            // Validate file size (50MB limit)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                        BlockchainResponse.error("File size exceeds 50MB limit")
                );
            }

            // Create upload request with authenticated user's wallet
            DocumentUploadRequest request = new DocumentUploadRequest(walletAddress, domain, ipfsHash);

            // Process upload
            BlockchainResponse<BlockchainDocument> response = blockchainService.uploadDocument(file, request);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Document upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Upload failed: " + e.getMessage())
            );
        }
    }

    /**
     * PUT /api/blockchain/documents/{documentId}
     * Update existing document (authenticated users only)
     */
    @PutMapping("/{documentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<DocumentVersion>> updateDocument(
            @PathVariable String documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "updateReason", required = false) String updateReason) {

        logger.info("Received authenticated document update request for document: {}", documentId);

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        BlockchainResponse.error("File cannot be empty")
                );
            }

            // Create update request
            DocumentUpdateRequest request = new DocumentUpdateRequest(walletAddress, updateReason);

            // Process update
            BlockchainResponse<DocumentVersion> response = blockchainService.updateDocument(
                    documentId, file, request);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Document update failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Update failed: " + e.getMessage())
            );
        }
    }

    /**
     * POST /api/blockchain/documents/{documentId}/share
     * Share document with another user (authenticated users only)
     */
    @PostMapping("/{documentId}/share")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<ShareRecord>> shareDocument(
            @PathVariable String documentId,
            @Valid @RequestBody DocumentShareRequest request) {

        logger.info("Received authenticated document share request for document: {}", documentId);

        try {
            // Validate that the requesting user owns the document by comparing wallet addresses
            Optional<String> currentUserWallet = authService.getCurrentUserWalletAddress();
            if (currentUserWallet.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            // Set the owner address from authenticated user
            request.setOwnerAddress(currentUserWallet.get());

            BlockchainResponse<ShareRecord> response = blockchainService.shareDocument(documentId, request);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Document sharing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Sharing failed: " + e.getMessage())
            );
        }
    }

    /**
     * DELETE /api/blockchain/documents/{documentId}/share/{userAddress}
     * Revoke access to a document (authenticated users only)
     */
    @DeleteMapping("/{documentId}/share/{userAddress}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<String>> revokeAccess(
            @PathVariable String documentId,
            @PathVariable String userAddress) {

        logger.info("Received authenticated access revocation request for document: {}", documentId);

        try {
            // Get current user's wallet address (owner)
            Optional<String> currentUserWallet = authService.getCurrentUserWalletAddress();
            if (currentUserWallet.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String ownerAddress = currentUserWallet.get();

            BlockchainResponse<String> response = blockchainService.revokeAccess(
                    documentId, ownerAddress, userAddress);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Access revocation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Revocation failed: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/{documentId}/verify
     * Verify document integrity (authenticated users only)
     */
    @GetMapping("/{documentId}/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<Boolean>> verifyDocument(@PathVariable String documentId) {

        logger.info("Received authenticated document verification request for document: {}", documentId);

        try {
            BlockchainResponse<Boolean> response = blockchainService.verifyDocumentIntegrity(documentId);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Document verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Verification failed: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/my-documents
     * Get documents owned by current authenticated user
     */
    @GetMapping("/my-documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<List<BlockchainDocument>>> getMyDocuments() {

        logger.info("Received request for authenticated user's documents");

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();
            BlockchainResponse<List<BlockchainDocument>> response = blockchainService.getUserDocuments(walletAddress);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Failed to get user documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Failed to retrieve documents: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/shared-with-me
     * Get documents shared with current authenticated user
     */
    @GetMapping("/shared-with-me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<List<BlockchainDocument>>> getDocumentsSharedWithMe() {

        logger.info("Received request for documents shared with authenticated user");

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();
            BlockchainResponse<List<BlockchainDocument>> response = blockchainService.getSharedDocuments(walletAddress);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Failed to get shared documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Failed to retrieve shared documents: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/{documentId}/audit
     * Get document audit trail (authenticated users only)
     */
    @GetMapping("/{documentId}/audit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<List<AuditLog>>> getDocumentAuditTrail(
            @PathVariable String documentId) {

        logger.info("Received authenticated request for audit trail: {}", documentId);

        try {
            // Optional: Add additional access control check here
            // For now, any authenticated user can view audit trails
            BlockchainResponse<List<AuditLog>> response = blockchainService.getDocumentAuditTrail(documentId);

            return response.isSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Failed to get audit trail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Failed to retrieve audit trail: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/{documentId}/download
     * Download document file (authenticated users only, requires access)
     */
    @GetMapping("/{documentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String documentId) {

        logger.info("Received authenticated download request for document: {}", documentId);

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String walletAddress = walletAddressOpt.get();

            // Check access using existing service methods
            BlockchainResponse<List<BlockchainDocument>> userDocsResponse = blockchainService.getUserDocuments(walletAddress);
            BlockchainResponse<List<BlockchainDocument>> sharedDocsResponse = blockchainService.getSharedDocuments(walletAddress);

            boolean hasAccess = false;
            String fileName = null;

            // Check if user owns the document
            if (userDocsResponse.isSuccess() && userDocsResponse.getData() != null) {
                for (BlockchainDocument doc : userDocsResponse.getData()) {
                    if (doc.getDocumentId().equals(documentId)) {
                        hasAccess = true;
                        fileName = doc.getFileName();
                        break;
                    }
                }
            }

            // Check if document is shared with user
            if (!hasAccess && sharedDocsResponse.isSuccess() && sharedDocsResponse.getData() != null) {
                for (BlockchainDocument doc : sharedDocsResponse.getData()) {
                    if (doc.getDocumentId().equals(documentId)) {
                        hasAccess = true;
                        fileName = doc.getFileName();
                        break;
                    }
                }
            }

            if (!hasAccess) {
                logger.warn("User {} attempted to access document {} without permission", walletAddress, documentId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Download using blockchain service
            BlockchainResponse<byte[]> downloadResponse = blockchainService.downloadDocument(documentId, walletAddress);

            if (!downloadResponse.isSuccess()) {
                logger.error("Failed to download document: {}", downloadResponse.getMessage());
                return ResponseEntity.badRequest().build();
            }

            byte[] fileData = downloadResponse.getData();
            ByteArrayResource resource = new ByteArrayResource(fileData);

            logger.info("Document {} successfully downloaded by user {}", documentId, walletAddress);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Document download failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/blockchain/documents/health
     * Health check endpoint (no authentication required)
     */
    @GetMapping("/health")
    public ResponseEntity<BlockchainResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                BlockchainResponse.success("Blockchain document service is running", "OK", null)
        );
    }

    /**
     * GET /api/blockchain/documents/domains
     * Get supported domains (no authentication required)
     */
    @GetMapping("/domains")
    public ResponseEntity<String[]> getSupportedDomains() {
        String[] domains = {"finance", "healthcare", "legal", "general"};
        return ResponseEntity.ok(domains);
    }

    /**
     * GET /api/blockchain/documents/{documentId}/metadata
     * Get document metadata (authenticated users only, requires access)
     */
    @GetMapping("/{documentId}/metadata")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<BlockchainDocument>> getDocumentMetadata(
            @PathVariable String documentId) {

        logger.info("Received authenticated request for document metadata: {}", documentId);

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();

            // Check access using existing service methods
            BlockchainResponse<List<BlockchainDocument>> userDocsResponse = blockchainService.getUserDocuments(walletAddress);
            BlockchainResponse<List<BlockchainDocument>> sharedDocsResponse = blockchainService.getSharedDocuments(walletAddress);

            BlockchainDocument document = null;

            // Check if user owns the document
            if (userDocsResponse.isSuccess() && userDocsResponse.getData() != null) {
                for (BlockchainDocument doc : userDocsResponse.getData()) {
                    if (doc.getDocumentId().equals(documentId)) {
                        document = doc;
                        break;
                    }
                }
            }

            // Check if document is shared with user
            if (document == null && sharedDocsResponse.isSuccess() && sharedDocsResponse.getData() != null) {
                for (BlockchainDocument doc : sharedDocsResponse.getData()) {
                    if (doc.getDocumentId().equals(documentId)) {
                        document = doc;
                        break;
                    }
                }
            }

            if (document == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        BlockchainResponse.error("Access denied or document not found")
                );
            }

            return ResponseEntity.ok(
                    BlockchainResponse.success("Document metadata retrieved successfully", document, null)
            );

        } catch (Exception e) {
            logger.error("Failed to get document metadata", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Failed to retrieve metadata: " + e.getMessage())
            );
        }
    }

    /**
     * GET /api/blockchain/documents/user-stats
     * Get current user's document statistics
     */
    @GetMapping("/user-stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BlockchainResponse<UserDocumentStats>> getUserDocumentStats() {

        logger.info("Received request for authenticated user's document statistics");

        try {
            // Get current user's wallet address
            Optional<String> walletAddressOpt = authService.getCurrentUserWalletAddress();
            if (walletAddressOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        BlockchainResponse.error("Unable to determine user wallet address")
                );
            }

            String walletAddress = walletAddressOpt.get();

            // Get user documents and shared documents
            BlockchainResponse<List<BlockchainDocument>> userDocsResponse = blockchainService.getUserDocuments(walletAddress);
            BlockchainResponse<List<BlockchainDocument>> sharedDocsResponse = blockchainService.getSharedDocuments(walletAddress);

            int ownedDocuments = 0;
            int sharedWithMe = 0;

            if (userDocsResponse.isSuccess() && userDocsResponse.getData() != null) {
                ownedDocuments = userDocsResponse.getData().size();
            }

            if (sharedDocsResponse.isSuccess() && sharedDocsResponse.getData() != null) {
                sharedWithMe = sharedDocsResponse.getData().size();
            }

            UserDocumentStats stats = new UserDocumentStats(ownedDocuments, sharedWithMe, walletAddress);

            return ResponseEntity.ok(
                    BlockchainResponse.success("User statistics retrieved successfully", stats, null)
            );

        } catch (Exception e) {
            logger.error("Failed to get user document statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BlockchainResponse.error("Failed to retrieve statistics: " + e.getMessage())
            );
        }
    }

    /**
     * Helper class for user document statistics
     */
    public static class UserDocumentStats {
        private int ownedDocuments;
        private int sharedWithMe;
        private String walletAddress;

        public UserDocumentStats(int ownedDocuments, int sharedWithMe, String walletAddress) {
            this.ownedDocuments = ownedDocuments;
            this.sharedWithMe = sharedWithMe;
            this.walletAddress = walletAddress;
        }

        // Getters
        public int getOwnedDocuments() { return ownedDocuments; }
        public int getSharedWithMe() { return sharedWithMe; }
        public String getWalletAddress() { return walletAddress; }
    }
}