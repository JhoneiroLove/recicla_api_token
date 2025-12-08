package com.upao.recicla.domain.entity;

import com.upao.recicla.domain.dto.actividadDto.DatosActualizarActividad;
import com.upao.recicla.domain.dto.actividadDto.DatosRegistroActividad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "actividades")
@Entity(name = "Actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Actividad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 155)
    private String nombre;
    @Column(name = "cantidad_reciclada", nullable = false)
    private Double cantidad;
    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String imagen;
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false)
    private LocalDateTime fecha;
    @Column(name = "blockchain_tx_hash", length = 66)
    private String blockchainTxHash;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residuo_id")
    private Residuo residuo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    public Actividad(DatosRegistroActividad datos) {
        this.nombre = datos.nombre();
        this.cantidad = datos.cantidad();
        this.imagen = datos.imagen();
        this.residuo = new Residuo(datos.residuo_id());
        this.usuario = new Usuario(datos.usuario_id());
    }

    public void actualizarActividad(DatosActualizarActividad datosActualizarActividad) {
        if (datosActualizarActividad.nombre() != null) {
            this.nombre = datosActualizarActividad.nombre();
        }
        if (datosActualizarActividad.cantidad() != null) {
            this.cantidad = datosActualizarActividad.cantidad();
        }
        if (datosActualizarActividad.imagen() != null) {
            this.imagen = datosActualizarActividad.imagen();
        }
    }

}
