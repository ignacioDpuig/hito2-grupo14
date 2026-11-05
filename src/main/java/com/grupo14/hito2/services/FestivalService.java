package com.grupo14.hito2.services;

import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.repositories.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public List<Festival> obtenerTodosFestivales() {
        return festivalRepository.findAll();
    }

    // Los festivales donde todavía se puede registrar una unidad
    public List<Festival> obtenerFestivalesEnCursoOFuturos() {
        return festivalRepository.findByFechaFinGreaterThanEqualOrderByFechaInicio(LocalDate.now());
    }

    public Festival buscarPorId(long id) {
        return festivalRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Festival no encontrado"));
    }
}
