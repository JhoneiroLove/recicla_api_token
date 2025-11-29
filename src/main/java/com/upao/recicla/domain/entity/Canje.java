package com.upao.recicla.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Table(name = "canjes")
@Entity(name = "Canje")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Canje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "fecha_solicitada", columnDefinition = "DATE")
    private LocalDate fecha;
    @Column(name = "puntos_canjeados")
    private Double puntosCanjear;
    @Column(name = "blockchain_tx_hash", length = 66)
    private String blockchainTxHash;
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recompensa_id")
    private Recompensa recompensa;
}
