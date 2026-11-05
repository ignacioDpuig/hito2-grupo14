package com.grupo14.hito2.enums;

import com.grupo14.hito2.entities.Cajero;
import com.grupo14.hito2.entities.Cocinero;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;

import java.util.Arrays;

/** Tipos de empleado que puede dar de alta un responsable. El valor coincide con el del formulario. */
public enum TipoPersonal {
    COCINERO("Cocinero"),
    CAJERO("Cajero");

    private final String valor;

    TipoPersonal(String valor) {
        this.valor = valor;
    }

    // Factory: el empleado vacío del tipo elegido en el formulario; sus datos del puesto los valida la subclase
    public Empleado nuevoEmpleado() {
        return switch (this) {
            case COCINERO -> new Cocinero();
            case CAJERO -> new Cajero();
        };
    }

    public Class<? extends Empleado> getClase() {
        return switch (this) {
            case COCINERO -> Cocinero.class;
            case CAJERO -> Cajero.class;
        };
    }

    public String getValor() {
        return valor;
    }

    public static TipoPersonal desde(String valor) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.valor.equals(valor))
                .findFirst()
                .orElseThrow(() -> new ReglaDeNegocioException("Tipo de personal inválido: " + valor));
    }
}
