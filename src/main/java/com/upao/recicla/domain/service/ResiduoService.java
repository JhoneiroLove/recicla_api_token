package com.upao.recicla.domain.service;

import com.upao.recicla.domain.entity.Residuo;
import com.upao.recicla.infra.repository.ResiduoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResiduoService {
    private final ResiduoRepository residuoRepository;

    public ResiduoService(ResiduoRepository residuoRepository) {
        this.residuoRepository = residuoRepository;
    }

    public Page<Residuo> getAllResiduos(Pageable pageable) {
        return residuoRepository.findAll(pageable);
    }

    public java.util.List<Residuo> getAllResiduosList() {
        return residuoRepository.findAll();
    }

    public Optional<Residuo> getResiduoById(Long id) {
        return residuoRepository.findById(id);
    }

    public Residuo getReferenceById(Long id) {
        return residuoRepository.getReferenceById(id);
    }

    public void addResiduo(Residuo residuo) {
        Optional<Residuo> existente = residuoRepository.findByNombre(residuo.getNombre());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un residuo con el nombre: " + residuo.getNombre());
        }
        residuoRepository.save(residuo);
    }

    public void updateResiduo(Residuo residuo, Long id) {
        Residuo residuoExists = residuoRepository.findById(id)
                .orElse(new Residuo());

        residuoExists.setNombre(residuo.getNombre());
        residuoRepository.save(residuoExists);
    }

    public void deleteResiduoById(Long id) {
        residuoRepository.deleteById(id);
    }
}
