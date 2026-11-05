package com.grupo14.hito2.dtos;

import com.grupo14.hito2.entities.Empleado;

/** Un empleado activo en el staff de una unidad. Lo arma la query, no persiste. */
public record EmpleadoDeUnidad(Empleado empleado) {
}
