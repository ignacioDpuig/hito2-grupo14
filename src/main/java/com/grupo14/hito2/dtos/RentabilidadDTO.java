package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Rentabilidad neta de una unidad en su festival. No persiste.
 * Neta = ventas − costo de los platos vendidos − sueldos del staff − canon; se calcula siempre de los otros campos.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RentabilidadDTO {
    private String nombreFestival;
    private String nombreUnidad;
    private BigDecimal recaudacion;
    private BigDecimal costosPlatos;
    private BigDecimal sueldos;
    private BigDecimal canon;

    public BigDecimal getRentabilidadNeta() {
        return recaudacion.subtract(costosPlatos).subtract(sueldos).subtract(canon);
    }
}
