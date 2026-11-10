package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.dtos.ResumenVentas;
import com.grupo14.hito2.entities.DetallePedido;
import com.grupo14.hito2.entities.DetallePedidoId;

import java.time.LocalDate;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, DetallePedidoId> {


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
