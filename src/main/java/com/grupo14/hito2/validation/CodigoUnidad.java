package com.grupo14.hito2.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Código de unidad de 10 caracteres: 2 letras del tipo (FT = Food Truck, PD = Puesto Desarmable)
 * seguidas de 8 letras mayúsculas o dígitos. Va sobre la clase porque el prefijo depende del tipo elegido.
 */
@Documented
@Constraint(validatedBy = CodigoUnidadValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CodigoUnidad {
    String message() default "El código debe tener 10 caracteres: FT (Food Truck) o PD (Puesto Desarmable) seguido de 8 letras mayúsculas o números";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
