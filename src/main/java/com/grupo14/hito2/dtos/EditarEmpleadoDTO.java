package com.grupo14.hito2.dtos;

import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.validation.Dni;
import com.grupo14.hito2.validation.FechasDeIngreso;
import com.grupo14.hito2.validation.IngresoMayorDeEdad;
import com.grupo14.hito2.validation.MayorDeEdad;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Modificación de un empleado. El tipo (cocinero o cajero) no se cambia: solo se muestra. */
@IngresoMayorDeEdad
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EditarEmpleadoDTO implements FechasDeIngreso {
    private String tipoPersonal;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotNull(message = "El DNI es obligatorio")
    @Dni
    private Long dni;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @MayorDeEdad(message = "El empleado tiene que ser mayor de edad")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaIngreso;

    private String especialidad;
    private CategoriaCocinero categoria;
    private String turno;
}
