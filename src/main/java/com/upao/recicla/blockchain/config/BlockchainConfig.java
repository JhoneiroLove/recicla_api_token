package com.upao.recicla.blockchain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
@ConditionalOnProperty(name = "blockchain.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class BlockchainConfig {

    @Value("${blockchain.rpc-url}")
    private String rpcUrl;

    @Value("${blockchain.backend-private-key}")
    private String privateKey;

    @Value("${blockchain.token-address}")
    private String tokenAddress;

    @Value("${blockchain.gas-price:20000000000}")
    private String gasPrice;

    @Value("${blockchain.gas-limit:3000000}")
    private String gasLimit;

    @Bean
    public Web3j web3j() {
        log.info("🔗 Conectando a blockchain en: {}", rpcUrl);
        Web3j web3j = Web3j.build(new HttpService(rpcUrl));

        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("✅ Conectado a: {}", clientVersion);
        } catch (Exception e) {
            log.error("❌ Error conectando a blockchain", e);
        }

        return web3j;
    }

    @Bean
    public Credentials backendCredentials() {
        log.info("🔑 Cargando credenciales del backend");
        return Credentials.create(privateKey);
    }

    @Bean
    public StaticGasProvider gasProvider() {
        return new StaticGasProvider(
                new BigInteger(gasPrice),
                new BigInteger(gasLimit)
        );
    }

    @Bean
    public String tokenContractAddress() {
        return tokenAddress;
    }
}