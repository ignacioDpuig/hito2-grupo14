package com.grupo14.hito2.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** DNI argentino: 7 u 8 dígitos (de 1.000.000 a 99.999.999). */
@Documented
@Min(1_000_000)
@Max(99_999_999)
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dni {
    String message() default "El DNI tiene que tener 7 u 8 dígitos";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
