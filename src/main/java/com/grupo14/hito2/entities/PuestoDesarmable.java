package com.grupo14.hito2.entities;

import com.grupo14.hito2.enums.TipoUnidad;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PuestoDesarmable extends UnidadVenta {
    private int cantidadCarpas;
    private float tiempoMontaje;

    // (superficie * costoSuperficie) - (tiempoMontaje * costoMontaje)
    @Override
    public BigDecimal calcularCanon(ConfiguracionCostos config) {
        return decimal(getSuperficie()).multiply(config.getCostoSuperficie())
                .subtract(decimal(tiempoMontaje).multiply(config.getCostoMontaje()));
    }

    @Override
    public TipoUnidad getTipoUnidad() {
        return TipoUnidad.PUESTO_DESARMABLE;
    }
}
