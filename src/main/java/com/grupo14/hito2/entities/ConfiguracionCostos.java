package com.grupo14.hito2.entities;

import java.math.BigDecimal;

import com.grupo14.hito2.enums.TipoUnidad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Costos que fija un festival. Sin tipo de unidad valen para todas las unidades;
 * con tipo, solo para las de ese tipo, y le ganan a la general.
 */
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConfiguracionCostos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal costoSuperficie;
    private BigDecimal costoMontaje;
    private BigDecimal plusElectricidad;
    private BigDecimal sueldoBase;
    private BigDecimal plusCocinero;
    private BigDecimal plusCocineroAyudante;
    private BigDecimal plusLavaplatos;
    private BigDecimal montoAntiguedad;

    // null = se aplica a todas las unidades del festival
    @Enumerated(EnumType.STRING)
    private TipoUnidad tipoUnidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    public boolean esGeneral() {
        return tipoUnidad == null;
    }
}