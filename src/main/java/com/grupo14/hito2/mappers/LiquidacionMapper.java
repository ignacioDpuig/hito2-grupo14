package com.grupo14.hito2.mappers;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.entities.Liquidacion;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Arma el recibo: copia los datos del empleado, la unidad y el festival, y los números que calculó Liquidacion. */
@Component
public class LiquidacionMapper {

    public LiquidacionDTO toDTO(Empleado empleado, UnidadVenta unidad, Liquidacion liquidacion) {
        Festival festival = unidad.getFestival();
        LiquidacionDTO recibo = new LiquidacionDTO();

        recibo.setEmpleadoId(empleado.getId());
        recibo.setNombreEmpleado(empleado.getNombre());
        recibo.setApellidoEmpleado(empleado.getApellido());
        recibo.setDni(empleado.getDni());
        recibo.setCargo(empleado.getCargo());
        recibo.setEspecialidadOTurno(empleado.getDetalleCargo());
        recibo.setDadoDeBaja(unidad.tieneEnStaff(empleado) && !unidad.tieneActivoEnStaff(empleado));
        recibo.setEmailEmpleado(empleado.getEmail());
        recibo.setFechaIngreso(empleado.getFechaIngreso());

        recibo.setUnidadId(unidad.getId());
        recibo.setUnidadCodigo(unidad.getCodigo());
        recibo.setUnidadNombre(unidad.getNombreComercial());
        recibo.setUnidadTipo(unidad.getTipoDescripcion());
        Personal responsable = unidad.getResponsable();
        if (responsable != null) {
            recibo.setResponsableNombre(responsable.getNombre());
            recibo.setResponsableApellido(responsable.getApellido());
            recibo.setResponsableDni(responsable.getDni());
            recibo.setResponsableEmail(responsable.getEmail());
        }

        recibo.setFestivalId(festival.getId());
        recibo.setNombreFestival(festival.getNombre());
        recibo.setFechaInicio(festival.getFechaInicio());
        recibo.setFechaFin(festival.getFechaFin());
        recibo.setDiasDuracion(festival.getDiasDuracion());

        recibo.setSueldoBase(liquidacion.sueldoBase());
        recibo.setPlus(liquidacion.plus());
        recibo.setConceptoPlus(liquidacion.conceptoPlus());
        recibo.setDesglose(liquidacion.desglose());
        recibo.setPorcentajeAplicado(liquidacion.porcentajeTexto());
        recibo.setTotalNeto(liquidacion.total());

        recibo.setFechaGeneracion(LocalDate.now());
        return recibo;
    }
}
