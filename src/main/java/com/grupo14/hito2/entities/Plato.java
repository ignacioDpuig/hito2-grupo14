package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    @Column(precision = 12, scale = 2)
    private BigDecimal costoProduccion;
    @Column(precision = 12, scale = 2)
    private BigDecimal precioVenta;

    // Baja lógica: true si el plato ya no se ofrece
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean eliminado = false;

    @ManyToOne
    @JoinColumn(name = "unidad_venta_id")
    private UnidadVenta unidadVenta;

    @OneToMany(mappedBy = "plato")
    private Set<DetallePedido> pedidos = new HashSet<>();

    public static Plato nuevo(String nombre, BigDecimal costoProduccion, BigDecimal precioVenta, UnidadVenta unidad) {
        Plato plato = new Plato();
        plato.actualizar(nombre, costoProduccion, precioVenta);
        plato.setUnidadVenta(unidad);
        return plato;
    }

    // Baja lógica: deja de ofrecerse en pedidos nuevos, pero sigue en los reportes de lo ya vendido
    public void darDeBaja() {
        eliminado = true;
    }

    public void reactivar() {
        eliminado = false;
    }

    // Los pedidos ya hechos guardan su propio precio, así que cambiarlo acá solo afecta a las ventas nuevas
    public void actualizar(String nombre, BigDecimal costoProduccion, BigDecimal precioVenta) {
        this.nombre = nombre;
        this.costoProduccion = costoProduccion;
        this.precioVenta = precioVenta;
    }
}
