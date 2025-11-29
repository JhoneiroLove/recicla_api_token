package com.upao.recicla.controller;

import com.upao.recicla.blockchain.dto.BlockchainBalance;
import com.upao.recicla.blockchain.service.BlockchainService;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blockchain")
@RequiredArgsConstructor
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
}