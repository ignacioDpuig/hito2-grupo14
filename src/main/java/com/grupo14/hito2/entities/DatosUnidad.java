package com.grupo14.hito2.entities;

/**
 * Value object (no persiste): los datos propios del tipo de unidad.
 * Cada subclase de UnidadVenta usa los que le corresponden: el food truck patente y conexión, el puesto carpas y montaje.
 */
public record DatosUnidad(String patente, Boolean estaConectado, Integer cantidadCarpas, Float tiempoMontaje) {
}
