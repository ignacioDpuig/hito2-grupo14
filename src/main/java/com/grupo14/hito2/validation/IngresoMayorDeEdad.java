package com.grupo14.hito2.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** La fecha de ingreso tiene que ser de cuando la persona ya era mayor de edad. Va sobre la clase porque usa dos campos. */
@Documented
@Constraint(validatedBy = IngresoMayorDeEdadValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface IngresoMayorDeEdad {
    String message() default "La fecha de ingreso tiene que ser de cuando ya era mayor de edad";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
