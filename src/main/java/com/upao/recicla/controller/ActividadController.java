package com.upao.recicla.controller;

import com.upao.recicla.domain.dto.actividadDto.DatosActualizarActividad;
import com.upao.recicla.domain.dto.actividadDto.DatosDetallesActividad;
import com.upao.recicla.domain.dto.actividadDto.DatosListadoActividad;
import com.upao.recicla.domain.dto.actividadDto.DatosRespuestaActividad;
import com.upao.recicla.domain.entity.Actividad;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.domain.service.ActividadService;
import com.upao.recicla.infra.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/actividad")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class ActividadController {

    @Autowired
    private final ActividadService actividadService;

    private static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("image/png", "image/jpeg", "image/jpg");
    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024; // 5MB

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<Page<DatosListadoActividad>> getAllActividades(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(actividadService.getAllActividades(pageable).map(DatosListadoActividad::new));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<DatosRespuestaActividad>> getHistorialPorUsuarioId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(currentUserName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Actividad> actividades = actividadService.getActividadesPorUsuarioId(usuario.getId());
        if (actividades.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El historial de actividades está vacío. Empieza a reciclar para ganar puntos.");
        }
        List<DatosRespuestaActividad> datosActividades = actividades.stream()
                .map(actividad -> new DatosRespuestaActividad(
                        actividad.getId(),
                        actividad.getNombre(),
                        actividad.getCantidad(),
                        actividad.getImagen(),
                        actividad.getFecha(),
                        actividad.getCantidad() * actividad.getResiduo().getPuntos(),
                        actividad.getResiduo().getId(),
                        actividad.getResiduo().getNombre(),
                        actividad.getResiduo().getDescripcion(),
                        actividad.getResiduo().getTipo(),
                        actividad.getResiduo().getPuntos(),
                        actividad.getUsuario().getId(),
                        actividad.getUsuario().getNombre(),
                        actividad.getUsuario().getPuntos()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(datosActividades);
    }

    @GetMapping("/historial-centro")
    public ResponseEntity<List<DatosRespuestaActividad>> getHistorialRegistradoPorCentro() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String centroUsername = authentication.getName();
        Usuario centro = usuarioRepository.findByUsername(centroUsername)
                .orElseThrow(() -> new RuntimeException("Centro de Acopio no encontrado"));

        List<Actividad> actividades = actividadService.getActividadesRegistradasPorCentro(centro.getId());

        if (actividades.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No has registrado ninguna actividad todavía.");
        }

        List<DatosRespuestaActividad> datosActividades = actividades.stream()
                .map(actividad -> new DatosRespuestaActividad(
                        actividad.getId(),
                        actividad.getNombre(),
                        actividad.getCantidad(),
                        actividad.getImagen(),
                        actividad.getFecha(),
                        actividad.getCantidad() * actividad.getResiduo().getPuntos(),
                        actividad.getResiduo().getId(),
                        actividad.getResiduo().getNombre(),
                        actividad.getResiduo().getDescripcion(),
                        actividad.getResiduo().getTipo(),
                        actividad.getResiduo().getPuntos(),
                        actividad.getUsuario().getId(),
                        actividad.getUsuario().getNombre(),
                        actividad.getUsuario().getPuntos()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(datosActividades);
    }

    @PostMapping("/registro")
    @Transactional
    public ResponseEntity<DatosDetallesActividad> addActividad(@RequestParam("nombre") String nombre,
            @RequestParam("cantidad") Double cantidad,
            @RequestParam("nombreResiduo") String nombreResiduo,
            @RequestParam("image") MultipartFile imagen) throws IOException {

        String imagenPath = guardarImagen(imagen);

        Actividad actividad = new Actividad();
        actividad.setNombre(nombre);
        actividad.setCantidad(cantidad);
        actividad.setImagen(imagenPath);

        actividadService.addActividad(actividad, nombreResiduo, imagen);

        DatosDetallesActividad datosDetallesActividad = new DatosDetallesActividad(actividad);
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.path("/{id}").buildAndExpand(actividad.getId());
        return ResponseEntity.created(builder.build().toUri()).body(datosDetallesActividad);
    }

    @PostMapping("/registro-centro")
    @Transactional
    public ResponseEntity<DatosDetallesActividad> addActividadCentro(
            @RequestParam("walletEstudiante") String walletEstudiante,
            @RequestParam("cantidad") Double cantidad,
            @RequestParam("nombreResiduo") String nombreResiduo,
            @RequestParam("image") MultipartFile imagen) throws IOException {

        log.info("📦 Centro de Acopio registrando actividad para estudiante: {}", walletEstudiante);

        String imagenPath = guardarImagen(imagen);

        Actividad actividad = new Actividad();
        actividad.setNombre("Entrega Centro Acopio");
        actividad.setCantidad(cantidad);
        actividad.setImagen(imagenPath);

        // Buscar usuario por wallet
        Usuario estudiante = usuarioRepository.findByWalletAddress(walletEstudiante)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con wallet: " + walletEstudiante));

        actividadService.addActividadCentro(actividad, nombreResiduo, estudiante, imagen);

        DatosDetallesActividad datosDetallesActividad = new DatosDetallesActividad(actividad);
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.path("/{id}").buildAndExpand(actividad.getId());
        return ResponseEntity.created(builder.build().toUri()).body(datosDetallesActividad);
    }

    private String guardarImagen(MultipartFile imagen) throws IOException {
        if (!imagen.isEmpty()) {
            if (!FORMATOS_PERMITIDOS.contains(imagen.getContentType())) {
                throw new IllegalArgumentException("Formato de archivo no permitido.");
            }
            if (imagen.getSize() > TAMANO_MAXIMO) {
                throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido.");
            }

            byte[] bytes = imagen.getBytes();
            return Base64.encodeBase64String(bytes);
        }
        return null;
    }

    @GetMapping("/qr/{actividadId}")
    public ResponseEntity<byte[]> getActividadQRCode(@PathVariable Long actividadId) {
        try {
            byte[] qrCode = actividadService.generateActividadQRCode(actividadId);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(qrCode);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticasUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        Usuario usuario = usuarioRepository.findByUsername(currentUserName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Actividad> actividades = actividadService.getActividadesPorUsuarioId(usuario.getId());
        double totalPuntosActividades = actividades.stream()
                .mapToDouble(a -> a.getCantidad() * a.getResiduo().getPuntos())
                .sum();
        double totalPuntosUsuario = usuario.getPuntos();

        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalActividades", actividades.size());
        estadisticas.put("totalPuntos", totalPuntosActividades);
        estadisticas.put("totalPuntosUsuario", totalPuntosUsuario);

        return ResponseEntity.ok(estadisticas);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping
    @Transactional
    public ResponseEntity updateActividad(@RequestBody @Valid DatosActualizarActividad datos) {
        var actividad = actividadService.getReferenceById(datos.id());
        actividad.actualizarActividad(datos);

        return ResponseEntity.ok(new DatosDetallesActividad(actividad));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteActividadById(@PathVariable Long id) {
        var actividad = actividadService.getReferenceById(id);
        actividadService.deleteActividadById(id);
        return ResponseEntity.noContent().build();
    }

}
