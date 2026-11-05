package com.grupo14.hito2.validation;

import java.time.LocalDate;

/** Formularios con la fecha de nacimiento y la de ingreso de una persona, para validarlas juntas. */
public interface FechasDeIngreso {
    LocalDate getFechaNacimiento();
    LocalDate getFechaIngreso();
}
