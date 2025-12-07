package com.upao.recicla.blockchain.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class ProponerActividadRequest {

    @NotBlank(message = "Wallet del usuario es requerido")
    @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Wallet address inválido")
    private String usuarioWallet;

    @NotNull(message = "Peso es requerido")
    @Min(value = 1, message = "Peso debe ser mayor a 0")
    private Integer pesoKg;

    @NotBlank(message = "Tipo de material es requerido")
    @Pattern(regexp = "^(plastico|papel|vidrio|metal|carton|organico)$", message = "Tipo de material debe ser: plastico, papel, vidrio, metal, carton u organico")
    private String tipoMaterial;

    @NotBlank(message = "Evidencia IPFS es requerida")
    private String evidenciaIPFS;
}
