package com.grupo14.hito2.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Filtros de la lista de empleados activos. */
@Getter @Setter @NoArgsConstructor
public class FiltroEmpleadosDTO {
    private String texto;          // busca en nombre, apellido y email
    private String tipoPersonal;   // "Cocinero", "Cajero" o vacío para todos
}
