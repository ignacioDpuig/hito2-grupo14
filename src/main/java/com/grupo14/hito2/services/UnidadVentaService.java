package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.ResumenPredioDTO;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.repositories.UnidadVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadVentaService {

    private final UnidadVentaRepository unidadVentaRepository;
    private final FestivalService festivalService;

    // Para el inicio del admin: cuántos festivales hay en curso y cuántas unidades esperan que su responsable las active
    @Transactional(readOnly = true)
    public ResumenPredioDTO obtenerResumenDelPredio() {
        LocalDate hoy = LocalDate.now();
        List<Festival> festivales = festivalService.obtenerTodosFestivales();
        List<UnidadVenta> unidades = unidadVentaRepository.findAll();
        return new ResumenPredioDTO(festivales.size(),
                festivales.stream().filter(f -> f.estaEnCurso(hoy)).count(),
                unidades.size(),
                unidades.stream().filter(u -> !u.isActiva()).count());
    }

    public List<UnidadVenta> obtenerUnidadesPorFestival(long festivalId) {
        return unidadVentaRepository.findByFestivalId(festivalId);
    }
}
