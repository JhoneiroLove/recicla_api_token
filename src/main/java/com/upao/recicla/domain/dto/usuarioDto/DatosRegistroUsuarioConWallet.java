package com.upao.recicla.domain.dto.usuarioDto;

import jakarta.validation.constraints.*;

import java.io.Serializable;

public record DatosRegistroUsuarioConWallet(
        @NotBlank String nombre,
        String edad,
        @Size(min = 0, max = 9) String telefono,
        @Email @NotBlank String correo,
        @Pattern(regexp = "\\d{8}") String dni,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank @Pattern(regexp = "^0x[a-fA-F0-9]{40}$", message = "Wallet address inválida")
        String walletAddress
) implements Serializable {
}