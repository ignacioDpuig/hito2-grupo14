package com.grupo14.hito2;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.mappers.LiquidacionMapper;
import com.grupo14.hito2.services.LiquidacionService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fija los resultados de los cálculos de negocio para que el refactor no los cambie.
 * Valores tomados del enunciado y de data.sql.
 */
class CalculosCaracterizacionTests {

    private final LiquidacionService liquidacionService =
            new LiquidacionService(null, null, new LiquidacionMapper());

    private ConfiguracionCostos config() {
        ConfiguracionCostos config = new ConfiguracionCostos();
        config.setCostoSuperficie(BigDecimal.valueOf(500));
        config.setCostoMontaje(BigDecimal.valueOf(10));
        config.setPlusElectricidad(BigDecimal.valueOf(2000));
        config.setSueldoBase(BigDecimal.valueOf(100000));
        config.setPlusCocinero(BigDecimal.valueOf(150000));
        config.setPlusCocineroAyudante(BigDecimal.valueOf(100000));
        config.setPlusLavaplatos(BigDecimal.valueOf(50000));
        config.setMontoAntiguedad(BigDecimal.valueOf(5000));
        return config;
    }

    private Festival festivalDeDias(long dias) {
        Festival festival = new Festival();
        festival.setId(1L);
        festival.setNombre("Festival");
        festival.setFechaInicio(LocalDate.of(2024, 12, 15));
        festival.setFechaFin(LocalDate.of(2024, 12, 15).plusDays(dias - 1));
        agregarConfiguracion(festival, config(), 1L);
        return festival;
    }

    private void agregarConfiguracion(Festival festival, ConfiguracionCostos config, long id) {
        config.setId(id);
        config.setFestival(festival);
        festival.getConfiguraciones().add(config);
    }

    // Unidad de un festival de esa duración: la liquidación usa los costos del festival para su tipo
    private UnidadVenta unidadDeDias(long dias) {
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setFestival(festivalDeDias(dias));
        return puesto;
    }

    private Cocinero cocinero(CategoriaCocinero categoria) {
        Cocinero cocinero = new Cocinero();
        cocinero.setNombre("Alberto");
        cocinero.setFechaIngreso(LocalDate.of(2023, 4, 12));
        cocinero.setEspecialidad("Parrillas");
        cocinero.setCategoria(categoria);
        return cocinero;
    }

