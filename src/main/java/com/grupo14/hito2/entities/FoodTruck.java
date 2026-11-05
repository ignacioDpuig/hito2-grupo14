package com.grupo14.hito2.entities;

import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.validation.PatenteValidator;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FoodTruck extends UnidadVenta {
    private String patente;
    @Column(columnDefinition = "BOOLEAN")
    private boolean estaConectado;

    // (superficie * costoSuperficie) + plusElectricidad si requiere conexión
    @Override
    public BigDecimal calcularCanon(ConfiguracionCostos config) {
        BigDecimal canon = decimal(getSuperficie()).multiply(config.getCostoSuperficie());
        return estaConectado ? canon.add(config.getPlusElectricidad()) : canon;
    }

    @Override
    public TipoUnidad getTipoUnidad() {
        return TipoUnidad.FOOD_TRUCK;
    }

    @Override
    public DatosUnidad getDatosDelTipo() {
        return new DatosUnidad(patente, estaConectado, null, null);
    }

    @Override
    public void actualizarDatosDelTipo(DatosUnidad datos) {
        if (datos.patente() == null || datos.patente().isBlank()) {
            throw new ReglaDeNegocioException("La patente del food truck es obligatoria");
        }
        this.patente = PatenteValidator.normalizar(datos.patente());
        this.estaConectado = Boolean.TRUE.equals(datos.estaConectado());
    }
}
