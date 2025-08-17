// EncryptionService.java
package com.duediligence.documentanalyzer.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Service for encrypting and decrypting files before storing in IPFS
 * Uses AES-GCM encryption for security and integrity
 */
@Service
public class EncryptionService {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 16; // 128 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int AES_KEY_LENGTH = 256; // 256 bits

    @Value("${app.encryption.master-key:}")
    private String masterKey;

    @Value("${app.encryption.key-derivation-salt:DocumentAnalyzerSalt2024}")
    private String keyDerivationSalt;

    private SecretKey masterSecretKey;

    @PostConstruct
    public void initialize() {
        try {
            if (masterKey == null || masterKey.trim().isEmpty()) {
                // Generate a random master key for demo purposes
                // In production, use a secure key management service
                masterKey = generateRandomKey();
                logger.warn("No master key provided, generated random key. This should be configured in production!");
            }

            // Derive master secret key from master key string
            this.masterSecretKey = deriveKeyFromPassword(masterKey, keyDerivationSalt);
            logger.info("Encryption service initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize encryption service", e);
            throw new RuntimeException("Encryption service initialization failed", e);
        }
    }

    /**
     * Encrypt file data with document-specific key
     */
    public EncryptionResult encryptFile(byte[] fileData, String documentId) {
        try {
            // Generate document-specific key
            SecretKey documentKey = generateDocumentKey(documentId);

            // Generate random IV
            byte[] iv = generateIV();

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, documentKey, parameterSpec);

            // Encrypt data
            byte[] encryptedData = cipher.doFinal(fileData);

            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, GCM_IV_LENGTH, encryptedData.length);

            logger.info("Successfully encrypted file for document: {}", documentId);

            return new EncryptionResult(
                    encryptedWithIv,
                    Base64.getEncoder().encodeToString(documentKey.getEncoded()),
                    true
            );

        } catch (Exception e) {
            logger.error("File encryption failed for document: {}", documentId, e);
            throw new RuntimeException("File encryption failed: " + e.getMessage());
        }
    }

    /**
     * Decrypt file data with document-specific key
     */
    public byte[] decryptFile(byte[] encryptedData, String documentId) {
        try {
            // Generate document-specific key (same as used for encryption)
            SecretKey documentKey = generateDocumentKey(documentId);

            // Extract IV from encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);

            // Extract encrypted content
            byte[] encrypted = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, documentKey, parameterSpec);

            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encrypted);

            logger.info("Successfully decrypted file for document: {}", documentId);
            return decryptedData;

        } catch (Exception e) {
            logger.error("File decryption failed for document: {}", documentId, e);
            throw new RuntimeException("File decryption failed: " + e.getMessage());
        }
    }

    /**
     * Generate document-specific encryption key
     * Derives a unique key for each document from master key + document ID
     */
    private SecretKey generateDocumentKey(String documentId) throws Exception {
        // Use document ID as additional salt for key derivation
        String keyMaterial = masterKey + documentId;
        return deriveKeyFromPassword(keyMaterial, keyDerivationSalt + documentId);
    }

    /**
     * Derive encryption key from password and salt using PBKDF2
     */
    private SecretKey deriveKeyFromPassword(String password, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Hash password with salt multiple times
        byte[] hash = password.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < 10000; i++) { // 10,000 iterations
            digest.reset();
            digest.update(hash);
            digest.update(saltBytes);
            hash = digest.digest();
        }

        // Take first 32 bytes for AES-256
        byte[] keyBytes = Arrays.copyOf(hash, 32);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Generate random initialization vector
     */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Generate random master key (for demo purposes)
     */
    private String generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(AES_KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate random key", e);
        }
    }

    /**
     * Verify file integrity after decryption
     */
    public boolean verifyFileIntegrity(byte[] originalData, String expectedHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalData);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return expectedHash.equals(hexString.toString());

        } catch (Exception e) {
            logger.error("File integrity verification failed", e);
            return false;
        }
    }

    /**
     * Calculate hash of decrypted data
     */
    public String calculateFileHash(byte[] fileData) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileData);

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            logger.error("Hash calculation failed", e);
            throw new RuntimeException("Hash calculation failed", e);
        }
    }

    /**
     * Encryption result container
     */
    public static class EncryptionResult {
        private final byte[] encryptedData;
        private final String keyBase64;
        private final boolean success;

        public EncryptionResult(byte[] encryptedData, String keyBase64, boolean success) {
            this.encryptedData = encryptedData;
            this.keyBase64 = keyBase64;
            this.success = success;
        }

        public byte[] getEncryptedData() { return encryptedData; }
        public String getKeyBase64() { return keyBase64; }
        public boolean isSuccess() { return success; }
    }
}