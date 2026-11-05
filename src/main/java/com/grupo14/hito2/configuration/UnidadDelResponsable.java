package com.grupo14.hito2.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * En un parámetro UnidadVenta de un controller: la unidad del responsable logueado.
 * La resuelve UnidadDelResponsableResolver, así los controllers no repiten la búsqueda por email.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnidadDelResponsable {
}
