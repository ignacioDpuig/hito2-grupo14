package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object (no persiste): el sueldo de un empleado por un festival, con la fórmula del enunciado.
 *
 * <pre>
 *   sueldo = (sueldo base + plus) × porcentaje por duración del festival
 * </pre>
 *
 * <ul>
 *   <li><b>Sueldo base:</b> lo fija el festival para el tipo de unidad (ConfiguracionCostos).</li>
 *   <li><b>Plus:</b> depende del puesto. Cocinero: el de su categoría. Cajero: un monto por año de antigüedad.</li>
 *   <li><b>Porcentaje:</b> hasta 15 días 50%, de 16 a 30 días 100%, más de 30 días 50% por cada bloque
 *       de 15 días, contando (días / 15) + 1 bloques. Ej.: 38 días → 3 bloques → 150%.</li>
 * </ul>
 *
 * Ejemplo: lavaplatos en un festival de 38 días → (100.000 + 50.000) × 150% = 225.000.
 */
public record Liquidacion(BigDecimal sueldoBase, BigDecimal plus, String conceptoPlus, long diasFestival) {

    private static final BigDecimal MEDIO = new BigDecimal("0.5");
    private static final BigDecimal DIAS_BASE = new BigDecimal("30");
    private static final BigDecimal CIEN = new BigDecimal("100");

    public BigDecimal total() {
        return sueldoBase.add(plus).multiply(porcentaje());
    }

    public BigDecimal porcentaje() {
        if (diasFestival <= 0) {
            return BigDecimal.ZERO;
        }
        if (diasFestival <= 15) {
            return BigDecimal.valueOf(diasFestival)
                    .multiply(MEDIO)
                    .divide(DIAS_BASE, 6, RoundingMode.HALF_UP);
        }
        if (diasFestival <= 30) {
            return BigDecimal.valueOf(diasFestival)
                    .divide(DIAS_BASE, 6, RoundingMode.HALF_UP);
        }

        long diasExtra = diasFestival - 30;
        BigDecimal diasPagos = BigDecimal.valueOf(30)
                .add(BigDecimal.valueOf(diasExtra).multiply(MEDIO));

        return diasPagos.divide(DIAS_BASE, 6, RoundingMode.HALF_UP);
    }

    public String desglose() {
        if (diasFestival <= 15) {
            return diasFestival + " días al 50%";
        }
        if (diasFestival <= 30) {
            return diasFestival + " días al 100%";
        }
        long diasExtra = diasFestival - 30;
        return diasFestival + " días: 30 al 100% y " + diasExtra + " al 50%";
    }

    public String porcentajeTexto() {
        BigDecimal porcentaje = porcentaje()
                .multiply(CIEN)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        return porcentaje.toPlainString() + "%";
    }
}
