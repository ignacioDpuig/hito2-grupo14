package com.grupo14.hito2.mappers;

import com.grupo14.hito2.dtos.CrearResponsableDTO;
import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.dtos.RankingUnidadDTO;
import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.entities.DatosUnidad;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.enums.TipoUnidad;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class UnidadVentaMapper {

    // No asigna festival ni responsable: eso lo resuelve el service
    public UnidadVenta toEntity(CrearUnidadVentaDTO dto) {
        UnidadVenta unidad = TipoUnidad.desde(dto.getTipoUnidad()).nuevaUnidad();
        unidad.actualizarDatosDelTipo(datosDelTipo(dto));
        unidad.setNombreComercial(dto.getNombreComercial());
        unidad.setCodigo(dto.getCodigo());
        unidad.setSuperficie(dto.getSuperficie());
        return unidad;
    }

    public EditarUnidadVentaDTO toEditarDTO(UnidadVenta unidad) {
        DatosUnidad datos = unidad.getDatosDelTipo();
        return new EditarUnidadVentaDTO(unidad.getCodigo(), unidad.getTipo(), unidad.getNombreComercial(),
                unidad.getSuperficie(), datos.patente(), datos.estaConectado(), datos.cantidadCarpas(), datos.tiempoMontaje());
    }

    // El código y el tipo del DTO se ignoran: la unidad conserva los suyos
    public void actualizar(UnidadVenta unidad, EditarUnidadVentaDTO dto) {
        unidad.setNombreComercial(dto.getNombreComercial());
        unidad.setSuperficie(dto.getSuperficie());
        unidad.actualizarDatosDelTipo(new DatosUnidad(dto.getPatente(), dto.getEstaConectado(),
                dto.getCantidadCarpas(), dto.getTiempoMontaje()));
    }

    public CrearResponsableDTO toResponsableDTO(CrearUnidadVentaDTO dto) {
        CrearResponsableDTO responsableDTO = new CrearResponsableDTO();
        responsableDTO.setNombre(dto.getNombreResponsable());
        responsableDTO.setApellido(dto.getApellidoResponsable());
        responsableDTO.setEmail(dto.getEmailResponsable());
        return responsableDTO;
    }

    public ReporteMayoresCanonDTO toReporteCanon(UnidadVenta unidad, BigDecimal canon) {
        return new ReporteMayoresCanonDTO(unidad.getNombreComercial(), unidad.getCodigo(), unidad.getTipoDescripcion(), canon);
    }

    public RankingUnidadDTO toRanking(UnidadVenta unidad, BigDecimal recaudacion) {
        return new RankingUnidadDTO(unidad.getNombreComercial(), unidad.getCodigo(), unidad.getTipoDescripcion(), recaudacion);
    }

    // Los datos del tipo pasan por actualizarDatosDelTipo, que los valida igual que en la modificación
    private DatosUnidad datosDelTipo(CrearUnidadVentaDTO dto) {
        return new DatosUnidad(dto.getPatente(), dto.getEstaConectado(), dto.getCantidadCarpas(), dto.getTiempoMontaje());
    }
}
