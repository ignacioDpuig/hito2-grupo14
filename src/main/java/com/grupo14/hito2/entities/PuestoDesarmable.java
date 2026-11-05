package com.grupo14.hito2.entities;

import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
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

    @Override
    public DatosUnidad getDatosDelTipo() {
        return new DatosUnidad(null, null, cantidadCarpas, tiempoMontaje);
    }

    @Override
    public void actualizarDatosDelTipo(DatosUnidad datos) {
        if (datos.cantidadCarpas() == null || datos.cantidadCarpas() < 1) {
            throw new ReglaDeNegocioException("El puesto tiene que tener al menos una carpa");
        }
        if (datos.tiempoMontaje() == null || datos.tiempoMontaje() < 0) {
            throw new ReglaDeNegocioException("El tiempo de montaje es obligatorio y no puede ser negativo");
        }
        this.cantidadCarpas = datos.cantidadCarpas();
        this.tiempoMontaje = datos.tiempoMontaje();
    }
}
