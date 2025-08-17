// DocumentRegistry.java
package com.duediligence.documentanalyzer.blockchain.contracts;

import com.duediligence.documentanalyzer.blockchain.utils.ContractLoader;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.web3j.tuples.generated.Tuple2;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DocumentRegistry extends Contract {

    private static final Logger logger = LoggerFactory.getLogger(DocumentRegistry.class);
    private static final String CONTRACT_NAME = "DocumentRegistry";
    private static String BINARY = "";

    // Events
    public static final Event DOCUMENTUPLOADED_EVENT = new Event("DocumentUploaded",
            Arrays.asList(
                    new TypeReference<Utf8String>(true) {},  // indexed documentId
                    new TypeReference<Address>(true) {},     // indexed owner
                    new TypeReference<Utf8String>() {}       // fileHash
            ));

    public static final Event DOCUMENTUPDATED_EVENT = new Event("DocumentUpdated",
            Arrays.asList(
                    new TypeReference<Utf8String>(true) {},  // indexed documentId
                    new TypeReference<Address>(true) {},     // indexed updatedBy
                    new TypeReference<Uint256>() {}          // version
            ));

    public static final Event DOCUMENTSHARED_EVENT = new Event("DocumentShared",
            Arrays.asList(
                    new TypeReference<Utf8String>(true) {},  // indexed documentId
                    new TypeReference<Address>(true) {},     // indexed sharedBy
                    new TypeReference<Address>(true) {}      // indexed sharedWith
            ));

    public static final Event DOCUMENTACCESSREVOKED_EVENT = new Event("DocumentAccessRevoked",
            Arrays.asList(
                    new TypeReference<Utf8String>(true) {},  // indexed documentId
                    new TypeReference<Address>(true) {},     // indexed owner
                    new TypeReference<Address>(true) {}      // indexed revokedFrom
            ));

    protected DocumentRegistry(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected DocumentRegistry(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // Contract function calls
    public RemoteFunctionCall<TransactionReceipt> uploadDocument(
            String documentId,
            String fileName,
            String fileHash,
            String domain,
            String ipfsHash) {

        final Function function = new Function(
                "uploadDocument",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Utf8String(fileName),
                        new Utf8String(fileHash),
                        new Utf8String(domain),
                        new Utf8String(ipfsHash)
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateDocument(
            String documentId,
            String newFileHash,
            String updateReason) {

        final Function function = new Function(
                "updateDocument",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Utf8String(newFileHash),
                        new Utf8String(updateReason)
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> shareDocument(
            String documentId,
            String recipient,
            String accessLevel) {

        final Function function = new Function(
                "shareDocument",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Address(recipient),
                        new Utf8String(accessLevel)
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> revokeAccess(
            String documentId,
            String userAddress) {

        final Function function = new Function(
                "revokeAccess",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Address(userAddress)
                ),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Boolean> checkAccess(
            String documentId,
            String userAddress) {

        final Function function = new Function(
                "checkAccess",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Address(userAddress)
                ),
                Arrays.asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteFunctionCall<Tuple2<Boolean, String>> verifyDocument(
            String documentId,
            String currentFileHash) {

        final Function function = new Function(
                "verifyDocument",
                Arrays.asList(
                        new Utf8String(documentId),
                        new Utf8String(currentFileHash)
                ),
                Arrays.asList(
                        new TypeReference<Bool>() {},
                        new TypeReference<Utf8String>() {}
                ));
        return new RemoteFunctionCall<>(function,
                () -> {
                    List<Type> results = executeCallSingleValueReturn(function, List.class);
                    return new Tuple2<>(
                            (Boolean) results.get(0).getValue(),
                            (String) results.get(1).getValue());
                });
    }

    // Event filters
    public Flowable<DocumentUploadedEventResponse> documentUploadedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getDocumentUploadedEventFromLog(log));
    }

    public Flowable<DocumentUploadedEventResponse> documentUploadedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DOCUMENTUPLOADED_EVENT));
        return documentUploadedEventFlowable(filter);
    }

    protected DocumentUploadedEventResponse getDocumentUploadedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DOCUMENTUPLOADED_EVENT, log);
        DocumentUploadedEventResponse typedResponse = new DocumentUploadedEventResponse();
        typedResponse.log = log;
        typedResponse.documentId = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.owner = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.fileHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    // Load contract with file-based ABI and bytecode
    public static DocumentRegistry load(
            String contractAddress,
            Web3j web3j,
            ReadonlyTransactionManager credentials,
            ContractGasProvider contractGasProvider,
            ContractLoader contractLoader) {

        // Load bytecode from file
        BINARY = contractLoader.loadContractBytecode(CONTRACT_NAME);
        logger.info("Loaded contract bytecode from file");

        return new DocumentRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    // Traditional load method (for backward compatibility)
    public static DocumentRegistry load(
            String contractAddress,
            Web3j web3j,
            Credentials credentials,
            ContractGasProvider contractGasProvider) {

        logger.warn("Using traditional load method - bytecode must be set manually");
        return new DocumentRegistry(contractAddress, web3j, credentials, contractGasProvider);
    }

    // Event response classes
    public static class DocumentUploadedEventResponse extends BaseEventResponse {
        public String documentId;
        public String owner;
        public String fileHash;
    }

    public static class DocumentUpdatedEventResponse extends BaseEventResponse {
        public String documentId;
        public String updatedBy;
        public BigInteger version;
    }

    public static class DocumentSharedEventResponse extends BaseEventResponse {
        public String documentId;
        public String sharedBy;
        public String sharedWith;
    }
}