package com.grupo14.hito2.repositories;

import com.grupo14.hito2.entities.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {

    // Todos los platos de una unidad (activos y dados de baja) para gestión
    @Query("SELECT p FROM Plato p WHERE p.unidadVenta.id = :unidadId")
    List<Plato> findByUnidadVentaId(@Param("unidadId") Long unidadId);

    // Activo o dado de baja: el nombre no se repite dentro de la unidad
    Optional<Plato> findFirstByUnidadVentaIdAndNombreIgnoreCase(Long unidadId, String nombre);
}
