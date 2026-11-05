package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.grupo14.hito2.enums.TipoPersonal;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Personal que cobra sueldo por festival (cajeros y cocineros).
 * liquidar es un Template Method: arma el sueldo igual para todos y cada subclase solo dice cuál es su plus.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public abstract class Empleado extends Personal {

    // El plus de cada puesto, con los costos del festival y a la fecha en que terminó
    public abstract BigDecimal calcularPlus(ConfiguracionCostos costos, LocalDate finDelFestival);

    public abstract String getCargo();

    public abstract String getDetalleCargo();

    public abstract String getConceptoPlus(LocalDate finDelFestival);

    public abstract TipoPersonal getTipoPersonal();

    public abstract DatosPuesto getDatosPuesto();

    // Cada tipo toma de los datos los que le corresponden y valida que estén
    public abstract void actualizarDatosDelPuesto(DatosPuesto datos);

    @Override
    public boolean requiereUnidadActiva() {
        return true;
    }

    // Sueldo por el festival de esa unidad, con los costos que el festival fija para su tipo.
    // La antigüedad se cuenta al terminar el festival: así el recibo de cada festival no cambia con el tiempo
    public Liquidacion liquidar(UnidadVenta unidad) {
        Festival festival = unidad.getFestival();
        ConfiguracionCostos costos = unidad.getConfiguracionCostos();
        LocalDate finDelFestival = festival.getFechaFin();
        return new Liquidacion(costos.getSueldoBase(), calcularPlus(costos, finDelFestival),
                getConceptoPlus(finDelFestival), festival.getDiasDuracion());
    }
}
