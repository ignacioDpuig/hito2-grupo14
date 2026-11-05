package com.grupo14.hito2;

import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Corre contra la base H2 de los tests, cargada con data.sql. */
@SpringBootTest
@Transactional
class ReportesIntegracionTests {

    @Autowired
    private UnidadVentaService unidadVentaService;
    @Autowired
    private CuentaService cuentaService;

    @Test
    void mayoresCanon() {
        List<ReporteMayoresCanonDTO> reporte = unidadVentaService.obtenerMayoresCanon(1, 1);

        assertThat(reporte).hasSize(1);
        assertThat(reporte.get(0).getCodigo()).isEqualTo("PDPARRLL01");
        assertThat(reporte.get(0).getCanon()).isEqualByComparingTo("14975");
    }

    @Test
    void laCantidadDelReporteDeCanonTieneQueEstarEntre1y20() {
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, 0)).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, -1)).isInstanceOf(ReglaDeNegocioException.class);
        assertThatThrownBy(() -> unidadVentaService.obtenerMayoresCanon(1, 21)).isInstanceOf(ReglaDeNegocioException.class);
        assertThat(unidadVentaService.obtenerMayoresCanon(1, 20)).hasSize(5);
    }

    @Test
    void elResponsableModificaSuUnidadSinCambiarCodigoNiTipo() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        assertThat(dto.getTipoUnidad()).isEqualTo("FoodTruck");
        dto.setNombreComercial("Sabores Renovados");
        dto.setSuperficie(30f);
        dto.setEstaConectado(false);
        dto.setCodigo("PDOTRO0001");
        dto.setTipoUnidad("PuestoDesarmable");

        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);

        UnidadVenta unidad = unidadDe("juan@email.com");
        assertThat(unidad.getNombreComercial()).isEqualTo("Sabores Renovados");
        assertThat(unidad.getCodigo()).isEqualTo("FTSDELSUR1");
        assertThat(unidad.getTipo()).isEqualTo("FoodTruck");
        assertThat(unidadVentaService.obtenerMayoresCanon(1, 2)).extracting(ReporteMayoresCanonDTO::getCanon)
                .usingElementComparator(BigDecimal::compareTo)
                .contains(new BigDecimal("15000"));
    }

    @Test
    void laPatenteSeGuardaNormalizadaYNoSeRepite() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        dto.setPatente("ab 123-cd");   // la del food truck de Sofía
        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto))
                .hasMessageContaining("Ya existe un food truck con la patente AB123CD");

        dto.setPatente("aa 111-bb");
        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);
        assertThat(unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com")).getPatente()).isEqualTo("AA111BB");

        // La propia patente no cuenta como repetida
        unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto);
    }

    @Test
    void unFoodTruckNoPuedeQuedarSinPatente() {
        EditarUnidadVentaDTO dto = unidadVentaService.obtenerUnidadParaEditar(unidadDe("juan@email.com"));
        dto.setPatente(" ");

        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("juan@email.com"), dto))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("patente");
    }

    @Test
    void altaDeUnidadValidaCodigoRepetidoYDatosDelTipo() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setCodigo("FTSDELSUR1");
        dto.setTipoUnidad("FoodTruck");
        dto.setPatente("AA111AA");
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("Ya existe una unidad con el código");

        dto.setCodigo("PDNUEVO001");
        dto.setTipoUnidad("PuestoDesarmable");
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("al menos una carpa");
    }

    // Un puesto válido en el Festival Primavera 2026 (en curso)
    private CrearUnidadVentaDTO puestoNuevo() {
        CrearUnidadVentaDTO dto = new CrearUnidadVentaDTO();
        dto.setCodigo("PDNUEVO001");
        dto.setTipoUnidad("PuestoDesarmable");
        dto.setNombreComercial("Puesto nuevo");
        dto.setSuperficie(10f);
        dto.setCantidadCarpas(1);
        dto.setTiempoMontaje(30f);
        dto.setFestivalId(2L);
        dto.setNombreResponsable("Ana");
        dto.setApellidoResponsable("Paz");
        dto.setEmailResponsable("ana.paz@email.com");
        return dto;
    }

    // El registro solo pide nombre, apellido y mail: dos responsables sin DNI no chocan por el DNI único
    @Test
    void elResponsableSeRegistraSinDniNiFechaDeNacimiento() {
        UnidadVenta unidad = unidadVentaService.crearUnidadVenta(puestoNuevo());
        CrearUnidadVentaDTO otro = puestoNuevo();
        otro.setCodigo("PDNUEVO002");
        otro.setEmailResponsable("otro.responsable@email.com");
        unidadVentaService.crearUnidadVenta(otro);

        Personal responsable = cuentaService.buscarPorEmail("ana.paz@email.com");
        assertThat(responsable.getNombre()).isEqualTo("Ana");
        assertThat(responsable.getApellido()).isEqualTo("Paz");
        assertThat(responsable.getDni()).isNull();
        assertThat(responsable.getFechaNacimiento()).isNull();
        assertThat(responsable.isEstaActivo()).isFalse();
        assertThat(unidad.isActiva()).isFalse();
        assertThat(cuentaService.buscarPorEmail("otro.responsable@email.com").getDni()).isNull();
    }

    @Test
    void noSeRegistranUnidadesEnUnFestivalTerminado() {
        CrearUnidadVentaDTO dto = puestoNuevo();
        dto.setFestivalId(1L);
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto)).hasMessageContaining("ya terminó");

        assertThat(unidadVentaService.crearUnidadVenta(puestoNuevo()).getFestival().getNombre())
                .isEqualTo("Festival Primavera 2026");
    }

    @Test
    void elCanonDeUnaUnidadNoPuedeDarNegativo() {
        // 1 m² × $500 − 100 min × $10 = −$500
        CrearUnidadVentaDTO dto = puestoNuevo();
        dto.setSuperficie(1f);
        dto.setTiempoMontaje(100f);
        assertThatThrownBy(() -> unidadVentaService.crearUnidadVenta(dto))
                .hasMessageContaining("canon da negativo");
        assertThat(cuentaService.obtenerPersonalPorEmail("ana.paz@email.com")).isEmpty();

        EditarUnidadVentaDTO edicion = unidadVentaService.obtenerUnidadParaEditar(unidadDe("responsable@responsable"));
        edicion.setSuperficie(1f);
        edicion.setTiempoMontaje(100f);
        assertThatThrownBy(() -> unidadVentaService.modificarUnidad(unidadDe("responsable@responsable"), edicion))
                .hasMessageContaining("canon da negativo");
    }

    private UnidadVenta unidadDe(String emailResponsable) {
        return unidadVentaService.buscarUnidadDelResponsable(emailResponsable);
    }
}
