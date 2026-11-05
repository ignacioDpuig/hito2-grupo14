package com.grupo14.hito2.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Patente argentina: formato viejo ABC123 o Mercosur AB123CD (sin importar mayúsculas, espacios ni guiones).
 * Vacía es válida: si es obligatoria lo decide el tipo de unidad.
 */
@Documented
@Constraint(validatedBy = PatenteValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Patente {
    String message() default "La patente tiene que ser del formato ABC123 o AB123CD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
