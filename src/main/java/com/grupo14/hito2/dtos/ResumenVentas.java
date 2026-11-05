package com.grupo14.hito2.dtos;

import java.math.BigDecimal;

/** Totales de ventas de una unidad en un festival. Lo arma la query, no persiste. */
public record ResumenVentas(BigDecimal recaudacion, BigDecimal costos, Long cantidadPedidos) {
}
