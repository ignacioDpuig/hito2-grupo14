package com.grupo14.hito2;

import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.enums.CategoriaCocinero;
import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.exceptions.NoEncontradoException;
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

    private Cocinero cocinero(CategoriaCocinero categoria) {
        Cocinero cocinero = new Cocinero();
        cocinero.setNombre("Alberto");
        cocinero.setFechaIngreso(LocalDate.of(2023, 4, 12));
        cocinero.setEspecialidad("Parrillas");
        cocinero.setCategoria(categoria);
        return cocinero;
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
