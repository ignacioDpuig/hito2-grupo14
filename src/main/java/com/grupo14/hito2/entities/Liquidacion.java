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

    private static final BigDecimal MEDIO_SUELDO = new BigDecimal("0.5");

    public BigDecimal total() {
        return sueldoBase.add(plus).multiply(porcentaje());
    }

    public BigDecimal porcentaje() {
        if (diasFestival <= 15) {
            return MEDIO_SUELDO;
        }
        if (diasFestival <= 30) {
            return BigDecimal.ONE;
        }
        return MEDIO_SUELDO.multiply(BigDecimal.valueOf(bloquesDe15Dias()));
    }

    // Lo que se muestra en el recibo para explicar el porcentaje
    public String desglose() {
        if (diasFestival <= 30) {
            return diasFestival + " días al " + porcentajeTexto();
        }
        return diasFestival + " días: " + bloquesDe15Dias() + " bloques de 15 días al 50% c/u";
    }

    public String porcentajeTexto() {
        return porcentaje().multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private long bloquesDe15Dias() {
        return diasFestival / 15 + 1;
    }
}
