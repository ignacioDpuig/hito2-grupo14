package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.PlatoDTO;
import com.grupo14.hito2.dtos.RentabilidadDTO;
import com.grupo14.hito2.dtos.ReporteVentaDTO;
import com.grupo14.hito2.dtos.RentabilidadEntreFechasDTO;
import com.grupo14.hito2.dtos.ReportesUnidadDTO;
import com.grupo14.hito2.dtos.ResumenVentas;
import com.grupo14.hito2.entities.Festival;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.repositories.DetallePedidoRepository;
import com.grupo14.hito2.repositories.EmpleadoRepository;
import com.grupo14.hito2.repositories.PlatoRepository;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/** Reportes del responsable: recaudación, plato estrella y rentabilidad neta de su unidad. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteUnidadService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PlatoRepository platoRepository;
    private final EmpleadoRepository empleadoRepository;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cada unidad pertenece a un solo festival: los reportes son siempre sobre ese
    public ReportesUnidadDTO generarReportes(UnidadVenta unidad) {
        Festival festival = unidad.getFestival();
        ResumenVentas ventas = detallePedidoRepository.resumirVentas(unidad.getId(), festival.getId());

        return new ReportesUnidadDTO(
                unidad.getNombreComercial(),
                festival.getNombre(),
                festival.getFechaInicio(),
                festival.getFechaFin(),
                generarReporteVenta(unidad, festival, ventas),
                buscarPlatoEstrella(unidad, festival),
                calcularRentabilidadNeta(unidad, festival, ventas));
    }

    // Ventas y costo de platos son los del período; sueldos y canon se prorratean por los días que cubre
    public RentabilidadEntreFechasDTO calcularRentabilidadEntreFechas(UnidadVenta unidad, LocalDate inicio, LocalDate fin) {
        if (inicio.isAfter(fin)) {
            throw new ReglaDeNegocioException("La fecha de inicio no puede ser posterior a la de fin");
        }
        Festival festival = unidad.getFestival();
        if (!festival.contiene(inicio, fin)) {
            throw new ReglaDeNegocioException("Las fechas tienen que estar dentro del festival, del "
                    + festival.getFechaInicio().format(FECHA) + " al " + festival.getFechaFin().format(FECHA));
        }

        ResumenVentas ventas = detallePedidoRepository.resumirVentasEntreFechas(unidad.getId(), inicio, fin);
        BigDecimal sueldos = festival.prorratear(calcularSueldos(unidad), inicio, fin);
        BigDecimal canon = festival.prorratear(unidad.calcularCanon(), inicio, fin);

        return new RentabilidadEntreFechasDTO(
                festival.getNombre(),
                unidad.getNombreComercial(),
                inicio,
                fin,
                ChronoUnit.DAYS.between(inicio, fin) + 1,
                festival.getDiasDuracion(),
                ventas.recaudacion(),
                ventas.costos(),
                sueldos,
                canon);
    }

    private ReporteVentaDTO generarReporteVenta(UnidadVenta unidad, Festival festival, ResumenVentas ventas) {
        return new ReporteVentaDTO(
                festival.getNombre(),
                unidad.getNombreComercial(),
                ventas.recaudacion(),
                ventas.costos(),
                ventas.cantidadPedidos().intValue());
    }

    private PlatoDTO buscarPlatoEstrella(UnidadVenta unidad, Festival festival) {
        return platoRepository.findPlatoEstrella(unidad.getId(), festival.getId()).orElse(null);
    }

    // La resta la hace RentabilidadDTO.getRentabilidadNeta: acá solo se juntan los cuatro números
    private RentabilidadDTO calcularRentabilidadNeta(UnidadVenta unidad, Festival festival, ResumenVentas ventas) {
        BigDecimal sueldos = calcularSueldos(unidad);
        BigDecimal canon = unidad.calcularCanon();

        return new RentabilidadDTO(
                festival.getNombre(),
                unidad.getNombreComercial(),
                ventas.recaudacion(),
                ventas.costos(),
                sueldos,
                canon);
    }

    // Lo que cobra en el festival todo el staff que es empleado (el responsable no cobra sueldo)
    private BigDecimal calcularSueldos(UnidadVenta unidad) {
        return empleadoRepository.findByUnidadId(unidad.getId()).stream()
                .map(empleado -> empleado.liquidar(unidad).total())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
