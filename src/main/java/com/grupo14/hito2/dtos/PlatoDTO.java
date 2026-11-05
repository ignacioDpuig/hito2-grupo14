package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PlatoDTO {
    private Long id;
    private String nombre;
    private BigDecimal costoProduccion;
    private BigDecimal precioVenta;
    private int cantidadVendida;
    private BigDecimal totalRecaudado;
}
