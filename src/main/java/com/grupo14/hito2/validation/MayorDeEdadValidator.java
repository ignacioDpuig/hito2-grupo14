package com.grupo14.hito2.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MayorDeEdadValidator implements ConstraintValidator<MayorDeEdad, LocalDate> {

    public static final int EDAD_MINIMA = 18;
    // Antes de esto la fecha es un error de carga, no una persona
    private static final LocalDate NACIMIENTO_MINIMO = LocalDate.of(1900, 1, 1);

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) return true; // de eso se encarga @NotNull
        if (fechaNacimiento.isBefore(NACIMIENTO_MINIMO)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("La fecha de nacimiento no puede ser anterior a 1900")
                    .addConstraintViolation();
            return false;
        }
        return ChronoUnit.YEARS.between(fechaNacimiento, LocalDate.now()) >= EDAD_MINIMA;
    }
}
