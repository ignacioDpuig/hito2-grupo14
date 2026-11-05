package com.grupo14.hito2.mappers;

import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.entities.UnidadVenta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UnidadVentaMapper {

    public ReporteMayoresCanonDTO toReporteCanon(UnidadVenta unidad, BigDecimal canon) {
        return new ReporteMayoresCanonDTO(unidad.getNombreComercial(), unidad.getCodigo(), unidad.getTipoDescripcion(), canon);
    }
}
