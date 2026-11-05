package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.entities.Festival;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
}
