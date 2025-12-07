package com.upao.recicla.blockchain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para representar una propuesta de actividad de reciclaje en blockchain
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPropuesta {

    private Long actividadId;
    private String usuarioWallet;
    private Integer pesoKg;
    private String tipoMaterial;
    private String evidenciaIPFS;
    private BigDecimal tokensCalculados;
    private Integer aprobaciones;
    private Boolean ejecutada;
    private Boolean rechazada;
    private String transactionHash;
    private Long blockNumber;
}
