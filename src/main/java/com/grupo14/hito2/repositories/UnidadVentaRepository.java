package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.entities.UnidadVenta;
import java.util.List;

@Repository
public interface UnidadVentaRepository extends JpaRepository<UnidadVenta, Long> {

    List<UnidadVenta> findByFestivalId(long festivalId);
}
