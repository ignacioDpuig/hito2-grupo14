package com.grupo14.hito2.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Responsable extends Personal {
    // El responsable es el dueño de la unidad de venta.
    // No tiene campos específicos.
    // No cobra sueldo: se queda con la rentabilidad neta.
}
