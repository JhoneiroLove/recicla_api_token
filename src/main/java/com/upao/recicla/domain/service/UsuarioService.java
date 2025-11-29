package com.upao.recicla.domain.service;

import com.upao.recicla.blockchain.dto.TransactionResult;
import com.upao.recicla.blockchain.service.BlockchainService;
import com.upao.recicla.domain.entity.NivelUsuario;
import com.upao.recicla.domain.entity.Rol;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.repository.UsuarioRepository;
import com.upao.recicla.infra.security.JwtService;
import com.upao.recicla.infra.security.LoginRequest;
import com.upao.recicla.infra.security.TokenResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import org.web3j.crypto.ECKeyPair;

import java.security.SecureRandom;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired(required = false)
    private BlockchainService blockchainService;

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Usuario user = usuarioRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.getToken(user, user);
        return TokenResponse.builder()
                .token(token)
                .build();
    }

    @Transactional
    public TokenResponse addUsuario(Usuario usuario) {
        // generar wallet address
        String walletAddress = generateWalletAddress();
        log.info("Wallet generada para {}: {}", usuario.getUsername(), walletAddress);

        Usuario user = Usuario.builder()
                .username(usuario.getUsername())
                .password(passwordEncoder.encode(usuario.getPassword()))
                .nombre(usuario.getNombre())
                .edad(usuario.getEdad())
                .telefono(usuario.getTelefono())
                .correo(usuario.getCorreo())
                .puntos(0.0)
                .dni(usuario.getDni())
                .walletAddress(walletAddress)
                .rol(Rol.PARTICIPANTE)
                .nivel(NivelUsuario.PLATA)
                .build();

        usuarioRepository.save(user);

        // registrar en blockchain
        if (blockchainService != null) {
            try {
                log.info("Registrando usuario en blockchain...");
                TransactionResult result = blockchainService.registerUserOnChain(
                        walletAddress,
                        usuario.getDni()
                );

                if (result.isSuccess()) {
                    log.info("Usuario registrado en blockchain. TX: {}", result.getTransactionHash());
                } else {
                    log.warn("⚠No se pudo registrar en blockchain: {}", result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("Error al registrar en blockchain", e);
                // No falla el registro si blockchain falla
            }
        } else {
            log.info("Blockchain deshabilitado, usuario registrado solo en DB");
        }

        String token = jwtService.getToken(user, user);
        return TokenResponse.builder()
                .token(token)
                .build();
    }

    // generar wallet address
    private String generateWalletAddress() {
        try {
            // Generar un par de claves aleatorio
            SecureRandom random = new SecureRandom();
            byte[] privateKeyBytes = new byte[32];
            random.nextBytes(privateKeyBytes);

            ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);
            String address = "0x" + Keys.getAddress(keyPair);

            return address;
        } catch (Exception e) {
            log.error("Error generando wallet address", e);
            throw new RuntimeException("No se pudo generar wallet address");
        }
    }
    // ========================================================

    public Page<Usuario> getAllUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Usuario getReferenceById(Long id) {
        return usuarioRepository.getReferenceById(id);
    }

    @Transactional
    public void cambiarRolUsuario(Long usuarioId, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
    }

    public void updateUsuario(Usuario usuario, Long id) {
        Usuario usuarioExists = usuarioRepository.findById(id)
                .orElse(new Usuario());
        usuarioExists.setRol(usuario.getRol());
        usuarioRepository.save(usuarioExists);
    }

    public void deleteUsuarioById(Long id){
        usuarioRepository.deleteById(id);
    }
}