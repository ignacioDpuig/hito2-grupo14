package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.CierreCajaDTO;
import com.grupo14.hito2.dtos.CrearDetallePedidoDTO;
import com.grupo14.hito2.dtos.CrearPedidoDTO;
import com.grupo14.hito2.dtos.ResumenVentas;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.repositories.CierreCajaRepository;
import com.grupo14.hito2.repositories.DetallePedidoRepository;
import com.grupo14.hito2.repositories.PedidoRepository;
import com.grupo14.hito2.repositories.PlatoRepository;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PlatoRepository platoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CierreCajaRepository cierreCajaRepository;

    @Transactional
    public Pedido crearPedido(CrearPedidoDTO dto, UnidadVenta unidad) {
        if (cajaCerradaHoy(unidad)) {
            throw new ReglaDeNegocioException("La caja de hoy ya está cerrada: no se cargan más pedidos hasta mañana");
        }
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

    // Cierre de caja del día: si ya se cerró, lo que quedó guardado; si no, lo vendido hasta ahora
    @Transactional(readOnly = true)
    public CierreCajaDTO calcularCierreDelDia(UnidadVenta unidad) {
        LocalDate hoy = LocalDate.now();
        return cierreCajaRepository.findByUnidadVentaIdAndFecha(unidad.getId(), hoy)
                .map(this::toDTO)
                .orElseGet(() -> {
                    ResumenVentas ventas = detallePedidoRepository.resumirVentasEntreFechas(unidad.getId(), hoy, hoy);
                    return new CierreCajaDTO(unidad.getNombreComercial(), unidad.getFestival().getNombre(), hoy,
                            ventas.cantidadPedidos(), ventas.recaudacion(), false, null);
                });
    }

    // Se cierra una sola vez por día: queda guardado lo vendido y la unidad no acepta más pedidos hoy
    @Transactional
    public CierreCajaDTO cerrarCaja(UnidadVenta unidad, Personal cajero) {
        LocalDate hoy = LocalDate.now();
        if (cajaCerradaHoy(unidad)) {
            throw new ReglaDeNegocioException("La caja de hoy ya está cerrada");
        }
        ResumenVentas ventas = detallePedidoRepository.resumirVentasEntreFechas(unidad.getId(), hoy, hoy);
        CierreCaja cierre = CierreCaja.deHoy(unidad, cajero, ventas.cantidadPedidos(), ventas.recaudacion());
        return toDTO(cierreCajaRepository.save(cierre));
    }

    @Transactional(readOnly = true)
    public List<CierreCajaDTO> obtenerCierresDeUnidad(UnidadVenta unidad) {
        return cierreCajaRepository.findByUnidadVentaIdOrderByFechaDesc(unidad.getId()).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean cajaCerradaHoy(UnidadVenta unidad) {
        return cierreCajaRepository.existsByUnidadVentaIdAndFecha(unidad.getId(), LocalDate.now());
    }

    private CierreCajaDTO toDTO(CierreCaja cierre) {
        UnidadVenta unidad = cierre.getUnidadVenta();
        Personal cajero = cierre.getCajero();
        return new CierreCajaDTO(unidad.getNombreComercial(), unidad.getFestival().getNombre(), cierre.getFecha(),
                cierre.getCantidadPedidos(), cierre.getTotalVendido(), true,
                cajero.getNombre() + " " + cajero.getApellido());
    }

    private Plato buscarPlato(long platoId) {
        return platoRepository.findById(platoId)
                .orElseThrow(() -> new NoEncontradoException("Plato no encontrado"));
    }
}
