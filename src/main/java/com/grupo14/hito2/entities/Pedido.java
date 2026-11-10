package com.grupo14.hito2.entities;

import com.grupo14.hito2.exceptions.ReglaDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaPedido;

    @ManyToOne
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @ManyToOne
    @JoinColumn(name = "unidad_venta_id")
    private UnidadVenta unidadVenta;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pedido", cascade = CascadeType.ALL)
    private Set<DetallePedido> detallesPedido = new HashSet<>();

    // Un pedido nuevo siempre tiene la fecha del día (el DTO no trae fecha, así que no se puede cargar otra)
    // y el festival de la unidad que lo entrega
    public static Pedido nuevo(UnidadVenta unidad) {
        // Una unidad se habilita cuando su responsable activa la cuenta: hasta entonces no puede vender
        if (!unidad.isActiva()) {
            throw new ReglaDeNegocioException("La unidad todavía no está habilitada: su responsable tiene que activar la cuenta");
        }
        if (!unidad.getFestival().estaEnCurso(LocalDate.now())) {
            throw new ReglaDeNegocioException("El festival " + unidad.getFestival().getNombre() + " no está en curso: solo se cargan pedidos durante el festival");
        }
        Pedido pedido = new Pedido();
        pedido.setFechaPedido(LocalDate.now());
        pedido.setUnidadVenta(unidad);
        pedido.setFestival(unidad.getFestival());
        return pedido;
    }

    // Si el plato ya está en el pedido se suma la cantidad: la clave es (pedido, plato) y no puede repetirse
    public void agregarDetalle(Plato plato, int cantidad) {
        if (!unidadVenta.ofrece(plato)) {
            throw new ReglaDeNegocioException("El plato " + plato.getNombre() + " no pertenece a esta unidad de venta");
        }
        if (plato.isEliminado()) {
            throw new ReglaDeNegocioException("El plato " + plato.getNombre() + " fue dado de baja");
        }
        for (DetallePedido existente : detallesPedido) {
            if (existente.getPlato().getId().equals(plato.getId())) {
                existente.setCantidad(existente.getCantidad() + cantidad);
                return;
            }
        }
        DetallePedido detalle = new DetallePedido();
        detalle.setId(new DetallePedidoId(id, plato.getId()));
        detalle.setPedido(this);
        detalle.setPlato(plato);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(plato.getPrecioVenta());
        detalle.setCostoUnitario(plato.getCostoProduccion());
        detallesPedido.add(detalle);
    }

    public BigDecimal getTotal() {
        return detallesPedido.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}