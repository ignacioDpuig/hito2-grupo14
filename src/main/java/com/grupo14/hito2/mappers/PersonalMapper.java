package com.grupo14.hito2.mappers;

import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.CrearResponsableDTO;
import com.grupo14.hito2.dtos.EditarEmpleadoDTO;
import com.grupo14.hito2.entities.DatosPuesto;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.Responsable;
import com.grupo14.hito2.enums.TipoPersonal;
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

    // TipoPersonal crea la subclase y la subclase valida sus datos (un cocinero sin categoría no se puede liquidar)
    public Empleado toEmpleado(CrearEmpleadoDTO dto) {
        Empleado empleado = TipoPersonal.desde(dto.getTipoPersonal()).nuevoEmpleado();
        copiarDatosPersonales(empleado, dto.getNombre(), dto.getApellido(), dto.getDni(),
                dto.getEmail(), dto.getFechaNacimiento());
        empleado.setFechaIngreso(dto.getFechaIngreso() != null ? dto.getFechaIngreso() : LocalDate.now());
        empleado.actualizarDatosDelPuesto(datosDelPuesto(dto));
        return empleado;
    }

    public EditarEmpleadoDTO toEditarDTO(Empleado empleado) {
        DatosPuesto puesto = empleado.getDatosPuesto();
        return new EditarEmpleadoDTO(empleado.getTipoPersonal().getValor(), empleado.getNombre(), empleado.getApellido(),
                empleado.getDni(), empleado.getEmail(), empleado.getFechaNacimiento(), empleado.getFechaIngreso(),
                puesto.especialidad(), puesto.categoria(), puesto.turno());
    }

    // El tipo de empleado no cambia: cada subclase toma del puesto los datos que le corresponden
    public void actualizar(Empleado empleado, EditarEmpleadoDTO dto) {
        copiarDatosPersonales(empleado, dto.getNombre(), dto.getApellido(), dto.getDni(),
                dto.getEmail(), dto.getFechaNacimiento());
        empleado.setFechaIngreso(dto.getFechaIngreso());
        empleado.actualizarDatosDelPuesto(new DatosPuesto(dto.getEspecialidad(), dto.getCategoria(), dto.getTurno()));
    }

    private DatosPuesto datosDelPuesto(CrearEmpleadoDTO dto) {
        return new DatosPuesto(dto.getEspecialidad(), dto.getCategoria(), dto.getTurno());
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
