package com.grupo14.hito2;

import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.entities.FoodTruck;
import com.grupo14.hito2.entities.Pedido;
import com.grupo14.hito2.entities.Plato;
import com.grupo14.hito2.entities.Responsable;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTests {

    private UnidadVenta unidad(long id) {
        Responsable responsable = new Responsable();
        responsable.setEstaActivo(true);
        Festival enCurso = new Festival();
        enCurso.setNombre("En curso");
        enCurso.setFechaInicio(LocalDate.now().minusDays(5));
        enCurso.setFechaFin(LocalDate.now().plusDays(5));
        FoodTruck unidad = new FoodTruck();
        unidad.setId(id);
        unidad.setResponsable(responsable);
        unidad.setFestival(enCurso);
        return unidad;
    }

    private Plato plato(long id, UnidadVenta unidad) {
        Plato plato = new Plato();
        plato.setId(id);
        plato.setNombre("Plato " + id);
        plato.setUnidadVenta(unidad);
        return plato;
    }

    @Test
    void pedidoNuevoTieneFechaDeHoy() {
        Pedido pedido = Pedido.nuevo(unidad(1));
        assertThat(pedido.getFechaPedido()).isEqualTo(LocalDate.now());
    }

    @Test
    void mismoPlatoDosVecesSumaLaCantidad() {
        UnidadVenta unidad = unidad(1);
        Pedido pedido = Pedido.nuevo(unidad);
        Plato pizza = plato(10, unidad);

        pedido.agregarDetalle(pizza, 2);
        pedido.agregarDetalle(pizza, 3);

        assertThat(pedido.getDetallesPedido()).hasSize(1);
        assertThat(pedido.getDetallesPedido().iterator().next().getCantidad()).isEqualTo(5);
    }

    @Test
    void elDetalleGuardaElPrecioDelMomentoDeLaVenta() {
        UnidadVenta unidad = unidad(1);
        Pedido pedido = Pedido.nuevo(unidad);
        Plato pizza = plato(10, unidad);
        pizza.setPrecioVenta(new BigDecimal("10000"));
        pizza.setCostoProduccion(new BigDecimal("3000"));

        pedido.agregarDetalle(pizza, 2);
        pizza.actualizar("Pizza", new BigDecimal("5000"), new BigDecimal("20000"));

        assertThat(pedido.getDetallesPedido().iterator().next().getPrecioUnitario()).isEqualByComparingTo("10000");
        assertThat(pedido.getDetallesPedido().iterator().next().getCostoUnitario()).isEqualByComparingTo("3000");
        assertThat(pedido.getTotal()).isEqualByComparingTo("20000");
    }

    @Test
    void noSePuedePedirUnPlatoDeOtraUnidad() {
        Pedido pedido = Pedido.nuevo(unidad(1));
        Plato ajeno = plato(20, unidad(2));

        assertThatThrownBy(() -> pedido.agregarDetalle(ajeno, 1))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no pertenece a esta unidad");
    }

    @Test
    void noSePuedePedirUnPlatoDadoDeBaja() {
        UnidadVenta unidad = unidad(1);
        Pedido pedido = Pedido.nuevo(unidad);
        Plato plato = plato(10, unidad);
        plato.setEliminado(true);

        assertThatThrownBy(() -> pedido.agregarDetalle(plato, 1))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("dado de baja");
    }

    @Test
    void unaUnidadNoHabilitadaNoPuedeVender() {
        UnidadVenta unidad = unidad(1);
        unidad.getResponsable().setEstaActivo(false);

        assertThatThrownBy(() -> Pedido.nuevo(unidad))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no está habilitada");
    }

    @Test
    void noSeCarganPedidosEnUnFestivalTerminado() {
        UnidadVenta unidad = unidad(1);
        unidad.getFestival().setFechaInicio(LocalDate.now().minusDays(30));
        unidad.getFestival().setFechaFin(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> Pedido.nuevo(unidad))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no está en curso");
    }
}
