package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.CrearDetallePedidoDTO;
import com.grupo14.hito2.dtos.CrearPedidoDTO;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.repositories.PedidoRepository;
import com.grupo14.hito2.repositories.PlatoRepository;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PlatoRepository platoRepository;

    @Transactional
    public Pedido crearPedido(CrearPedidoDTO dto, UnidadVenta unidad) {
        Pedido pedido = Pedido.nuevo(unidad);

        if (dto.getDetalles() != null) {
            for (CrearDetallePedidoDTO detalle : dto.getDetalles()) {
                if (detalle.getPlatoId() != null && detalle.getCantidad() > 0) {
                    pedido.agregarDetalle(buscarPlato(detalle.getPlatoId()), detalle.getCantidad());
                }
            }
        }
        if (pedido.getDetallesPedido().isEmpty()) {
            throw new ReglaDeNegocioException("El pedido debe tener al menos un plato");
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosDeUnidad(long unidadId) {
        return pedidoRepository.findByUnidadVentaIdWithDetalles(unidadId);
    }

    private Plato buscarPlato(long platoId) {
        return platoRepository.findById(platoId)
                .orElseThrow(() -> new NoEncontradoException("Plato no encontrado"));
    }
}
