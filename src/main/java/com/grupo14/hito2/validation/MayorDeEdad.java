package com.grupo14.hito2.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** La fecha de nacimiento tiene que corresponder a alguien de 18 años o más. */
@Documented
@Constraint(validatedBy = MayorDeEdadValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MayorDeEdad {
    String message() default "Tiene que ser mayor de edad";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
