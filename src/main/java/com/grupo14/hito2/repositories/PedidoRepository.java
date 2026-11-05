package com.grupo14.hito2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.grupo14.hito2.entities.Pedido;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("""
            SELECT DISTINCT p FROM Pedido p
            LEFT JOIN FETCH p.detallesPedido detalle
            LEFT JOIN FETCH detalle.plato
            WHERE p.unidadVenta.id = :unidadId
            ORDER BY p.fechaPedido DESC, p.id DESC
            """)
    List<Pedido> findByUnidadVentaIdWithDetalles(@Param("unidadId") long unidadId);
}
