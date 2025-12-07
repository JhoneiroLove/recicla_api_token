package com.upao.recicla.blockchain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class AprobarActividadRequest {

    @NotBlank(message = "Wallet del validador es requerido")
    private String validadorWallet;

    @NotBlank(message = "Private key del validador es requerida")
    private String validadorPrivateKey;
}
