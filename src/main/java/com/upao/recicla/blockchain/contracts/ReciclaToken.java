package com.upao.recicla.blockchain.contracts;

import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.TypeReference;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class ReciclaToken extends Contract {

    public static final String BINARY = "";

    protected ReciclaToken(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    protected ReciclaToken(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static ReciclaToken load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new ReciclaToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> addToWhitelist(String user, String dniHash) {
        final Function function = new Function(
                "addToWhitelist",
                Arrays.asList(new Address(user), new Utf8String(dniHash)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> mintForActivity(String to, BigInteger amount, String reason) {
        final Function function = new Function(
                "mintForActivity",
                Arrays.asList(new Address(to), new Uint256(amount), new Utf8String(reason)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> burnForRedemption(String from, BigInteger amount, String reason) {
        final Function function = new Function(
                "burnForRedemption",
                Arrays.asList(new Address(from), new Uint256(amount), new Utf8String(reason)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String owner) {
        final Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(owner)),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<Boolean> isWhitelisted(String user) {
        final Function function = new Function(
                "isWhitelisted",
                Arrays.asList(new Address(user)),
                Arrays.asList(new TypeReference<Bool>() {
                }));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<BigInteger> totalTokensEarnedByUser(String user) {
        final Function function = new Function(
                "totalTokensEarnedByUser",
                Arrays.asList(new Address(user)),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> totalTokensSpentByUser(String user) {
        final Function function = new Function(
                "totalTokensSpentByUser",
                Arrays.asList(new Address(user)),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> proponerActividad(String usuario, BigInteger pesoKg, String tipoMaterial,
            String evidenciaIPFS) {
        final Function function = new Function(
                "proponerActividad",
                Arrays.asList(new Address(usuario), new Uint256(pesoKg), new Utf8String(tipoMaterial),
                        new Utf8String(evidenciaIPFS)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> aprobarActividad(BigInteger actividadId) {
        final Function function = new Function(
                "aprobarActividad",
                Arrays.asList(new Uint256(actividadId)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> rechazarActividad(BigInteger actividadId, String razon) {
        final Function function = new Function(
                "rechazarActividad",
                Arrays.asList(new Uint256(actividadId), new Utf8String(razon)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<DynamicStruct> getActividad(BigInteger actividadId) {
        final Function function = new Function(
                "getActividad",
                Arrays.asList(new Uint256(actividadId)),
                Arrays.asList(new TypeReference<DynamicStruct>() {
                }));
        return executeRemoteCallSingleValueReturn(function, DynamicStruct.class);
    }

    public RemoteCall<BigInteger> contadorActividades() {
        final Function function = new Function(
                "contadorActividades",
                Arrays.asList(),
                Arrays.asList(new TypeReference<Uint256>() {
                }));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }
}