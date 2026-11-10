package com.grupo14.hito2.entities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.grupo14.hito2.enums.TipoUnidad;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String temporada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "festival", cascade = CascadeType.ALL)
    private Set<UnidadVenta> unidades = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "festival")
    private Set<Pedido> pedidos = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "festival", cascade = CascadeType.ALL)
    private Set<ConfiguracionCostos> configuraciones = new HashSet<>();

    // Cuenta el día de inicio y el de fin
    public long getDiasDuracion() {
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }

    // Solo corre al guardar con JPA: los inserts de data.sql no pasan por acá
    @PrePersist
    @PreUpdate
    void validarPeriodo() {
        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            throw new ReglaDeNegocioException("La fecha de inicio del festival no puede ser posterior a la de fin");
        }
    }

    public boolean estaEnCurso(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    // Comparten al menos un día. Del otro se usan los getters: puede ser un proxy lazy de Hibernate
    public boolean seSuperponeCon(Festival otro) {
        return !getFechaFin().isBefore(otro.getFechaInicio()) && !otro.getFechaFin().isBefore(getFechaInicio());
    }

    public boolean terminoAntesDe(LocalDate fecha) {
        return fechaFin.isBefore(fecha);
    }

    public boolean contiene(LocalDate inicio, LocalDate fin) {
        return !inicio.isBefore(fechaInicio) && !fin.isAfter(fechaFin);
    }

    // La parte de un monto del festival que corresponde a un período (ambos días incluidos): monto × días / días del festival.
    // Se usa para prorratear sueldos y canon
    public BigDecimal prorratear(BigDecimal monto, LocalDate inicio, LocalDate fin) {
        long diasPeriodo = ChronoUnit.DAYS.between(inicio, fin) + 1;
        return monto.multiply(BigDecimal.valueOf(diasPeriodo))
                .divide(BigDecimal.valueOf(getDiasDuracion()), 2, RoundingMode.HALF_UP);
    }

    // La configuración del tipo de unidad le gana a la general (sin tipo).
    // Si hay más de una se usa la primera cargada, para que el resultado no dependa del orden del Set
    public ConfiguracionCostos getConfiguracionCostos(TipoUnidad tipo) {
        Comparator<ConfiguracionCostos> primeraCargada =
                Comparator.comparing(ConfiguracionCostos::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        return configuraciones.stream()
                .filter(config -> config.getTipoUnidad() == tipo)
                .min(primeraCargada)
                .or(() -> configuraciones.stream().filter(ConfiguracionCostos::esGeneral).min(primeraCargada))
                .orElseThrow(() -> new NoEncontradoException("El festival " + nombre + " no tiene costos configurados"));
    }
}