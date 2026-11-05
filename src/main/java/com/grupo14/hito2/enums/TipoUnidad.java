package com.grupo14.hito2.enums;

import com.grupo14.hito2.entities.FoodTruck;
import com.grupo14.hito2.entities.PuestoDesarmable;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;

import java.util.Arrays;

/** Tipos de unidad de venta. El valor coincide con el del formulario. */
public enum TipoUnidad {
    FOOD_TRUCK("FoodTruck", "Food truck"),
    PUESTO_DESARMABLE("PuestoDesarmable", "Puesto desarmable");

    private final String valor;
    private final String descripcion;   // como se muestra en pantalla

    TipoUnidad(String valor, String descripcion) {
        this.valor = valor;
        this.descripcion = descripcion;
    }

    // Factory: la unidad vacía del tipo elegido en el formulario; sus datos propios los valida la subclase
    public UnidadVenta nuevaUnidad() {
        return switch (this) {
            case FOOD_TRUCK -> new FoodTruck();
            case PUESTO_DESARMABLE -> new PuestoDesarmable();
        };
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getValor() {
        return valor;
    }

    public static TipoUnidad desde(String valor) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.valor.equals(valor))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException("Tipo de unidad inválido: " + valor));
    }
}
