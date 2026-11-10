package com.grupo14.hito2.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CrearPlatoDTO {
    @NotBlank(message = "El nombre del plato es obligatorio")
    private String nombre;

    @NotNull(message = "El costo de producción es obligatorio")
    @PositiveOrZero(message = "El costo de producción no puede ser negativo")
    private BigDecimal costoProduccion;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a 0")
    private BigDecimal precioVenta;

    // Un plato que se vende por menos de lo que cuesta da pérdida en cada venta
    @AssertTrue(message = "El precio de venta no puede ser menor al costo de producción")
    public boolean isPrecioMayorAlCosto() {
        return costoProduccion == null || precioVenta == null || precioVenta.compareTo(costoProduccion) >= 0;
    }
}
