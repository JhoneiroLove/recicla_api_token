package com.upao.recicla.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResult {
    private boolean success;
    private String transactionHash;
    private Long blockNumber;
    private String errorMessage;

    public static TransactionResult success(String txHash, Long blockNumber) {
        return TransactionResult.builder()
                .success(true)
                .transactionHash(txHash)
                .blockNumber(blockNumber)
                .build();
    }

    public static TransactionResult failure(String errorMessage) {
        return TransactionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}