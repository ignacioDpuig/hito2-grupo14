package com.grupo14.hito2.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre; // ROLE_ADMIN, ROLE_RESPONSABLE, ROLE_EMPLEADO

    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<Personal> personal = new HashSet<>();
}