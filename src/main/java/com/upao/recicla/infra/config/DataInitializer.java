package com.upao.recicla.infra.config;

import com.upao.recicla.domain.entity.NivelUsuario;
import com.upao.recicla.domain.entity.Rol;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para crear usuario administrador por defecto
 * Se ejecuta automáticamente al arrancar la aplicación
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearAdministrador();
        crearValidadores();
        actualizarNivelesNulos();
    }

    /**
     * Crea el usuario administrador si no existe
     */
    private void crearAdministrador() {
        var adminList = usuarioRepository.findByRol(Rol.ADMINISTRADOR);

        if (adminList.isEmpty()) {
            log.info("🔧 Inicializando datos: Creando usuario administrador...");

            // Account #0 de Hardhat (SIEMPRE LA MISMA con mnemonic determinista)
            // Ver ACCOUNTS.md en recicla-upao-token para más detalles
            String adminWallet = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";

            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .edad("30")
                    .telefono("999999999")
                    .correo("admin@reciclaupao.edu.pe")
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .dni("00000000")
                    .walletAddress(adminWallet)
                    .rol(Rol.ADMINISTRADOR)
                    .nivel(NivelUsuario.PLATA)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(admin);

            log.info("✅ Usuario administrador creado exitosamente");
            log.info("   👤 Username: admin");
            log.info("   🔑 Password: admin123");
            log.info("   💼 Wallet:   {}", adminWallet);
            log.info("   📧 Correo:   admin@reciclaupao.edu.pe");
        } else {
            log.info("ℹ️  Usuario administrador ya existe");
        }
    }

    /**
     * Crea los usuarios validadores (ONGs) si no existen
     */
    private void crearValidadores() {
        // Validador 1 - Account #2 de Hardhat
        String validador1Wallet = "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC";
        var validador1Exist = usuarioRepository.findByWalletAddress(validador1Wallet);

        if (validador1Exist.isEmpty()) {
            log.info("🔧 Creando Validador 1 (ONG Ambiental)...");

            Usuario validador1 = Usuario.builder()
                    .nombre("ONG Ambiental UPAO")
                    .edad("0")
                    .telefono("987654321")
                    .correo("ong1@reciclaupao.edu.pe")
                    .username("ong1")
                    .password(passwordEncoder.encode("ong123"))
                    .dni("11111111")
                    .walletAddress(validador1Wallet)
                    .rol(Rol.ONG)
                    .nivel(NivelUsuario.ORO)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(validador1);

            log.info("✅ Validador 1 creado exitosamente");
            log.info("   👤 Username: ong1");
            log.info("   🔑 Password: ong123");
            log.info("   💼 Wallet:   {}", validador1Wallet);
        } else {
            log.info("ℹ️  Validador 1 ya existe");
        }

        // Validador 2 - Account #3 de Hardhat
        String validador2Wallet = "0x90F79bf6EB2c4f870365E785982E1f101E93b906";
        var validador2Exist = usuarioRepository.findByWalletAddress(validador2Wallet);

        if (validador2Exist.isEmpty()) {
            log.info("🔧 Creando Validador 2 (Centro de Acopio)...");

            Usuario validador2 = Usuario.builder()
                    .nombre("Centro de Acopio UPAO")
                    .edad("0")
                    .telefono("987654322")
                    .correo("ong2@reciclaupao.edu.pe")
                    .username("ong2")
                    .password(passwordEncoder.encode("ong123"))
                    .dni("22222222")
                    .walletAddress(validador2Wallet)
                    .rol(Rol.ONG)
                    .nivel(NivelUsuario.ORO)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(validador2);

            log.info("✅ Validador 2 creado exitosamente");
            log.info("   👤 Username: ong2");
            log.info("   🔑 Password: ong123");
            log.info("   💼 Wallet:   {}", validador2Wallet);
        } else {
            log.info("ℹ️  Validador 2 ya existe");
        }
    }

    /**
     * Actualiza el nivel de usuarios que tienen nivel null
     */
    private void actualizarNivelesNulos() {
        var todosUsuarios = usuarioRepository.findAll();
        todosUsuarios.forEach(usuario -> {
            if (usuario.getNivel() == null) {
                usuario.setNivel(NivelUsuario.PLATA);
                usuarioRepository.save(usuario);
                log.info("🔄 Actualizado nivel de usuario: {} → PLATA", usuario.getUsername());
            }
        });
    }
}
