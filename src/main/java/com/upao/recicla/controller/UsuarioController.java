package com.upao.recicla.controller;

import com.upao.recicla.domain.dto.usuarioDto.*;
import com.upao.recicla.domain.entity.Usuario;
import com.upao.recicla.domain.service.UsuarioService;
import com.upao.recicla.infra.security.LoginRequest;
import com.upao.recicla.infra.security.TokenResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UsuarioController {

    @Autowired
    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<Page<DatosListadoUsuario>> getAllUsuarios(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.getAllUsuarios(pageable).map(DatosListadoUsuario::new));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<DatosRespuestaUsuario> getUsuarioById(@PathVariable Long id) {
        Usuario usuario = usuarioService.getReferenceById(id);
        var datosUsuario = new DatosRespuestaUsuario(usuario.getId(), usuario.getNombre(), usuario.getEdad(),
                usuario.getTelefono(), usuario.getCorreo(), usuario.getPuntos(), usuario.getDni(), usuario.getRol());
        return ResponseEntity.ok(datosUsuario);
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    // Ahora se requiere wallet address
    @PostMapping("/registrar")
    @Transactional
    public ResponseEntity<TokenResponse> addUsuario(@RequestBody @Valid DatosRegistroUsuarioConWallet datos) {
        return ResponseEntity.ok(usuarioService.addUsuario(datos));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping
    @Transactional
    public ResponseEntity updateUsuario(@RequestBody @Valid DatosActualizarUsuario datos) {
        var usuario = usuarioService.getReferenceById(datos.id());
        usuario.actualizarUsuario(datos);

        return ResponseEntity.ok(new DatosDetallesUsuario(usuario));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/{usuarioId}/cambiarRol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long usuarioId, @RequestBody DatosActualizarRolUsuario datosRol) {
        usuarioService.cambiarRolUsuario(usuarioId, datosRol.rol());
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteUsuarioById(@PathVariable Long id) {
        var usuario = usuarioService.getReferenceById(id);
        usuarioService.deleteUsuarioById(id);
        return ResponseEntity.noContent().build();
    }
}