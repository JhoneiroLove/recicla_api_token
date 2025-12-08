package com.upao.recicla.infra.config;

import com.upao.recicla.domain.entity.NivelUsuario;
import com.upao.recicla.domain.entity.Rol;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para crear usuarios por defecto
 * Se ejecuta automáticamente al arrancar la aplicación
 * 
 * Las wallets se leen desde application.properties para permitir
 * configuración flexible según el entorno de desarrollo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyección de wallets desde application.properties
    @Value("${admin.wallet:0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266}")
    private String adminWallet;

    @Value("${validator1.wallet:0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC}")
    private String validator1Wallet;

    @Value("${validator2.wallet:0x90F79bf6EB2c4f870365E785982E1f101E93b906}")
    private String validator2Wallet;

    @Value("${centro.wallet:0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65}")
    private String centroWallet;

    @Override
    public void run(String... args) {
        crearAdministrador();
        crearValidadores();
        crearCentroAcopio();
        actualizarNivelesNulos();
    }

    /**
     * Crea el usuario administrador si no existe
     * Lee la wallet desde application.properties (admin.wallet)
     */
    private void crearAdministrador() {
        var adminList = usuarioRepository.findByRol(Rol.ADMINISTRADOR);

        if (adminList.isEmpty()) {
            log.info("🔧 Inicializando datos: Creando usuario administrador...");
            log.info("   💼 Wallet configurada: {}", adminWallet);

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
     * Lee las wallets desde application.properties (validator1.wallet,
     * validator2.wallet)
     */
    private void crearValidadores() {
        // Validador 1 - ONG Ambiental
        var validador1Exist = usuarioRepository.findByWalletAddress(validator1Wallet);

        if (validador1Exist.isEmpty()) {
            log.info("🔧 Creando Validador 1 (ONG Ambiental)...");
            log.info("   💼 Wallet configurada: {}", validator1Wallet);

            Usuario validador1 = Usuario.builder()
                    .nombre("ONG Ambiental UPAO")
                    .edad("0")
                    .telefono("987654321")
                    .correo("ong1@reciclaupao.edu.pe")
                    .username("ong1")
                    .password(passwordEncoder.encode("ong123"))
                    .dni("11111111")
                    .walletAddress(validator1Wallet)
                    .rol(Rol.ONG)
                    .nivel(NivelUsuario.ORO)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(validador1);

            log.info("✅ Validador 1 creado exitosamente");
            log.info("   👤 Username: ong1");
            log.info("   🔑 Password: ong123");
            log.info("   💼 Wallet:   {}", validator1Wallet);
        } else {
            log.info("ℹ️  Validador 1 ya existe");
        }

        // Validador 2 - Centro de Acopio
        var validador2Exist = usuarioRepository.findByWalletAddress(validator2Wallet);

        if (validador2Exist.isEmpty()) {
            log.info("🔧 Creando Validador 2 (Centro de Acopio)...");
            log.info("   💼 Wallet configurada: {}", validator2Wallet);

            Usuario validador2 = Usuario.builder()
                    .nombre("Centro de Acopio UPAO")
                    .edad("0")
                    .telefono("987654322")
                    .correo("ong2@reciclaupao.edu.pe")
                    .username("ong2")
                    .password(passwordEncoder.encode("ong123"))
                    .dni("22222222")
                    .walletAddress(validator2Wallet)
                    .rol(Rol.ONG)
                    .nivel(NivelUsuario.ORO)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(validador2);

            log.info("✅ Validador 2 creado exitosamente");
            log.info("   👤 Username: ong2");
            log.info("   🔑 Password: ong123");
            log.info("   💼 Wallet:   {}", validator2Wallet);
        } else {
            log.info("ℹ️  Validador 2 ya existe");
        }
    }

    /**
     * Crea el usuario del Centro de Acopio si no existe
     * Lee la wallet desde application.properties (centro.wallet)
     * Responsable de registrar actividades de estudiantes
     */
    private void crearCentroAcopio() {
        var centroExist = usuarioRepository.findByWalletAddress(centroWallet);

        if (centroExist.isEmpty()) {
            log.info("🔧 Creando usuario Centro de Acopio...");
            log.info("   💼 Wallet configurada: {}", centroWallet);

            Usuario centro = Usuario.builder()
                    .nombre("Centro de Acopio UPAO")
                    .edad("0")
                    .telefono("987654323")
                    .correo("centroacopio@reciclaupao.edu.pe")
                    .username("centroacopio")
                    .password(passwordEncoder.encode("centro123"))
                    .dni("33333333")
                    .walletAddress(centroWallet)
                    .rol(Rol.CENTRO_ACOPIO)
                    .nivel(NivelUsuario.DIAMANTE)
                    .puntos(0.0)
                    .build();

            usuarioRepository.save(centro);

            log.info("✅ Centro de Acopio creado exitosamente");
            log.info("   👤 Username: centroacopio");
            log.info("   🔑 Password: centro123");
            log.info("   💼 Wallet:   {}", centroWallet);
        } else {
            log.info("ℹ️  Centro de Acopio ya existe");
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
