package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.entities.UnidadVenta;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

    Optional<UnidadVenta> findByCodigo(String codigo);

    Optional<UnidadVenta> findByResponsableEmail(String email);

    List<UnidadVenta> findByFestivalId(long festivalId);

    // Sin contar a la propia unidad (excluirId null en un alta)
    @Query("SELECT COUNT(f) > 0 FROM FoodTruck f WHERE f.patente = :patente AND (:excluirId IS NULL OR f.id <> :excluirId)")
    boolean existsFoodTruckConPatente(@Param("patente") String patente, @Param("excluirId") Long excluirId);
}
