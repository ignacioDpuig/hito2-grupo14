package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Los reportes del responsable sobre el festival de su unidad. No persiste. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportesUnidadDTO {
    private String nombreUnidad;
    private String nombreFestival;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private ReporteVentaDTO recaudacion;
    private PlatoDTO platoEstrella; // null si la unidad todavía no vendió nada
    private RentabilidadDTO rentabilidad;
}
