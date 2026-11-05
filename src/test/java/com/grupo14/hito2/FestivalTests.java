package com.grupo14.hito2;

import com.grupo14.hito2.entities.Festival;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FestivalTests {

    private Festival festival() {
        Festival festival = new Festival();
        festival.setFechaInicio(LocalDate.of(2024, 12, 15));
        festival.setFechaFin(LocalDate.of(2025, 1, 21));
        return festival;
    }

    @Test
    void estaEnCursoIncluyeElPrimerYUltimoDia() {
        assertThat(festival().estaEnCurso(LocalDate.of(2024, 12, 15))).isTrue();
        assertThat(festival().estaEnCurso(LocalDate.of(2025, 1, 21))).isTrue();
        assertThat(festival().estaEnCurso(LocalDate.of(2024, 12, 14))).isFalse();
        assertThat(festival().estaEnCurso(LocalDate.of(2025, 1, 22))).isFalse();
    }

    @Test
    void contieneSoloPeriodosDentroDelFestival() {
        assertThat(festival().contiene(LocalDate.of(2024, 12, 20), LocalDate.of(2024, 12, 25))).isTrue();
        assertThat(festival().contiene(LocalDate.of(2024, 12, 10), LocalDate.of(2024, 12, 25))).isFalse();
        assertThat(festival().contiene(LocalDate.of(2025, 1, 20), LocalDate.of(2025, 1, 25))).isFalse();
    }

    @Test
    void prorratearCuentaAmbosDiasYRedondeaAlCentavo() {
        BigDecimal canon = new BigDecimal("14750");
        // 2 de 38 días: 14.750 × 2 / 38 = 776,315... → 776,32
        assertThat(festival().prorratear(canon, LocalDate.of(2024, 12, 20), LocalDate.of(2024, 12, 21))).isEqualByComparingTo("776.32");
        assertThat(festival().prorratear(canon, LocalDate.of(2024, 12, 15), LocalDate.of(2025, 1, 21))).isEqualByComparingTo("14750");
    }
}
