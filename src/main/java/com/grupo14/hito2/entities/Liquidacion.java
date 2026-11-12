package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object (no persiste): el sueldo de un empleado por un festival, con la fórmula del enunciado.
 *
 * <pre>
 *   sueldo = (sueldo base + plus) × porcentaje según los días trabajados
 * </pre>
 *
 * <ul>
 *   <li><b>Sueldo base:</b> lo fija el festival para el tipo de unidad (ConfiguracionCostos).</li>
 *   <li><b>Plus:</b> depende del puesto. Cocinero: el de su categoría. Cajero: un monto por año de antigüedad.</li>
 *   <li><b>Porcentaje:</b> proporcional a un mes de 30 días. Hasta 15 días: días × 50% / 30;
 *       de 16 a 30: días / 30; más de 30: los primeros 30 al 100% y el resto al 50%.</li>
 * </ul>
 *
 * Ejemplo: lavaplatos que trabajó 38 días → (30 + 8 × 50%) / 30 = 113,33% → (100.000 + 50.000) × 113,33% = 169.999,95.
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
