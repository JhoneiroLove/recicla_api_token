package com.upao.recicla.domain.service;

import com.upao.recicla.blockchain.dto.TransactionResult;
import com.upao.recicla.blockchain.service.BlockchainService;
import com.upao.recicla.blockchain.service.IPFSService;
import com.upao.recicla.domain.entity.Actividad;
import com.upao.recicla.domain.entity.Residuo;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.infra.email.QrCodeGenerator;
import com.upao.recicla.infra.repository.ActividadRepository;
import com.upao.recicla.infra.repository.ResiduoRepository;
import com.upao.recicla.infra.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ActividadService {
    @Autowired
    private final ActividadRepository actividadRepository;

    @Autowired
    private final ResiduoRepository residuoRepository;

    @Autowired
    private final UsuarioRepository usuarioRepository;

    @Autowired(required = false)
    private BlockchainService blockchainService;

    @Autowired(required = false)
    private IPFSService ipfsService;

    public ActividadService(ActividadRepository actividadRepository,
            ResiduoRepository residuoRepository,
            UsuarioRepository usuarioRepository) {
        this.actividadRepository = actividadRepository;
        this.residuoRepository = residuoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Page<Actividad> getAllActividades(Pageable pageable) {
        return actividadRepository.findAll(pageable);
    }

    public Optional<Actividad> getActividadById(Long id) {
        return actividadRepository.findById(id);
    }

    public List<Actividad> getActividadesPorUsuarioId(Long usuarioId) {
        return actividadRepository.findByUsuarioId(usuarioId);
    }

    public Actividad getReferenceById(Long id) {
        return actividadRepository.getReferenceById(id);
    }

    public ResponseEntity<String> addActividad(Actividad actividad, String nombreResiduo, MultipartFile imagen) {
        if (actividad.getCantidad() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La cantidad no debe ser menor que 0.");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(currentUserName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        actividad.setUsuario(usuario);

        Residuo residuo = residuoRepository.findByNombre(nombreResiduo)
                .orElseThrow(() -> new RuntimeException("Residuo no encontrado"));
        actividad.setResiduo(residuo);

        double puntosGanados = actividad.getCantidad() * residuo.getPuntos();

        if (blockchainService != null && usuario.getWalletAddress() != null) {
            try {
                log.info("📝 Proponiendo actividad en blockchain para {}: {}kg de {}",
                        usuario.getUsername(), actividad.getCantidad(), residuo.getNombre());

                String evidenciaIPFS = "QmPendiente";

                // Subir imagen a IPFS si está disponible
                if (ipfsService != null && imagen != null && !imagen.isEmpty()) {
                    try {
                        String metadata = String.format(
                                "{\"usuario\":\"%s\",\"material\":\"%s\",\"peso\":%d,\"fecha\":\"%s\"}",
                                usuario.getUsername(),
                                residuo.getNombre(),
                                actividad.getCantidad().intValue(),
                                java.time.LocalDateTime.now().toString());

                        evidenciaIPFS = ipfsService.uploadEvidencia(imagen, metadata);
                        log.info("📎 Evidencia subida a IPFS: {}", evidenciaIPFS);
                    } catch (Exception ipfsError) {
                        log.error("❌ Error subiendo evidencia a IPFS: {}", ipfsError.getMessage());
                    }
                }

                TransactionResult result = blockchainService.proponerActividad(
                        usuario.getWalletAddress(),
                        actividad.getCantidad().intValue(),
                        mapearTipoMaterial(residuo.getNombre()),
                        evidenciaIPFS);

                if (result.isSuccess()) {
                    actividad.setBlockchainTxHash(result.getTransactionHash());
                    log.info("✅ Actividad propuesta en blockchain. TX: {} - Esperando validación ONG",
                            result.getTransactionHash());
                } else {
                    log.warn("⚠️ No se pudo proponer actividad en blockchain: {}", result.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("❌ Error proponiendo actividad en blockchain", e);
            }
        } else {
            log.info("ℹ️ Blockchain deshabilitado o usuario sin wallet");
        }

        actualizarPuntosUsuario(usuario.getId(), puntosGanados);
        actividadRepository.save(actividad);

        return ResponseEntity.ok("Actividad registrada con éxito. Puntos ganados: " + puntosGanados + ".");
    }

    private String mapearTipoMaterial(String nombreResiduo) {
        String nombre = nombreResiduo.toLowerCase();
        if (nombre.contains("plastico") || nombre.contains("plástico"))
            return "plastico";
        if (nombre.contains("papel"))
            return "papel";
        if (nombre.contains("vidrio"))
            return "vidrio";
        if (nombre.contains("metal") || nombre.contains("aluminio"))
            return "metal";
        if (nombre.contains("carton") || nombre.contains("cartón"))
            return "carton";
        if (nombre.contains("organico") || nombre.contains("orgánico"))
            return "organico";
        return "plastico";
    }

    private void actualizarPuntosUsuario(Long idUsuario, double puntos) {
        Usuario usuario = usuarioRepository.findById(idUsuario).get();
        usuario.setPuntos(usuario.getPuntos() + puntos);
        usuario.actualizarNivel();
        usuarioRepository.save(usuario);
    }

    public void updateActividad(Actividad actividad, Long id) {
        Actividad actividadExists = actividadRepository.findById(id)
                .orElse(new Actividad());
        actividadExists.setNombre(actividad.getNombre());
        actividadRepository.save(actividadExists);
    }

    public void deleteActividadById(Long id) {
        actividadRepository.deleteById(id);
    }

    public byte[] generateActividadQRCode(Long actividadId) throws Exception {
        Actividad actividad = getReferenceById(actividadId);
        String qrCodeText = createQRCodeText(actividad);
        return QrCodeGenerator.generateQRCodeImage(qrCodeText, 250, 250);
    }

    private String createQRCodeText(Actividad actividad) {
        return String.format(
                "Actividad: %s\nCreador: %s\nFecha: %s\nResiduo: %s\nCantidad Reciclada: %.2f kg\nPuntos Ganados: %.2f\nBlockchain TX: %s",
                actividad.getNombre(),
                actividad.getUsuario().getNombre(),
                actividad.getFecha().toString(),
                actividad.getResiduo().getNombre(),
                actividad.getCantidad(),
                actividad.getCantidad() * actividad.getResiduo().getPuntos(),
                actividad.getBlockchainTxHash() != null ? actividad.getBlockchainTxHash() : "N/A");
    }
}