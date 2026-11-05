package com.grupo14.hito2.mappers;

import com.grupo14.hito2.dtos.CrearResponsableDTO;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.Responsable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Pasa los datos de los formularios a entidades. No encripta, no asigna roles ni guarda. */
@Component
public class PersonalMapper {

    public Responsable toResponsable(CrearResponsableDTO dto) {
        Responsable responsable = new Responsable();
        // Sin DNI ni fecha de nacimiento: el registro solo pide nombre, apellido y mail
        copiarDatosPersonales(responsable, dto.getNombre(), dto.getApellido(), null, dto.getEmail(), null);
        responsable.setFechaIngreso(LocalDate.now());
        return responsable;
    }

    private void copiarDatosPersonales(Personal personal, String nombre, String apellido, Long dni,
                                       String email, LocalDate fechaNacimiento) {
        personal.setNombre(nombre);
        personal.setApellido(apellido);
        personal.setDni(dni);
        personal.setEmail(email);
        personal.setFechaNacimiento(fechaNacimiento);
    }
}
