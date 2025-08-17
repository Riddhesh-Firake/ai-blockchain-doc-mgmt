// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/**
 * DocumentRegistry Smart Contract
 * Handles secure document storage, sharing, and audit tracking
 * Stores document hashes and metadata on blockchain for integrity verification
 */
contract DocumentRegistry {
    
    // Document structure
    struct Document {
        string documentId;          // Unique document identifier
        string fileName;            // Original file name
        string fileHash;            // SHA-256 hash of document content
        string domain;              // Domain (finance, healthcare, legal, general)
        address owner;              // Document owner's wallet address
        uint256 timestamp;          // Creation timestamp
        bool isActive;              // Document status
        string ipfsHash;            // Optional IPFS hash for decentralized storage
    }
    
    // Document version for tracking updates
    struct DocumentVersion {
        string documentId;          // Reference to original document
        string fileHash;            // New file hash
        uint256 version;            // Version number
        address updatedBy;          // Who updated the document
        uint256 timestamp;          // Update timestamp
        string updateReason;        // Reason for update
    }
    
    // Sharing record
    struct ShareRecord {
        string documentId;          // Document being shared
        address sharedBy;           // Who shared the document
        address sharedWith;         // Recipient (0x0 for public)
        uint256 timestamp;          // When shared
        string accessLevel;         // read, write, admin
        bool isActive;              // Share status
    }
    
    // Audit log entry
    struct AuditLog {
        string documentId;          // Document reference
        address actor;              // Who performed the action
        string action;              // upload, update, share, revoke
        uint256 timestamp;          // When action occurred
        string details;             // Additional details
    }
    
    // Events for frontend notifications
    event DocumentUploaded(string indexed documentId, address indexed owner, string fileHash);
    event DocumentUpdated(string indexed documentId, address indexed updatedBy, uint256 version);
    event DocumentShared(string indexed documentId, address indexed sharedBy, address indexed sharedWith);
    event DocumentAccessRevoked(string indexed documentId, address indexed owner, address indexed revokedFrom);
    event IntegrityViolation(string indexed documentId, string expectedHash, string actualHash);
    
    // Storage mappings
    mapping(string => Document) public documents;
    mapping(string => DocumentVersion[]) public documentVersions;
    mapping(string => ShareRecord[]) public shareRecords;
    mapping(string => AuditLog[]) public auditLogs;
    mapping(address => string[]) public userDocuments; // Documents owned by user
    mapping(address => string[]) public sharedWithUser; // Documents shared with user
    
    // Access control
    mapping(string => mapping(address => bool)) public hasAccess;
    
    // Contract owner
    address public contractOwner;
    
    modifier onlyOwner() {
        require(msg.sender == contractOwner, "Only contract owner can call this");
        _;
    }
    
    modifier onlyDocumentOwner(string memory documentId) {
        require(documents[documentId].owner == msg.sender, "Only document owner can call this");
        _;
    }
    
    modifier documentExists(string memory documentId) {
        require(documents[documentId].owner != address(0), "Document does not exist");
        _;
    }
    
    constructor() {
        contractOwner = msg.sender;
    }
    
    /**
     * Upload a new document to the registry
     */
    function uploadDocument(
        string memory documentId,
        string memory fileName,
        string memory fileHash,
        string memory domain,
        string memory ipfsHash
    ) external {
        require(documents[documentId].owner == address(0), "Document ID already exists");
        require(bytes(fileHash).length > 0, "File hash required");
        require(bytes(fileName).length > 0, "File name required");
        
        // Create document record
        documents[documentId] = Document({
            documentId: documentId,
            fileName: fileName,
            fileHash: fileHash,
            domain: domain,
            owner: msg.sender,
            timestamp: block.timestamp,
            isActive: true,
            ipfsHash: ipfsHash
        });
        
        // Grant access to owner
        hasAccess[documentId][msg.sender] = true;
        
        // Add to user's document list
        userDocuments[msg.sender].push(documentId);
        
        // Create audit log
        _addAuditLog(documentId, msg.sender, "upload", "Document uploaded to blockchain");
        
        emit DocumentUploaded(documentId, msg.sender, fileHash);
    }
    
    /**
     * Update an existing document (creates new version)
     */
    function updateDocument(
        string memory documentId,
        string memory newFileHash,
        string memory updateReason
    ) external onlyDocumentOwner(documentId) documentExists(documentId) {
        require(bytes(newFileHash).length > 0, "New file hash required");
        
        Document storage doc = documents[documentId];
        
        // Create version record
        DocumentVersion memory newVersion = DocumentVersion({
            documentId: documentId,
            fileHash: newFileHash,
            version: documentVersions[documentId].length + 1,
            updatedBy: msg.sender,
            timestamp: block.timestamp,
            updateReason: updateReason
        });
        
        documentVersions[documentId].push(newVersion);
        
        // Update main document hash
        doc.fileHash = newFileHash;
        
        // Create audit log
        _addAuditLog(documentId, msg.sender, "update", updateReason);
        
        emit DocumentUpdated(documentId, msg.sender, newVersion.version);
    }
    
    /**
     * Share document with another user
     */
    function shareDocument(
        string memory documentId,
        address recipient,
        string memory accessLevel
    ) external onlyDocumentOwner(documentId) documentExists(documentId) {
        require(recipient != address(0), "Invalid recipient address");
        require(!hasAccess[documentId][recipient], "User already has access");
        
        // Create share record
        ShareRecord memory shareRecord = ShareRecord({
            documentId: documentId,
            sharedBy: msg.sender,
            sharedWith: recipient,
            timestamp: block.timestamp,
            accessLevel: accessLevel,
            isActive: true
        });
        
        shareRecords[documentId].push(shareRecord);
        
        // Grant access
        hasAccess[documentId][recipient] = true;
        sharedWithUser[recipient].push(documentId);
        
        // Create audit log
        string memory details = string(abi.encodePacked("Shared with access level: ", accessLevel));
        _addAuditLog(documentId, msg.sender, "share", details);
        
        emit DocumentShared(documentId, msg.sender, recipient);
    }
    
    /**
     * Revoke access to a document
     */
    function revokeAccess(
        string memory documentId,
        address user
    ) external onlyDocumentOwner(documentId) documentExists(documentId) {
        require(hasAccess[documentId][user], "User does not have access");
        require(user != msg.sender, "Cannot revoke access from owner");
        
        // Revoke access
        hasAccess[documentId][user] = false;
        
        // Mark share records as inactive
        ShareRecord[] storage shares = shareRecords[documentId];
        for (uint i = 0; i < shares.length; i++) {
            if (shares[i].sharedWith == user && shares[i].isActive) {
                shares[i].isActive = false;
            }
        }
        
        // Remove from user's shared documents
        _removeFromSharedList(user, documentId);
        
        // Create audit log
        _addAuditLog(documentId, msg.sender, "revoke", "Access revoked");
        
        emit DocumentAccessRevoked(documentId, msg.sender, user);
    }
    
    /**
     * Verify document integrity
     */
    function verifyDocument(
        string memory documentId,
        string memory currentFileHash
    ) external view documentExists(documentId) returns (bool isValid, string memory storedHash) {
        Document memory doc = documents[documentId];
        return (
            keccak256(abi.encodePacked(doc.fileHash)) == keccak256(abi.encodePacked(currentFileHash)),
            doc.fileHash
        );
    }
    
    /**
     * Get document details
     */
    function getDocument(string memory documentId) 
        external 
        view 
        documentExists(documentId) 
        returns (Document memory) {
        require(hasAccess[documentId][msg.sender], "Access denied");
        return documents[documentId];
    }
    
    /**
     * Get document versions
     */
    function getDocumentVersions(string memory documentId)
        external
        view
        documentExists(documentId)
        returns (DocumentVersion[] memory) {
        require(hasAccess[documentId][msg.sender], "Access denied");
        return documentVersions[documentId];
    }
    
    /**
     * Get document share records
     */
    function getShareRecords(string memory documentId)
        external
        view
        documentExists(documentId)
        returns (ShareRecord[] memory) {
        require(hasAccess[documentId][msg.sender] || documents[documentId].owner == msg.sender, "Access denied");
        return shareRecords[documentId];
    }
    
    /**
     * Get audit logs for a document
     */
    function getAuditLogs(string memory documentId)
        external
        view
        documentExists(documentId)
        returns (AuditLog[] memory) {
        require(hasAccess[documentId][msg.sender] || documents[documentId].owner == msg.sender, "Access denied");
        return auditLogs[documentId];
    }
    
    /**
     * Get user's documents
     */
    function getUserDocuments(address user) external view returns (string[] memory) {
        return userDocuments[user];
    }
    
    /**
     * Get documents shared with user
     */
    function getSharedDocuments(address user) external view returns (string[] memory) {
        return sharedWithUser[user];
    }
    
    /**
     * Check if user has access to document
     */
    function checkAccess(string memory documentId, address user) external view returns (bool) {
        return hasAccess[documentId][user];
    }
    
    // Internal functions
    function _addAuditLog(
        string memory documentId,
        address actor,
        string memory action,
        string memory details
    ) internal {
        AuditLog memory logEntry = AuditLog({
            documentId: documentId,
            actor: actor,
            action: action,
            timestamp: block.timestamp,
            details: details
        });
        
        auditLogs[documentId].push(logEntry);
    }
    
    function _removeFromSharedList(address user, string memory documentId) internal {
        string[] storage userShared = sharedWithUser[user];
        for (uint i = 0; i < userShared.length; i++) {
            if (keccak256(abi.encodePacked(userShared[i])) == keccak256(abi.encodePacked(documentId))) {
                userShared[i] = userShared[userShared.length - 1];
                userShared.pop();
                break;
            }
        }
    }
}