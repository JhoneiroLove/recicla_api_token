package com.upao.recicla.blockchain.service;

import com.upao.recicla.blockchain.contracts.ReciclaToken;
import com.upao.recicla.blockchain.dto.BlockchainBalance;
import com.upao.recicla.blockchain.dto.TransactionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BlockchainService {

    private final Web3j web3j;
    private final Credentials backendCredentials;
    private final StaticGasProvider gasProvider;
    private final String tokenContractAddress;

    public TransactionResult registerUserOnChain(String walletAddress, String dni) {
        try {
            log.info("📝 Registrando usuario en blockchain: wallet={}", walletAddress);

            String dniHash = Hash.sha3String(dni);

            ReciclaToken contract = ReciclaToken.load(
                    tokenContractAddress,
                    web3j,
                    backendCredentials,
                    gasProvider
            );

            TransactionReceipt receipt = contract.addToWhitelist(walletAddress, dniHash).send();

            log.info("✅ Usuario registrado. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(
                    receipt.getTransactionHash(),
                    receipt.getBlockNumber().longValue()
            );

        } catch (Exception e) {
            log.error("❌ Error registrando usuario", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    public TransactionResult mintTokensForActivity(String userWallet, BigDecimal tokenAmount, String description) {
        try {
            log.info("🪙 Acuñando {} REC para {}", tokenAmount, userWallet);

            BigInteger amountInWei = Convert.toWei(tokenAmount, Convert.Unit.ETHER).toBigInteger();

            ReciclaToken contract = ReciclaToken.load(tokenContractAddress, web3j, backendCredentials, gasProvider);

            TransactionReceipt receipt = contract.mintForActivity(userWallet, amountInWei, description).send();

            log.info("✅ Tokens acuñados. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(receipt.getTransactionHash(), receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error acuñando tokens", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    public TransactionResult burnTokensForRedemption(String userWallet, BigDecimal tokenAmount, String description) {
        try {
            log.info("🔥 Quemando {} REC de {}", tokenAmount, userWallet);

            BigInteger amountInWei = Convert.toWei(tokenAmount, Convert.Unit.ETHER).toBigInteger();

            ReciclaToken contract = ReciclaToken.load(tokenContractAddress, web3j, backendCredentials, gasProvider);

            TransactionReceipt receipt = contract.burnForRedemption(userWallet, amountInWei, description).send();

            log.info("✅ Tokens quemados. TX: {}", receipt.getTransactionHash());

            return TransactionResult.success(receipt.getTransactionHash(), receipt.getBlockNumber().longValue());

        } catch (Exception e) {
            log.error("❌ Error quemando tokens", e);
            return TransactionResult.failure(e.getMessage());
        }
    }

    public BlockchainBalance getUserBalance(String walletAddress) {
        try {
            ReciclaToken contract = ReciclaToken.load(tokenContractAddress, web3j, backendCredentials, gasProvider);

            BigInteger balance = contract.balanceOf(walletAddress).send();
            BigInteger earned = contract.totalTokensEarnedByUser(walletAddress).send();
            BigInteger spent = contract.totalTokensSpentByUser(walletAddress).send();
            boolean isWhitelisted = contract.isWhitelisted(walletAddress).send();

            return BlockchainBalance.builder()
                    .currentBalance(Convert.fromWei(balance.toString(), Convert.Unit.ETHER))
                    .totalEarned(Convert.fromWei(earned.toString(), Convert.Unit.ETHER))
                    .totalSpent(Convert.fromWei(spent.toString(), Convert.Unit.ETHER))
                    .isWhitelisted(isWhitelisted)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error consultando balance", e);
            return null;
        }
    }
}