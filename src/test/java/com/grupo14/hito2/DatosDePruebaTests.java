package com.grupo14.hito2;

import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.LiquidacionService;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.EmpleadoService;
import com.grupo14.hito2.services.FestivalService;
import com.grupo14.hito2.services.PlatoService;
import com.grupo14.hito2.services.ReporteUnidadService;
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
    private LiquidacionService liquidacionService;
    @Autowired
    private UnidadVentaService unidadVentaService;
    @Autowired
    private ReporteUnidadService reporteUnidadService;
    @Autowired
    private PlatoService platoService;
    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void elAyudanteTrabajoEnDosFestivalesYTieneUnReciboPorCada() {
        long martinId = cuentaService.buscarPorEmail("martin@email.com").getId();
        List<LiquidacionDTO> recibos = liquidacionService.obtenerRecibos("martin@email.com");

        assertThat(unidadVentaService.obtenerHistorial(martinId)).hasSize(2);
        // Primavera 2026 (41 días, 150%): sueldo base de los food trucks $110.000 + plus ayudante $100.000
        // Festival 2024 (38 días, 150%): sueldo base general $100.000 + $100.000
        assertThat(recibos).satisfiesExactly(
                primavera -> {
                    assertThat(primavera.getNombreFestival()).isEqualTo("Festival Primavera 2026");
                    assertThat(primavera.getTotalNeto()).isEqualByComparingTo("315000");
                },
                anterior -> {
                    assertThat(anterior.getNombreFestival()).isEqualTo("Festival Gastronómico 2024");
                    assertThat(anterior.getTotalNeto()).isEqualByComparingTo("300000");
                });
        assertThat(unidadVentaService.obtenerUnidadEnFestivalEnCurso(martinId))
                .hasValueSatisfying(u -> assertThat(u.getNombreComercial()).isEqualTo("Food Truck La Primavera"));
    }

    @Test
    void losFoodTrucksDePrimaveraUsanSusCostosPropios() {
        // 20 m² × $700, sin conexión eléctrica
        assertThat(reporteUnidadService.generarReportes(unidadDe("sofia@email.com")).getRentabilidad().getCanon()).isEqualByComparingTo("14000");
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
            assertThat(liquidacionService.calcularLiquidacionDeEmpleadoDeLaUnidad(unidad, martinId)).isNotNull();
        }
        assertThat(unidadVentaService.obtenerHistorial(martinId)).hasSize(2);
    }

    @Test
    void laProvoletaEstaDadaDeBajaYNoSeOfreceEnPedidos() {
        UnidadVenta puesto = unidadDe("responsable@responsable");

        assertThat(platoService.obtenerTodosLosPlatosPorUnidad(puesto.getId()))
                .anyMatch(p -> p.getNombre().equals("Provoleta") && p.isEliminado());
        assertThat(platoService.obtenerPlatosActivosPorUnidad(puesto.getId()))
                .noneMatch(p -> p.getNombre().equals("Provoleta"));
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
                    assertThat(reporteUnidadService.generarReportes(u).getRentabilidad().getRentabilidadNeta()).isPositive();
                });
        assertThat(unidadVentaService.obtenerMayoresCanon(FESTIVAL_DE_OTONIO, 3)).hasSize(3);
        assertThat(unidadVentaService.obtenerRankingPorFestival(FESTIVAL_DE_OTONIO)).hasSize(5);
        assertThat(usuarioDetailsService.loadUserByUsername("pablo@email.com").isEnabled()).isTrue();
        assertThat(cuentaService.buscarPorEmail("pablo@email.com").isEstaActivo()).isTrue();
    }

    @Test
    void cadaFestivalDePruebaTieneCincoUnidadesConRentabilidadPositiva() {
        for (long festivalId : new long[]{1, 2, FESTIVAL_DE_OTONIO}) {
            List<UnidadVenta> unidades = unidadVentaService.obtenerUnidadesPorFestival(festivalId);
            assertThat(unidades).as("festival %s", festivalId).hasSize(5);
            assertThat(unidades).allSatisfy(unidad ->
                    assertThat(reporteUnidadService.generarReportes(unidad).getRentabilidad().getRentabilidadNeta())
                            .as("rentabilidad de " + unidad.getNombreComercial())
                            .isPositive());
        }
    }

    // Para la presentación: ninguna unidad cargada da pérdida y todas tienen quien cocine
    @Test
    void todasLasUnidadesTienenCocinaYRentabilidadPositiva() {
        for (Festival festival : festivalService.obtenerTodosFestivales()) {
            for (UnidadVenta unidad : unidadVentaService.obtenerUnidadesPorFestival(festival.getId())) {
                assertThat(reporteUnidadService.generarReportes(unidad).getRentabilidad().getRentabilidadNeta())
                        .as("rentabilidad neta de %s", unidad.getCodigo())
                        .isPositive();
                assertThat(liquidacionService.obtenerLiquidacionesDeLaUnidad(unidad))
                        .as("cocina de %s", unidad.getCodigo())
                        .extracting(LiquidacionDTO::getCargo)
                        .anyMatch(cargo -> !cargo.equals("Cajero"));
            }
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
