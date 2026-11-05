package com.grupo14.hito2.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Personal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;

    // Los empleados lo tienen siempre; el responsable se registra solo con nombre, apellido y mail
    @Column(unique = true)
    private Long dni;

    private LocalDate fechaNacimiento;
    private LocalDate fechaIngreso;

    @Column(unique = true)
    private String email;
    private String pass;

    @Column(columnDefinition = "BOOLEAN")
    private boolean estaActivo;

    @OneToOne(mappedBy = "responsable")
    private UnidadVenta unidadResponsable;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "personal_rol",
            joinColumns = @JoinColumn(name = "personal_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    // Años cumplidos desde el ingreso hasta esa fecha; si todavía no había ingresado, 0
    public long getAniosAntiguedad(LocalDate alDia) {
        if (fechaIngreso == null || fechaIngreso.isAfter(alDia)) return 0;
        return ChronoUnit.YEARS.between(fechaIngreso, alDia);
    }

    public boolean tieneRol(String nombreRol) {
        return roles.stream().anyMatch(rol -> rol.getNombre().equals(nombreRol));
    }

    // Un empleado dado de baja en todas sus unidades ya no entra al sistema; responsables y admin siempre
    public boolean requiereUnidadActiva() {
        return false;
    }

    // Por defecto nadie carga pedidos: solo el Cajero lo redefine
    public boolean puedeCargarPedidos() {
        return false;
    }
}
