package com.grupo14.hito2.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Una persona en el staff de una unidad. Se conserva el estado de cada asignación para los
 * historiales, aunque una baja definitiva marque como inactivas todas las asignaciones del empleado.
 */
@Entity
@Table(name = "unidad_personal", uniqueConstraints = @UniqueConstraint(columnNames = {"unidad_id", "personal_id"}))
@Getter @Setter @NoArgsConstructor
public class AsignacionStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id", nullable = false)
    private UnidadVenta unidadVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    // Baja lógica en esta unidad
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean eliminado = false;

    public AsignacionStaff(UnidadVenta unidadVenta, Personal personal) {
        this.unidadVenta = unidadVenta;
        this.personal = personal;
    }

    public boolean esDe(Personal otro) {
        return personal.getId().equals(otro.getId());
    }
}
