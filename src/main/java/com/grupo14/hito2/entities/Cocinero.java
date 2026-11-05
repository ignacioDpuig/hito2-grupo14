package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.enums.TipoPersonal;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cocinero extends Empleado {
    private String especialidad;

    @Enumerated(EnumType.STRING)
    private CategoriaCocinero categoria;

    @Override
    public BigDecimal calcularPlus(ConfiguracionCostos costos, LocalDate finDelFestival) {
        return categoria.getPlus(costos);
    }

    @Override
    public String getCargo() {
        return categoria.getDescripcion();
    }

    @Override
    public String getDetalleCargo() {
        return especialidad;
    }

    @Override
    public String getConceptoPlus(LocalDate finDelFestival) {
        return "Plus " + getCargo();
    }

    @Override
    public TipoPersonal getTipoPersonal() {
        return TipoPersonal.COCINERO;
    }

    @Override
    public DatosPuesto getDatosPuesto() {
        return new DatosPuesto(especialidad, categoria, null);
    }

    @Override
    public void actualizarDatosDelPuesto(DatosPuesto datos) {
        if (datos.categoria() == null) {
            throw new ReglaDeNegocioException("Hay que elegir la categoría del cocinero");
        }
        this.especialidad = datos.especialidad();
        this.categoria = datos.categoria();
    }
}
