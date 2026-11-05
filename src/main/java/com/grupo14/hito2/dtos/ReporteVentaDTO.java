package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Recaudación de una unidad o de todo un festival. Rentabilidad bruta = vendido − costo de los platos. No persiste. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReporteVentaDTO {
    private String nombreFestival;
    private String nombreUnidad;   // null cuando el reporte es de todo el festival
    private BigDecimal totalVendido;
    private BigDecimal totalCostos;
    private int cantidadPedidos;

    public BigDecimal getRentabilidadBruta() {
        return totalVendido.subtract(totalCostos);
    }
}
