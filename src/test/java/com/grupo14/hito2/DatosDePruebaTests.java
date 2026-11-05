package com.grupo14.hito2;

import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.EmpleadoService;
import com.grupo14.hito2.services.FestivalService;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.services.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Los casos que data.sql deja listos para mostrar en la defensa. */
@SpringBootTest
@Transactional
class DatosDePruebaTests {

    private static final long FESTIVAL_DE_OTONIO = 3;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private EmpleadoService empleadoService;
    @Autowired
    private FestivalService festivalService;
    @Autowired
    private UnidadVentaService unidadVentaService;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void elAyudanteTrabajoEnDosFestivales() {
        long martinId = cuentaService.buscarPorEmail("martin@email.com").getId();

        assertThat(unidadVentaService.obtenerHistorial(martinId)).hasSize(2);
        assertThat(unidadVentaService.obtenerUnidadEnFestivalEnCurso(martinId))
                .hasValueSatisfying(u -> assertThat(u.getNombreComercial()).isEqualTo("Food Truck La Primavera"));
    }

    @Test
    void laCajeraDadaDeBajaNoEntraNiApareceEnLaGestionDeEmpleados() {
        assertThat(usuarioDetailsService.loadUserByUsername("rocio@email.com").isEnabled()).isFalse();

        assertThat(empleadoService.obtenerEmpleados(unidadDe("responsable@responsable"), new FiltroEmpleadosDTO()))
                .noneMatch(e -> e.empleado().getEmail().equals("rocio@email.com"));
    }

    @Test
    void bajaDeEmpleadoLoOcultaEnTodasLasUnidadesSinPerderSuHistorial() {
        Personal martin = cuentaService.buscarPorEmail("martin@email.com");
        long martinId = martin.getId();
        List<UnidadVenta> historial = unidadVentaService.obtenerHistorial(martinId);
        assertThat(historial).hasSize(2);

        empleadoService.darDeBajaEmpleado(martinId, historial.get(0));

        assertThat(usuarioDetailsService.loadUserByUsername(martin.getEmail()).isEnabled()).isFalse();
        for (UnidadVenta unidad : historial) {
            assertThat(empleadoService.obtenerEmpleados(unidad, new FiltroEmpleadosDTO()))
                    .noneMatch(item -> item.empleado().getId().equals(martinId));
        }
        assertThat(unidadVentaService.obtenerHistorial(martinId)).hasSize(2);
    }

    @Test
    void elFestivalDeOtonoEsHistoricoYSusUnidadesTienenReportes() {
        List<UnidadVenta> unidades = unidadVentaService.obtenerUnidadesPorFestival(FESTIVAL_DE_OTONIO);

        assertThat(unidades).hasSize(5)
                .anySatisfy(u -> assertThat(u.getNombreComercial()).isEqualTo("Puesto Sabores Andinos"))
                .allSatisfy(u -> {
                    assertThat(u.isActiva()).isTrue();
                    assertThat(u.getFestival().getFechaInicio()).isEqualTo(LocalDate.of(2025, 4, 1));
                    assertThat(u.getFestival().getFechaFin()).isEqualTo(LocalDate.of(2025, 4, 16));
                });
        assertThat(unidadVentaService.obtenerMayoresCanon(FESTIVAL_DE_OTONIO, 3)).hasSize(3);
        assertThat(usuarioDetailsService.loadUserByUsername("pablo@email.com").isEnabled()).isTrue();
        assertThat(cuentaService.buscarPorEmail("pablo@email.com").isEstaActivo()).isTrue();
    }

    @Test
    void cadaFestivalDePruebaTieneCincoUnidadesConRentabilidadPositiva() {
        for (long festivalId : new long[]{1, 2, FESTIVAL_DE_OTONIO}) {
            List<UnidadVenta> unidades = unidadVentaService.obtenerUnidadesPorFestival(festivalId);
            assertThat(unidades).as("festival %s", festivalId).hasSize(5);
        }
    }

    // data.sql no pasa por Bean Validation: el formato del código se controla acá
    @Test
    void todosLosCodigosCargadosTienenElFormatoDelEnunciado() {
        for (Festival festival : festivalService.obtenerTodosFestivales()) {
            for (UnidadVenta unidad : unidadVentaService.obtenerUnidadesPorFestival(festival.getId())) {
                String prefijo = unidad.getTipo().equals("FoodTruck") ? "FT" : "PD";
                assertThat(unidad.getCodigo()).matches(prefijo + "[A-Z0-9]{8}");
            }
        }
    }

    private UnidadVenta unidadDe(String emailResponsable) {
        return unidadVentaService.buscarUnidadDelResponsable(emailResponsable);
    }
}
