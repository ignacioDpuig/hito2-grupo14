package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.grupo14.hito2.enums.TipoPersonal;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cajero extends Empleado {
    private String turno;

    @Override
    public BigDecimal calcularPlus(ConfiguracionCostos costos, LocalDate finDelFestival) {
        return costos.getMontoAntiguedad().multiply(BigDecimal.valueOf(getAniosAntiguedad(finDelFestival)));
    }

    @Override
    public String getCargo() {
        return "Cajero";
    }

    @Override
    public String getDetalleCargo() {
        return turno;
    }

    @Override
    public TipoPersonal getTipoPersonal() {
        return TipoPersonal.CAJERO;
    }

    @Override
    public DatosPuesto getDatosPuesto() {
        return new DatosPuesto(null, null, turno);
    }

    @Override
    public void actualizarDatosDelPuesto(DatosPuesto datos) {
        if (datos.turno() == null || datos.turno().isBlank()) {
            throw new ReglaDeNegocioException("Hay que elegir el turno del cajero");
        }
        this.turno = datos.turno();
    }

    @Override
    public boolean puedeCargarPedidos() {
        return true;
    }

    @Override
    public String getConceptoPlus(LocalDate finDelFestival) {
        long anios = getAniosAntiguedad(finDelFestival);
        return "Antigüedad (" + anios + (anios == 1 ? " año)" : " años)");
    }
}
