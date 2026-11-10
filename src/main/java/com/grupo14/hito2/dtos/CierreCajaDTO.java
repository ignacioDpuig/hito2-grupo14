package com.grupo14.hito2.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Lo vendido por una unidad en el día y si la caja ya se cerró. No persiste. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CierreCajaDTO {
    private String nombreUnidad;
    private String nombreFestival;
    private LocalDate fecha;
    private long cantidadPedidos;
    private BigDecimal totalVendido;
    private boolean cerrada;
    private String cerradaPor;   // nombre del cajero que cerró la caja, null si sigue abierta
}
