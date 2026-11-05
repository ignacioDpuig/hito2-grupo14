package com.grupo14.hito2.entities;

import com.grupo14.hito2.exceptions.ReglaDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cierre de caja de una unidad en un día: lo hace el cajero al terminar la jornada.
 * Guarda lo vendido en ese momento y, una vez cerrada, la unidad no carga más pedidos ese día.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"unidad_venta_id", "fecha"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_venta_id", nullable = false)
    private UnidadVenta unidadVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_id", nullable = false)
    private Personal cajero;

    private long cantidadPedidos;
    @Column(precision = 12, scale = 2)
    private BigDecimal totalVendido;

    // Solo un cajero cierra la caja, y solo la de hoy: el total es lo vendido hasta este momento
    public static CierreCaja deHoy(UnidadVenta unidad, Personal cajero, long cantidadPedidos, BigDecimal totalVendido) {
        if (!cajero.puedeCargarPedidos()) {
            throw new ReglaDeNegocioException("Solo un cajero puede cerrar la caja");
        }
        return new CierreCaja(null, LocalDate.now(), unidad, cajero, cantidadPedidos, totalVendido);
    }
}
