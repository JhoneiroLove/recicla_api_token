package com.upao.recicla.infra.repository;

import com.upao.recicla.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    // Validar wallet única
    boolean existsByWalletAddress(String walletAddress);

    // Validar DNI único
    boolean existsByDni(String dni);

    // Buscar por wallet
    Optional<Usuario> findByWalletAddress(String walletAddress);
}