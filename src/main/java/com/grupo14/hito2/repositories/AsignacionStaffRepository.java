package com.grupo14.hito2.repositories;

import com.grupo14.hito2.entities.AsignacionStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionStaffRepository extends JpaRepository<AsignacionStaff, Long> {

    // Un empleado puede operar mientras siga activo en alguna unidad
    boolean existsByPersonalIdAndEliminadoFalse(long personalId);
}
