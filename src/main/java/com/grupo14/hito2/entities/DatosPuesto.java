package com.grupo14.hito2.entities;

import com.grupo14.hito2.enums.CategoriaCocinero;

/**
 * Value object (no persiste): los datos propios del puesto de un empleado.
 * Cada subclase de Empleado usa los que le corresponden: el cocinero especialidad y categoría, el cajero el turno.
 */
public record DatosPuesto(String especialidad, CategoriaCocinero categoria, String turno) {
}
