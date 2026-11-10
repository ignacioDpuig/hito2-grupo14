package com.grupo14.hito2.repositories;

import com.grupo14.hito2.entities.CierreCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    Optional<CierreCaja> findByUnidadVentaIdAndFecha(long unidadId, LocalDate fecha);

    boolean existsByUnidadVentaIdAndFecha(long unidadId, LocalDate fecha);

    List<CierreCaja> findByUnidadVentaIdOrderByFechaDesc(long unidadId);
}
