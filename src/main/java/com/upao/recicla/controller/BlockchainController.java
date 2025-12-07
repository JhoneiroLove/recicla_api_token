package com.upao.recicla.controller;

import com.upao.recicla.blockchain.dto.*;
import com.upao.recicla.blockchain.service.BlockchainService;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Credentials;

import jakarta.validation.Valid;
import java.math.BigInteger;

@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
@Slf4j
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping("/balance")
    public ResponseEntity<BlockchainBalance> getMyBalance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getWalletAddress() == null) {
            return ResponseEntity.badRequest().build();
        }

        BlockchainBalance balance = blockchainService.getUserBalance(usuario.getWalletAddress());
        return ResponseEntity.ok(balance);
    }

    /**
     * Propone una actividad de reciclaje en blockchain
     * Solo el backend (con rol PROPOSER_ROLE) puede llamar esto
     */
    @PostMapping("/actividades/proponer")
    public ResponseEntity<TransactionResult> proponerActividad(@Valid @RequestBody ProponerActividadRequest request) {
        log.info("📝 Endpoint: Proponiendo actividad para {}", request.getUsuarioWallet());

        TransactionResult result = blockchainService.proponerActividad(
                request.getUsuarioWallet(),
                request.getPesoKg(),
                request.getTipoMaterial(),
                request.getEvidenciaIPFS());

        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Validador aprueba una actividad propuesta
     * Requiere credenciales del validador con rol VALIDATOR_ROLE
     */
    @PostMapping("/actividades/{actividadId}/aprobar")
    public ResponseEntity<TransactionResult> aprobarActividad(
            @PathVariable Long actividadId,
            @Valid @RequestBody AprobarActividadRequest request) {

        log.info("✅ Endpoint: Aprobando actividad #{} por validador {}", actividadId, request.getValidadorWallet());

        try {
            // Crear credenciales del validador desde la private key
            Credentials validadorCredentials = Credentials.create(request.getValidadorPrivateKey());

            // Verificar que la wallet coincida
            if (!validadorCredentials.getAddress().equalsIgnoreCase(request.getValidadorWallet())) {
                return ResponseEntity.badRequest().body(
                        TransactionResult.failure("Wallet y private key no coinciden"));
            }

            TransactionResult result = blockchainService.aprobarActividad(
                    BigInteger.valueOf(actividadId),
                    validadorCredentials);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.internalServerError().body(result);
            }

        } catch (Exception e) {
            log.error("❌ Error creando credenciales del validador", e);
            return ResponseEntity.badRequest().body(
                    TransactionResult.failure("Private key inválida"));
        }
    }

    /**
     * Validador rechaza una actividad propuesta
     * Requiere credenciales del validador con rol VALIDATOR_ROLE
     */
    @PostMapping("/actividades/{actividadId}/rechazar")
    public ResponseEntity<TransactionResult> rechazarActividad(
            @PathVariable Long actividadId,
            @Valid @RequestBody RechazarActividadRequest request) {

        log.info("❌ Endpoint: Rechazando actividad #{} por validador {}: {}",
                actividadId, request.getValidadorWallet(), request.getRazon());

        try {
            // Crear credenciales del validador desde la private key
            Credentials validadorCredentials = Credentials.create(request.getValidadorPrivateKey());

            // Verificar que la wallet coincida
            if (!validadorCredentials.getAddress().equalsIgnoreCase(request.getValidadorWallet())) {
                return ResponseEntity.badRequest().body(
                        TransactionResult.failure("Wallet y private key no coinciden"));
            }

            TransactionResult result = blockchainService.rechazarActividad(
                    BigInteger.valueOf(actividadId),
                    request.getRazon(),
                    validadorCredentials);

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.internalServerError().body(result);
            }

        } catch (Exception e) {
            log.error("❌ Error creando credenciales del validador", e);
            return ResponseEntity.badRequest().body(
                    TransactionResult.failure("Private key inválida"));
        }
    }

    /**
     * Consulta información de una actividad propuesta
     * Público - cualquiera puede consultar el estado de una actividad
     */
    @GetMapping("/actividades/{actividadId}")
    public ResponseEntity<ActividadPropuesta> getActividad(@PathVariable Long actividadId) {
        log.info("🔍 Consultando actividad #{}", actividadId);

        ActividadPropuesta actividad = blockchainService.getActividad(BigInteger.valueOf(actividadId));

        if (actividad != null) {
            return ResponseEntity.ok(actividad);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todas las propuestas de actividades pendientes de aprobaci�n
     * Filtra las que no han sido ejecutadas ni rechazadas
     * Endpoint para panel de validadores ONG
     */
    @GetMapping("/actividades/pendientes")
    public ResponseEntity<java.util.List<ActividadPropuesta>> getPropuestasPendientes() {
        log.info(" Consultando propuestas pendientes");

        java.util.List<ActividadPropuesta> propuestas = blockchainService.getPropuestasPendientes();
        
        return ResponseEntity.ok(propuestas);
    }
}
