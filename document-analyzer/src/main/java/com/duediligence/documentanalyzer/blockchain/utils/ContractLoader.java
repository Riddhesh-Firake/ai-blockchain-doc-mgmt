// ContractLoader.java
package com.duediligence.documentanalyzer.blockchain.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ContractLoader {

    private static final Logger logger = LoggerFactory.getLogger(ContractLoader.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load contract ABI from resources
     */
    public String loadContractABI(String contractName) {
        try {
            ClassPathResource resource = new ClassPathResource("contracts/" + contractName + "-abi.json");
            return new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            logger.error("Failed to load ABI for contract: {}", contractName, e);
            throw new RuntimeException("Failed to load contract ABI", e);
        }
    }

    /**
     * Load contract bytecode from resources
     */
    public String loadContractBytecode(String contractName) {
        try {
            ClassPathResource resource = new ClassPathResource("contracts/" + contractName + "-bytecode.txt");
            String bytecode = new String(resource.getInputStream().readAllBytes()).trim();

            // Ensure bytecode starts with 0x
            if (!bytecode.startsWith("0x")) {
                bytecode = "0x" + bytecode;
            }

            return bytecode;
        } catch (IOException e) {
            logger.error("Failed to load bytecode for contract: {}", contractName, e);
            throw new RuntimeException("Failed to load contract bytecode", e);
        }
    }

    /**
     * Load full contract compilation output from Remix
     */
    public ContractCompilation loadContractCompilation(String contractName) {
        try {
            ClassPathResource resource = new ClassPathResource("contracts/" + contractName + "-compilation.json");
            JsonNode compilation = objectMapper.readTree(resource.getInputStream());

            // Extract ABI and bytecode from Remix compilation output
            JsonNode contracts = compilation.path("contracts").path(contractName + ".sol").path(contractName);
            String abi = contracts.path("abi").toString();
            String bytecode = contracts.path("evm").path("bytecode").path("object").asText();

            if (!bytecode.startsWith("0x")) {
                bytecode = "0x" + bytecode;
            }

            return new ContractCompilation(abi, bytecode);

        } catch (IOException e) {
            logger.error("Failed to load compilation for contract: {}", contractName, e);
            throw new RuntimeException("Failed to load contract compilation", e);
        }
    }

    /**
     * Container for contract compilation data
     */
    public static class ContractCompilation {
        private final String abi;
        private final String bytecode;

        public ContractCompilation(String abi, String bytecode) {
            this.abi = abi;
            this.bytecode = bytecode;
        }

        public String getAbi() { return abi; }
        public String getBytecode() { return bytecode; }
    }
}