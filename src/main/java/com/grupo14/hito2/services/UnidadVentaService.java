package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.dtos.ResumenPredioDTO;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.mappers.UnidadVentaMapper;
import com.grupo14.hito2.repositories.UnidadVentaRepository;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadVentaService {

    public static final int TOP_MINIMO = 1;
    public static final int TOP_MAXIMO = 20;

    private final UnidadVentaRepository unidadVentaRepository;
    private final FestivalService festivalService;
    private final UnidadVentaMapper unidadVentaMapper;

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

    // Solo unidades habilitadas, igual que el ranking; cada una con los costos de su tipo
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> obtenerMayoresCanon(long festivalId, int top) {
        if (top < TOP_MINIMO || top > TOP_MAXIMO) {
            throw new ReglaDeNegocioException("La cantidad de unidades tiene que estar entre " + TOP_MINIMO + " y " + TOP_MAXIMO);
        }
        festivalService.buscarPorId(festivalId);
        return unidadVentaRepository.findByFestivalId(festivalId).stream()
                .filter(UnidadVenta::isActiva)
                .map(u -> unidadVentaMapper.toReporteCanon(u, u.calcularCanon()))
                .sorted(Comparator.comparing(ReporteMayoresCanonDTO::getCanon).reversed())
                .limit(top)
                .toList();
    }
}
