package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.entities.Festival;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {

    // En curso o futuros: los que terminan hoy o después
    List<Festival> findByFechaFinGreaterThanEqualOrderByFechaInicio(LocalDate fecha);
}
