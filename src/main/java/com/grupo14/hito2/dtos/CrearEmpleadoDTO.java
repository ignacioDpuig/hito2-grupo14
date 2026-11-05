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
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

// especialidad, categoria y turno dependen del tipo de personal: los valida cada subclase de Empleado
@IngresoMayorDeEdad
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CrearEmpleadoDTO implements FechasDeIngreso {
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

    @NotBlank(message = "El tipo de personal es obligatorio")
    private String tipoPersonal;

    private String especialidad;
    private CategoriaCocinero categoria;
    private String turno;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @MayorDeEdad(message = "El empleado tiene que ser mayor de edad")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaIngreso;
}
