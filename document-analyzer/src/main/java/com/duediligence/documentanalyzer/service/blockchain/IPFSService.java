// IPFSService.java
package com.duediligence.documentanalyzer.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * Service for IPFS (InterPlanetary File System) operations
 * Provides decentralized file storage using Pinata or Infura IPFS
 */
@Service
public class IPFSService {

    private static final Logger logger = LoggerFactory.getLogger(IPFSService.class);

    @Value("${app.ipfs.enabled:true}")
    private boolean ipfsEnabled;

    @Value("${app.ipfs.provider:pinata}")
    private String ipfsProvider; // pinata, infura, or local

    // Pinata configuration
    @Value("${app.ipfs.pinata.api-key:}")
    private String pinataApiKey;

    @Value("${app.ipfs.pinata.secret-key:}")
    private String pinataSecretKey;

    @Value("${app.ipfs.pinata.jwt:}")
    private String pinataJWT;

    // Infura configuration
    @Value("${app.ipfs.infura.project-id:}")
    private String infuraProjectId;

    @Value("${app.ipfs.infura.project-secret:}")
    private String infuraProjectSecret;

    // Local IPFS node configuration
    @Value("${app.ipfs.local.api-url:http://localhost:5001}")
    private String localIpfsApiUrl;

    @Value("${app.ipfs.gateway.url:https://gateway.pinata.cloud/ipfs/}")
    private String ipfsGatewayUrl;

    private final RestTemplate restTemplate;

    public IPFSService() {
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void initialize() {
        if (!ipfsEnabled) {
            logger.info("IPFS service is disabled");
            return;
        }

        logger.info("Initializing IPFS service with provider: {}", ipfsProvider);

        switch (ipfsProvider.toLowerCase()) {
            case "pinata":
                validatePinataConfig();
                break;
            case "infura":
                validateInfuraConfig();
                break;
            case "local":
                validateLocalConfig();
                break;
            default:
                throw new IllegalArgumentException("Unsupported IPFS provider: " + ipfsProvider);
        }
    }

    /**
     * Upload encrypted file to IPFS
     */
    public IPFSUploadResult uploadFile(byte[] encryptedData, String fileName, String documentId) {
        if (!ipfsEnabled) {
            throw new RuntimeException("IPFS is not enabled");
        }

        try {
            switch (ipfsProvider.toLowerCase()) {
                case "pinata":
                    return uploadToPinata(encryptedData, fileName, documentId);
                case "infura":
                    return uploadToInfura(encryptedData, fileName, documentId);
                case "local":
                    return uploadToLocal(encryptedData, fileName, documentId);
                default:
                    throw new RuntimeException("Unsupported IPFS provider: " + ipfsProvider);
            }
        } catch (Exception e) {
            logger.error("Failed to upload file to IPFS", e);
            throw new RuntimeException("IPFS upload failed: " + e.getMessage());
        }
    }

    /**
     * Download file from IPFS
     */
    public byte[] downloadFile(String ipfsHash) {
        if (!ipfsEnabled) {
            throw new RuntimeException("IPFS is not enabled");
        }

        try {
            String downloadUrl = ipfsGatewayUrl + ipfsHash;

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    downloadUrl,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Successfully downloaded file from IPFS: {}", ipfsHash);
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to download from IPFS: " + response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("Failed to download file from IPFS: {}", ipfsHash, e);
            throw new RuntimeException("IPFS download failed: " + e.getMessage());
        }
    }

    /**
     * Delete/Unpin file from IPFS (Pinata only)
     */
    public boolean deleteFile(String ipfsHash) {
        if (!ipfsEnabled || !ipfsProvider.equals("pinata")) {
            return false;
        }

        try {
            String url = "https://api.pinata.cloud/pinning/unpin/" + ipfsHash;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + pinataJWT);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    entity,
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (Exception e) {
            logger.error("Failed to delete file from IPFS: {}", ipfsHash, e);
            return false;
        }
    }

    // Private methods for different providers

    private IPFSUploadResult uploadToPinata(byte[] data, String fileName, String documentId) {
        String url = "https://api.pinata.cloud/pinning/pinFileToIPFS";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + pinataJWT);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(data, fileName));

        // Add metadata
        String metadata = String.format(
                "{\"name\":\"%s\",\"keyvalues\":{\"documentId\":\"%s\",\"uploadedAt\":\"%d\"}}",
                fileName, documentId, System.currentTimeMillis()
        );
        body.add("pinataMetadata", metadata);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            String ipfsHash = (String) responseBody.get("IpfsHash");
            logger.info("Successfully uploaded to Pinata IPFS: {}", ipfsHash);
            return new IPFSUploadResult(ipfsHash, ipfsGatewayUrl + ipfsHash, true);
        } else {
            throw new RuntimeException("Pinata upload failed: " + response.getStatusCode());
        }
    }

    private IPFSUploadResult uploadToInfura(byte[] data, String fileName, String documentId) {
        String url = "https://ipfs.infura.io:5001/api/v0/add";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBasicAuth(infuraProjectId, infuraProjectSecret);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(data, fileName));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            String ipfsHash = (String) responseBody.get("Hash");
            logger.info("Successfully uploaded to Infura IPFS: {}", ipfsHash);
            return new IPFSUploadResult(ipfsHash, "https://ipfs.io/ipfs/" + ipfsHash, true);
        } else {
            throw new RuntimeException("Infura upload failed: " + response.getStatusCode());
        }
    }

    private IPFSUploadResult uploadToLocal(byte[] data, String fileName, String documentId) {
        String url = localIpfsApiUrl + "/api/v0/add";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(data, fileName));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            String ipfsHash = (String) responseBody.get("Hash");
            logger.info("Successfully uploaded to local IPFS: {}", ipfsHash);
            return new IPFSUploadResult(ipfsHash, "http://localhost:8080/ipfs/" + ipfsHash, true);
        } else {
            throw new RuntimeException("Local IPFS upload failed: " + response.getStatusCode());
        }
    }

    private void validatePinataConfig() {
        if (pinataJWT == null || pinataJWT.trim().isEmpty()) {
            throw new IllegalArgumentException("Pinata JWT token is required");
        }
        logger.info("Pinata IPFS configuration validated");
    }

    private void validateInfuraConfig() {
        if (infuraProjectId == null || infuraProjectId.trim().isEmpty() ||
                infuraProjectSecret == null || infuraProjectSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("Infura project ID and secret are required");
        }
        logger.info("Infura IPFS configuration validated");
    }

    private void validateLocalConfig() {
        // Test connection to local IPFS node
        try {
            String testUrl = localIpfsApiUrl + "/api/v0/version";
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("Local IPFS node connection validated");
            } else {
                throw new RuntimeException("Cannot connect to local IPFS node");
            }
        } catch (Exception e) {
            logger.warn("Local IPFS node not available, uploads will fail: {}", e.getMessage());
        }
    }

    // Helper classes
    public static class IPFSUploadResult {
        private final String ipfsHash;
        private final String gatewayUrl;
        private final boolean success;

        public IPFSUploadResult(String ipfsHash, String gatewayUrl, boolean success) {
            this.ipfsHash = ipfsHash;
            this.gatewayUrl = gatewayUrl;
            this.success = success;
        }

        public String getIpfsHash() { return ipfsHash; }
        public String getGatewayUrl() { return gatewayUrl; }
        public boolean isSuccess() { return success; }
    }

    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        private final String filename;

        public ByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}