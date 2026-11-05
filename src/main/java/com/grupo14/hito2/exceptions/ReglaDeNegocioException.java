package com.grupo14.hito2.exceptions;

/**
 * Se rompió una regla del negocio (DNI repetido, caja cerrada, canon negativo...). El mensaje es para el usuario:
 * los controllers la atrapan para mostrarlo en el formulario. Cualquier otra excepción es un error del programa.
 */
public class ReglaDeNegocioException extends RuntimeException {
    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
