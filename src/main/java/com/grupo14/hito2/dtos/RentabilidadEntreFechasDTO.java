package com.grupo14.hito2.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * La misma rentabilidad neta, en un período del festival. No persiste.
 * Ventas y costo de platos son los del período; sueldos y canon vienen ya prorrateados por los días que cubre.
 */
@Getter @Setter @NoArgsConstructor
public class RentabilidadEntreFechasDTO extends RentabilidadDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private long diasPeriodo;
    private long diasFestival;

    public RentabilidadEntreFechasDTO(String nombreFestival, String nombreUnidad, LocalDate fechaInicio, LocalDate fechaFin,
                                      long diasPeriodo, long diasFestival,
                                      BigDecimal recaudacion, BigDecimal costosPlatos, BigDecimal sueldos, BigDecimal canon) {
        super(nombreFestival, nombreUnidad, recaudacion, costosPlatos, sueldos, canon);
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.diasPeriodo = diasPeriodo;
        this.diasFestival = diasFestival;
    }
}
