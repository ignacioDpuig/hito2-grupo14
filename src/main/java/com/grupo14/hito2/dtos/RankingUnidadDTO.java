package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Fila del ranking de unidades por recaudación. No persiste. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RankingUnidadDTO {
    private String nombreComercial;
    private String codigo;
    private String tipoUnidad;
    private BigDecimal recaudacion;
}
