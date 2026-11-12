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
            "1,  1.67%,  2500.050000",
            "15, 25%,  37500.000000",
            "16, 53.33%, 79999.950000",
            "30, 100%, 150000",
            "31, 101.67%, 152500.050000",   // 31 / 15 + 1 = 3 bloques
            "44, 123.33%, 184999.950000",
            "45, 125%, 187500.000000",   // 45 / 15 + 1 = 4 bloques
    })
    void porcentajeSegunLosDiasDelFestival(long dias, String porcentaje, String total) {
        Liquidacion liquidacion = new Liquidacion(BigDecimal.valueOf(100000), BigDecimal.valueOf(50000), "Plus", dias);

        assertThat(liquidacion.porcentajeTexto()).isEqualTo(porcentaje);
        assertThat(liquidacion.total()).isEqualByComparingTo(total);
    }
}
