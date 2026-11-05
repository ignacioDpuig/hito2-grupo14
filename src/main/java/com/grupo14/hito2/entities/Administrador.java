package com.grupo14.hito2.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Administrador extends Personal {
    // Usuario precargado que administra los festivales. No cobra sueldo.
}
