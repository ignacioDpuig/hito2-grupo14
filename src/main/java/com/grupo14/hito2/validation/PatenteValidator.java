package com.grupo14.hito2.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PatenteValidator implements ConstraintValidator<Patente, String> {

    private static final String FORMATO = "^([A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2})$";

    @Override
    public boolean isValid(String patente, ConstraintValidatorContext context) {
        if (patente == null || patente.isBlank()) return true;
        return normalizar(patente).matches(FORMATO);
    }

    // Como se guarda: en mayúsculas, sin espacios ni guiones ("ab 123-cd" → "AB123CD")
    public static String normalizar(String patente) {
        return patente.replaceAll("[\\s-]", "").toUpperCase();
    }
}