    @Test
    void liquidacionLavaplatos38Dias() {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.LAVAPLATOS), unidadDeDias(38));

        assertThat(recibo.getTotalNeto()).isEqualByComparingTo("225000");
        assertThat(recibo.getPlus()).isEqualByComparingTo("50000");
        assertThat(recibo.getCargo()).isEqualTo("Lavaplatos");
        assertThat(recibo.getConceptoPlus()).isEqualTo("Plus Lavaplatos");
        assertThat(recibo.getEspecialidadOTurno()).isEqualTo("Parrillas");
        assertThat(recibo.getPorcentajeAplicado()).isEqualTo("150%");
        assertThat(recibo.getDesglose()).isEqualTo("38 días: 3 bloques de 15 días al 50% c/u");
        assertThat(recibo.getDiasDuracion()).isEqualTo(38);
    }

    @Test
    void liquidacionCocinero38Dias() {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.COCINERO), unidadDeDias(38));
        assertThat(recibo.getTotalNeto()).isEqualByComparingTo("375000");
    }

    @Test
    void liquidacionAyudanteHasta15DiasCobra50() {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.COCINERO_AYUDANTE), unidadDeDias(10));
        assertThat(recibo.getTotalNeto()).isEqualByComparingTo("100000");
        assertThat(recibo.getPorcentajeAplicado()).isEqualTo("50%");
        assertThat(recibo.getDesglose()).isEqualTo("10 días al 50%");
    }

    @Test
    void liquidacionCocineroEntre16y30DiasCobra100() {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.COCINERO), unidadDeDias(20));
        assertThat(recibo.getTotalNeto()).isEqualByComparingTo("250000");
        assertThat(recibo.getPorcentajeAplicado()).isEqualTo("100%");
        assertThat(recibo.getDesglose()).isEqualTo("20 días al 100%");
    }

    @Test
    void liquidacionCajero52DiasCon3AniosDeAntiguedad() {
        Cajero cajero = new Cajero();
        cajero.setNombre("Ana");
        cajero.setTurno("Noche");
        // El festival termina el 04/02/2025: a esa fecha tiene 3 años de antigüedad
        cajero.setFechaIngreso(LocalDate.of(2022, 1, 10));

        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cajero, unidadDeDias(52));

        assertThat(recibo.getTotalNeto()).isEqualByComparingTo("230000");
        assertThat(recibo.getPlus()).isEqualByComparingTo("15000");
        assertThat(recibo.getCargo()).isEqualTo("Cajero");
        assertThat(recibo.getConceptoPlus()).isEqualTo("Antigüedad (3 años)");
        assertThat(recibo.getEspecialidadOTurno()).isEqualTo("Noche");
    }

    @Test
    void laAntiguedadEsLaQueTeniaAlTerminarElFestivalYNoCambiaConElTiempo() {
        Cajero cajero = new Cajero();
        cajero.setTurno("Mañana");
        // Festival del 15/12/2024 al 21/01/2025: al terminar tenía 1 año (hoy tendría más)
        cajero.setFechaIngreso(LocalDate.of(2023, 10, 1));

        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(cajero, unidadDeDias(38));

        assertThat(recibo.getPlus()).isEqualByComparingTo("5000");
        assertThat(recibo.getConceptoPlus()).isEqualTo("Antigüedad (1 año)");
    }

    @Test
    void siIngresoDespuesDelFestivalNoTieneAntiguedadEnEse() {
        Cajero cajero = new Cajero();
        cajero.setFechaIngreso(LocalDate.of(2026, 1, 1));

        assertThat(cajero.getAniosAntiguedad(LocalDate.of(2025, 1, 21))).isZero();
    }

    @Test
    void soloElCajeroPuedeCargarPedidos() {
        assertThat(new Cajero().puedeCargarPedidos()).isTrue();
        assertThat(cocinero(CategoriaCocinero.COCINERO).puedeCargarPedidos()).isFalse();
        assertThat(new Responsable().puedeCargarPedidos()).isFalse();
    }

    @Test
    void canonFoodTruckConectado() {
        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setSuperficie(25.5f);
        foodTruck.setEstaConectado(true);
        assertThat(foodTruck.calcularCanon(config())).isEqualByComparingTo("14750");
    }

    @Test
    void canonFoodTruckSinConexion() {
        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setSuperficie(25.5f);
        assertThat(foodTruck.calcularCanon(config())).isEqualByComparingTo("12750");
    }

    @Test
    void canonPuestoDesarmable() {
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setSuperficie(30f);
        puesto.setTiempoMontaje(2.5f);
        assertThat(puesto.calcularCanon(config())).isEqualByComparingTo("14975");
    }

    @Test
    void losCostosDelTipoDeUnidadLeGananALosGenerales() {
        Festival festival = festivalDeDias(20);
        ConfiguracionCostos foodTrucks = config();
        foodTrucks.setTipoUnidad(TipoUnidad.FOOD_TRUCK);
        foodTrucks.setCostoSuperficie(BigDecimal.valueOf(700));
        foodTrucks.setSueldoBase(BigDecimal.valueOf(120000));
        agregarConfiguracion(festival, foodTrucks, 2L);

        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setFestival(festival);
        foodTruck.setSuperficie(25.5f);
        foodTruck.setEstaConectado(true);
        PuestoDesarmable puesto = new PuestoDesarmable();
        puesto.setFestival(festival);
        puesto.setSuperficie(30f);
        puesto.setTiempoMontaje(2.5f);

        // 25,5 × 700 + 2.000; el puesto no tiene costos propios y usa los generales
        assertThat(foodTruck.calcularCanon()).isEqualByComparingTo("19850");
        assertThat(puesto.calcularCanon()).isEqualByComparingTo("14975");
        // Sueldo base del tipo: (120.000 + 150.000) × 100%
        assertThat(liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.COCINERO), foodTruck).getTotalNeto())
                .isEqualByComparingTo("270000");
        assertThat(liquidacionService.calcularLiquidacion(cocinero(CategoriaCocinero.COCINERO), puesto).getTotalNeto())
                .isEqualByComparingTo("250000");
    }

    @Test
    void sinCostosConfiguradosNoSePuedeCalcular() {
        Festival festival = new Festival();
        festival.setNombre("Sin costos");
        FoodTruck foodTruck = new FoodTruck();
        foodTruck.setFestival(festival);

        assertThatThrownBy(foodTruck::calcularCanon).isInstanceOf(NoEncontradoException.class);
    }
}
