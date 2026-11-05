package com.grupo14.hito2;

import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Corre contra la base H2 de los tests, cargada con data.sql. */
@SpringBootTest
@Transactional
class ReportesIntegracionTests {

    @Autowired
    private UnidadVentaService unidadVentaService;

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
}
