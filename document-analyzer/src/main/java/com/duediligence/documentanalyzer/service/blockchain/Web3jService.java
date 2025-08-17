// Web3jService.java
package com.duediligence.documentanalyzer.service.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

/**
 * Service for Web3j utilities and blockchain interaction helpers
 */
@Service
public class Web3jService {

    private static final Logger logger = LoggerFactory.getLogger(Web3jService.class);

    @Value("${app.blockchain.ganache.url:http://127.0.0.1:7545}")
    private String ganacheUrl;

    private Web3j web3j;

    /**
     * Initialize Web3j connection
     */
    public void initialize() {
        try {
            web3j = Web3j.build(new HttpService(ganacheUrl));
            logger.info("Web3j service initialized with Ganache URL: {}", ganacheUrl);
        } catch (Exception e) {
            logger.error("Failed to initialize Web3j service", e);
            throw new RuntimeException("Web3j initialization failed", e);
        }
    }

    /**
     * Check if connected to blockchain
     */
    public boolean isConnected() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber().compareTo(BigInteger.ZERO) >= 0;
        } catch (Exception e) {
            logger.error("Failed to check blockchain connection", e);
            return false;
        }
    }

    /**
     * Get current block number
     */
    public BigInteger getCurrentBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber();
        } catch (Exception e) {
            logger.error("Failed to get current block number", e);
            return BigInteger.ZERO;
        }
    }

    /**
     * Get current gas price
     */
    public BigInteger getCurrentGasPrice() {
        try {
            EthGasPrice gasPrice = web3j.ethGasPrice().send();
            return gasPrice.getGasPrice();
        } catch (Exception e) {
            logger.error("Failed to get current gas price", e);
            return BigInteger.valueOf(20_000_000_000L); // Default 20 gwei
        }
    }

    /**
     * Get account balance
     */
    public BigInteger getBalance(String address) {
        try {
            EthGetBalance balance = web3j.ethGetBalance(
                    address, DefaultBlockParameterName.LATEST).send();
            return balance.getBalance();
        } catch (Exception e) {
            logger.error("Failed to get balance for address: {}", address, e);
            return BigInteger.ZERO;
        }
    }

    /**
     * Convert Wei to Ether
     */
    public String weiToEther(BigInteger wei) {
        return org.web3j.utils.Convert.fromWei(wei.toString(), org.web3j.utils.Convert.Unit.ETHER).toString();
    }

    /**
     * Get Web3j instance
     */
    public Web3j getWeb3j() {
        if (web3j == null) {
            initialize();
        }
        return web3j;
    }

    /**
     * Get blockchain network information
     */
    public BlockchainNetworkInfo getNetworkInfo() {
        try {
            BigInteger blockNumber = getCurrentBlockNumber();
            BigInteger gasPrice = getCurrentGasPrice();
            String gasPriceEther = weiToEther(gasPrice);

            return new BlockchainNetworkInfo(
                    ganacheUrl,
                    blockNumber,
                    gasPrice,
                    gasPriceEther,
                    isConnected()
            );
        } catch (Exception e) {
            logger.error("Failed to get network info", e);
            return new BlockchainNetworkInfo(ganacheUrl, BigInteger.ZERO, BigInteger.ZERO, "0", false);
        }
    }

    /**
     * Network information container
     */
    public static class BlockchainNetworkInfo {
        private final String networkUrl;
        private final BigInteger currentBlock;
        private final BigInteger gasPrice;
        private final String gasPriceEther;
        private final boolean connected;

        public BlockchainNetworkInfo(String networkUrl, BigInteger currentBlock,
                                     BigInteger gasPrice, String gasPriceEther, boolean connected) {
            this.networkUrl = networkUrl;
            this.currentBlock = currentBlock;
            this.gasPrice = gasPrice;
            this.gasPriceEther = gasPriceEther;
            this.connected = connected;
        }

        // Getters
        public String getNetworkUrl() { return networkUrl; }
        public BigInteger getCurrentBlock() { return currentBlock; }
        public BigInteger getGasPrice() { return gasPrice; }
        public String getGasPriceEther() { return gasPriceEther; }
        public boolean isConnected() { return connected; }

        @Override
        public String toString() {
            return String.format("BlockchainNetworkInfo{url='%s', block=%s, gasPrice=%s ETH, connected=%s}",
                    networkUrl, currentBlock, gasPriceEther, connected);
        }
    }
}