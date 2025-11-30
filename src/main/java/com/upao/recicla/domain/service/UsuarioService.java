package com.upao.recicla.domain.service;

import com.upao.recicla.blockchain.dto.TransactionResult;
import com.upao.recicla.blockchain.service.BlockchainService;
import com.upao.recicla.domain.dto.usuarioDto.DatosRegistroUsuarioConWallet;
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
    public TokenResponse addUsuario(DatosRegistroUsuarioConWallet datos) {
        // Validar que la wallet no esté ya registrada
        if (usuarioRepository.existsByWalletAddress(datos.walletAddress())) {
            throw new RuntimeException("Esta wallet ya está registrada en el sistema");
        }

        // Validar que el DNI no esté registrado
        if (usuarioRepository.existsByDni(datos.dni())) {
            throw new RuntimeException("Este DNI ya está registrado");
        }

        // Validar formato de wallet address
        if (!datos.walletAddress().matches("^0x[a-fA-F0-9]{40}$")) {
            throw new RuntimeException("Formato de wallet address inválido");
        }

        log.info("Registrando usuario con wallet de MetaMask: {}", datos.walletAddress());

        Usuario user = Usuario.builder()
                .username(datos.username())
                .password(passwordEncoder.encode(datos.password()))
                .nombre(datos.nombre())
                .edad(datos.edad())
                .telefono(datos.telefono())
                .correo(datos.correo())
                .puntos(0.0)
                .dni(datos.dni())
                .walletAddress(datos.walletAddress())  // Wallet del usuario
                .rol(Rol.PARTICIPANTE)
                .nivel(NivelUsuario.PLATA)
                .build();

        usuarioRepository.save(user);

        // Registrar en blockchain (backend paga gas)
        if (blockchainService != null) {
            try {
                log.info("Registrando usuario en blockchain whitelist...");
                TransactionResult result = blockchainService.registerUserOnChain(
                        datos.walletAddress(),
                        datos.dni()
                );

                if (result.isSuccess()) {
                    log.info("Usuario registrado en blockchain. TX: {}", result.getTransactionHash());
                } else {
                    log.error("Error en blockchain: {}", result.getErrorMessage());
                    // Decidir si falla todo o continúa
                    throw new RuntimeException("Error registrando en blockchain: " + result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("Error al registrar en blockchain", e);
                throw new RuntimeException("Error al registrar en blockchain: " + e.getMessage());
            }
        } else {
            log.warn("Blockchain deshabilitado");
        }

        String token = jwtService.getToken(user, user);
        return TokenResponse.builder()
                .token(token)
                .build();
    }

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