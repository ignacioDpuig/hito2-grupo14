package com.grupo14.hito2.dtos;

import java.math.BigDecimal;

/** Resultado de la suma de ventas por unidad. Lo arma la query, no persiste. */
public record RecaudacionPorUnidad(Long unidadId, BigDecimal total) {
}
