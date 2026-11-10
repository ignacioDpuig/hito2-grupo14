package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.dtos.RecaudacionPorUnidad;
import com.grupo14.hito2.dtos.ResumenVentas;
import com.grupo14.hito2.entities.DetallePedido;
import com.grupo14.hito2.entities.DetallePedidoId;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, DetallePedidoId> {

    // Precio cobrado * cantidad de cada plato vendido en el festival, agrupado por la unidad dueña del plato
    @Query("""
        SELECT new com.grupo14.hito2.dtos.RecaudacionPorUnidad(dp.plato.unidadVenta.id, SUM(dp.cantidad * dp.precioUnitario))
        FROM DetallePedido dp
        WHERE dp.pedido.festival.id = :festivalId
        GROUP BY dp.plato.unidadVenta.id
    """)
    List<RecaudacionPorUnidad> sumarRecaudacionPorUnidad(@Param("festivalId") long festivalId);

    // Ingresos, costo de los platos vendidos y cantidad de pedidos de una unidad en un festival
    @Query("""
        SELECT new com.grupo14.hito2.dtos.ResumenVentas(
            COALESCE(SUM(dp.cantidad * dp.precioUnitario), 0.0bd),
            COALESCE(SUM(dp.cantidad * dp.costoUnitario), 0.0bd),
            COUNT(DISTINCT dp.pedido.id))
        FROM DetallePedido dp
        WHERE dp.plato.unidadVenta.id = :unidadId
        AND dp.pedido.festival.id = :festivalId
    """)
    ResumenVentas resumirVentas(@Param("unidadId") long unidadId, @Param("festivalId") long festivalId);

    // Lo mismo que resumirVentas pero de todas las unidades del festival
    @Query("""
        SELECT new com.grupo14.hito2.dtos.ResumenVentas(
            COALESCE(SUM(dp.cantidad * dp.precioUnitario), 0.0bd),
            COALESCE(SUM(dp.cantidad * dp.costoUnitario), 0.0bd),
            COUNT(DISTINCT dp.pedido.id))
        FROM DetallePedido dp
        WHERE dp.pedido.festival.id = :festivalId
    """)
    ResumenVentas resumirVentasDelFestival(@Param("festivalId") long festivalId);

    // Lo mismo que resumirVentas pero entre dos fechas, ambas incluidas
    @Query("""
        SELECT new com.grupo14.hito2.dtos.ResumenVentas(
            COALESCE(SUM(dp.cantidad * dp.precioUnitario), 0.0bd),
            COALESCE(SUM(dp.cantidad * dp.costoUnitario), 0.0bd),
            COUNT(DISTINCT dp.pedido.id))
        FROM DetallePedido dp
        WHERE dp.plato.unidadVenta.id = :unidadId
        AND dp.pedido.fechaPedido BETWEEN :inicio AND :fin
    """)
    ResumenVentas resumirVentasEntreFechas(@Param("unidadId") long unidadId,
                                           @Param("inicio") LocalDate inicio,
                                           @Param("fin") LocalDate fin);
}
