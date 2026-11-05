package com.grupo14.hito2.repositories;

import com.grupo14.hito2.dtos.PlatoDTO;
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

    // El plato con más unidades vendidas en un festival; si empatan, el primero por nombre.
    // Incluye los dados de baja: sus ventas ya ocurrieron
    @Query("""
        SELECT new com.grupo14.hito2.dtos.PlatoDTO(
            p.id,
            p.nombre,
            p.costoProduccion,
            p.precioVenta,
            CAST(SUM(dp.cantidad) AS int),
            SUM(dp.cantidad * dp.precioUnitario)
        )
        FROM DetallePedido dp
        JOIN dp.plato p
        WHERE p.unidadVenta.id = :unidadId
        AND dp.pedido.festival.id = :festivalId
        GROUP BY p.id, p.nombre, p.costoProduccion, p.precioVenta
        ORDER BY SUM(dp.cantidad) DESC, p.nombre
        LIMIT 1
        """)
    Optional<PlatoDTO> findPlatoEstrella(@Param("unidadId") long unidadId, @Param("festivalId") long festivalId);
}
