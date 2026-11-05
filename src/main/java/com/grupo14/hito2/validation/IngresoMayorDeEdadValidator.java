package com.grupo14.hito2.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IngresoMayorDeEdadValidator implements ConstraintValidator<IngresoMayorDeEdad, FechasDeIngreso> {

    @Override
    public boolean isValid(FechasDeIngreso fechas, ConstraintValidatorContext context) {
        // Sin alguna de las fechas no hay nada que comparar: de eso se encargan @NotNull y los defaults
        if (fechas.getFechaNacimiento() == null || fechas.getFechaIngreso() == null) return true;
        if (!fechas.getFechaIngreso().isBefore(fechas.getFechaNacimiento().plusYears(MayorDeEdadValidator.EDAD_MINIMA))) {
            return true;
        }
        // El error se marca en el campo fechaIngreso para que el formulario lo muestre ahí
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("fechaIngreso").addConstraintViolation();
        return false;
    }
}
