package com.upao.recicla.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainBalance {
    private BigDecimal currentBalance;
    private BigDecimal totalEarned;
    private BigDecimal totalSpent;
    private boolean isWhitelisted;
}