package com.grupo14.hito2.enums;

import com.grupo14.hito2.entities.ConfiguracionCostos;
import java.math.BigDecimal;

public enum CategoriaCocinero {
    COCINERO("Cocinero"),
    COCINERO_AYUDANTE("Cocinero Ayudante"),
    LAVAPLATOS("Lavaplatos");

    private final String descripcion;

    CategoriaCocinero(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPlus(ConfiguracionCostos config) {
        return switch (this) {
            case COCINERO -> config.getPlusCocinero();
            case COCINERO_AYUDANTE -> config.getPlusCocineroAyudante();
            case LAVAPLATOS -> config.getPlusLavaplatos();
        };
    }
}
