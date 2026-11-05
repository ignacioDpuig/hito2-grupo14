package com.grupo14.hito2.repositories;

import com.grupo14.hito2.dtos.EmpleadoDeUnidad;
import com.grupo14.hito2.entities.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    // Vacío si el email es de un responsable o del administrador
    Optional<Empleado> findByEmail(String email);

    // Solo cajeros y cocineros, activos y dados de baja: el responsable también está en el staff pero no es empleado
    @Query("""
        SELECT e FROM AsignacionStaff a, Empleado e
        WHERE a.personal.id = e.id AND a.unidadVenta.id = :unidadId
        ORDER BY e.apellido, e.nombre
    """)
    List<Empleado> findByUnidadId(@Param("unidadId") long unidadId);

    // Filtros opcionales de empleados activos: texto en nombre, apellido o email y tipo de empleado
    @Query("""
        SELECT new com.grupo14.hito2.dtos.EmpleadoDeUnidad(e)
        FROM AsignacionStaff a, Empleado e
        WHERE a.personal.id = e.id AND a.unidadVenta.id = :unidadId
        AND a.eliminado = FALSE
        AND (:texto IS NULL OR LOWER(e.nombre) LIKE :texto OR LOWER(e.apellido) LIKE :texto OR LOWER(e.email) LIKE :texto)
        AND (:tipo IS NULL OR TYPE(e) = :tipo)
        ORDER BY e.apellido, e.nombre
    """)
    List<EmpleadoDeUnidad> filtrar(@Param("unidadId") long unidadId,
                           @Param("texto") String texto,
                           @Param("tipo") Class<? extends Empleado> tipo);
}
