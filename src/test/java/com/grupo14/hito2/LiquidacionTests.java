package com.grupo14.hito2;

import com.grupo14.hito2.entities.Liquidacion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Los bordes del porcentaje por duración del festival: (sueldo base + plus) × porcentaje. */
class LiquidacionTests {

    @ParameterizedTest(name = "{0} días → {1}")
    @CsvSource({
            "1,  50%,  75000",
            "15, 50%,  75000",
            "16, 100%, 150000",
            "30, 100%, 150000",
            "31, 150%, 225000",   // 31 / 15 + 1 = 3 bloques
            "44, 150%, 225000",
            "45, 200%, 300000",   // 45 / 15 + 1 = 4 bloques
    })
    void porcentajeSegunLosDiasDelFestival(long dias, String porcentaje, String total) {
        Liquidacion liquidacion = new Liquidacion(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), "Plus", dias);

        assertThat(liquidacion.porcentajeTexto()).isEqualTo(porcentaje);
        assertThat(liquidacion.total()).isEqualByComparingTo(total);
    }
}
