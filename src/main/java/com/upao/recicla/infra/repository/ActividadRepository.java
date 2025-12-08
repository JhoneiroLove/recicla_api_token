package com.upao.recicla.infra.repository;

import com.upao.recicla.domain.entity.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    List<Actividad> findByUsuarioId(Long usuarioId);

    List<Actividad> findByRegistradoPorId(Long registradoPorId);
}
