package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.dtos.RankingUnidadDTO;
import com.grupo14.hito2.dtos.RecaudacionPorUnidad;
import com.grupo14.hito2.dtos.ReporteMayoresCanonDTO;
import com.grupo14.hito2.dtos.ReporteVentaDTO;
import com.grupo14.hito2.dtos.ResumenPredioDTO;
import com.grupo14.hito2.dtos.ResumenVentas;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.mappers.UnidadVentaMapper;
import com.grupo14.hito2.repositories.DetallePedidoRepository;
import com.grupo14.hito2.repositories.UnidadVentaRepository;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnidadVentaService {

    public static final int TOP_MINIMO = 1;
    public static final int TOP_MAXIMO = 20;

    private final UnidadVentaRepository unidadVentaRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CuentaService cuentaService;
    private final FestivalService festivalService;
    private final UnidadVentaMapper unidadVentaMapper;

    // Para el inicio del admin: cuántos festivales hay en curso y cuántas unidades esperan que su responsable las active
    @Transactional(readOnly = true)
    public ResumenPredioDTO obtenerResumenDelPredio() {
        LocalDate hoy = LocalDate.now();
        List<Festival> festivales = festivalService.obtenerTodosFestivales();
        List<UnidadVenta> unidades = unidadVentaRepository.findAll();
        return new ResumenPredioDTO(festivales.size(),
                festivales.stream().filter(f -> f.estaEnCurso(hoy)).count(),
                unidades.size(),
                unidades.stream().filter(u -> !u.isActiva()).count());
    }

    public List<UnidadVenta> obtenerUnidadesPorFestival(long festivalId) {
        return unidadVentaRepository.findByFestivalId(festivalId);
    }

    public Optional<UnidadVenta> obtenerUnidadPorCodigo(String codigo) {
        return unidadVentaRepository.findByCodigo(codigo);
    }

    // Si trabajó en varios festivales, la unidad del más reciente donde sigue activo
    public Optional<UnidadVenta> obtenerPorStaff(long personalId) {
        return unidadVentaRepository.findActivasByStaffId(personalId).stream().findFirst();
    }

    // Todas las unidades donde trabajó, del festival más reciente al más viejo
    public List<UnidadVenta> obtenerHistorial(long personalId) {
        return unidadVentaRepository.findByStaffId(personalId);
    }

    // Solo donde sigue activo: si lo dieron de baja en esa unidad, ya no trabaja ahí
    public Optional<UnidadVenta> obtenerUnidadEnFestivalEnCurso(long personalId) {
        LocalDate hoy = LocalDate.now();
        return unidadVentaRepository.findActivasByStaffId(personalId).stream()
                .filter(u -> u.getFestival().estaEnCurso(hoy))
                .findFirst();
    }

    public UnidadVenta buscarUnidadEnFestivalEnCurso(long personalId) {
        return obtenerUnidadEnFestivalEnCurso(personalId)
                .orElseThrow(() -> new NoEncontradoException("No tenés un festival en curso"));
    }

    public UnidadVenta buscarPorStaff(long personalId) {
        return obtenerPorStaff(personalId)
                .orElseThrow(() -> new NoEncontradoException("Empleado no tiene unidad de venta asignada"));
    }

    @Transactional
    public UnidadVenta crearUnidadVenta(CrearUnidadVentaDTO dto) {
        validarAltaDeUnidad(dto);
        UnidadVenta unidad = unidadVentaMapper.toEntity(dto);
        validarPatenteUnica(unidad);
        Festival festival = festivalService.buscarPorId(dto.getFestivalId());
        // Una unidad en un festival terminado nunca podría vender
        if (festival.terminoAntesDe(LocalDate.now())) {
            throw new ReglaDeNegocioException("El festival " + festival.getNombre() + " ya terminó: elegí uno en curso o futuro");
        }
        unidad.setFestival(festival);
        unidad.validarCanon();
        // Recién con la unidad válida se crea el usuario del responsable
        unidad.asignarResponsable(cuentaService.crearResponsable(unidadVentaMapper.toResponsableDTO(dto)));
        return unidadVentaRepository.save(unidad);
    }

    // Único lugar donde se busca la unidad de un responsable: en los controllers la resuelve @UnidadDelResponsable
    @Transactional(readOnly = true)
    public UnidadVenta buscarUnidadDelResponsable(String emailResponsable) {
        return unidadVentaRepository.findByResponsableEmail(emailResponsable)
                .orElseThrow(() -> new NoEncontradoException("No tenés una unidad de venta asignada"));
    }

    public EditarUnidadVentaDTO obtenerUnidadParaEditar(UnidadVenta unidad) {
        return unidadVentaMapper.toEditarDTO(unidad);
    }

    @Transactional
    public void modificarUnidad(UnidadVenta unidad, EditarUnidadVentaDTO dto) {
        unidadVentaMapper.actualizar(unidad, dto);
        validarPatenteUnica(unidad);
        unidad.validarCanon();
        unidadVentaRepository.save(unidad);
    }

    @Transactional(readOnly = true)
    // Solo unidades habilitadas: las pendientes de activación todavía no pueden vender
    public List<RankingUnidadDTO> obtenerRankingPorFestival(long festivalId) {
        Map<Long, BigDecimal> recaudaciones = detallePedidoRepository.sumarRecaudacionPorUnidad(festivalId).stream()
                .collect(Collectors.toMap(RecaudacionPorUnidad::unidadId, RecaudacionPorUnidad::total));

        return unidadVentaRepository.findByFestivalId(festivalId).stream()
                .filter(UnidadVenta::isActiva)
                .map(u -> unidadVentaMapper.toRanking(u, recaudaciones.getOrDefault(u.getId(), BigDecimal.ZERO)))
                .sorted(Comparator.comparing(RankingUnidadDTO::getRecaudacion).reversed())
                .toList();
    }

    // Reporte de Recaudación del enunciado: el total de un festival, sumando todas sus unidades
    @Transactional(readOnly = true)
    public ReporteVentaDTO obtenerRecaudacionDelFestival(long festivalId) {
        Festival festival = festivalService.buscarPorId(festivalId);
        ResumenVentas ventas = detallePedidoRepository.resumirVentasDelFestival(festivalId);
        return new ReporteVentaDTO(festival.getNombre(), null, ventas.recaudacion(), ventas.costos(),
                ventas.cantidadPedidos().intValue());
    }

    // Solo unidades habilitadas, igual que el ranking; cada una con los costos de su tipo
    @Transactional(readOnly = true)
    public List<ReporteMayoresCanonDTO> obtenerMayoresCanon(long festivalId, int top) {
        if (top < TOP_MINIMO || top > TOP_MAXIMO) {
            throw new ReglaDeNegocioException("La cantidad de unidades tiene que estar entre " + TOP_MINIMO + " y " + TOP_MAXIMO);
        }
        festivalService.buscarPorId(festivalId);
        return unidadVentaRepository.findByFestivalId(festivalId).stream()
                .filter(UnidadVenta::isActiva)
                .map(u -> unidadVentaMapper.toReporteCanon(u, u.calcularCanon()))
                .sorted(Comparator.comparing(ReporteMayoresCanonDTO::getCanon).reversed())
                .limit(top)
                .toList();
    }


    // Los formatos los validan @CodigoUnidad y @Patente, y los datos del tipo cada subclase; acá lo que necesita la base
    private void validarAltaDeUnidad(CrearUnidadVentaDTO dto) {
        if (unidadVentaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe una unidad con el código " + dto.getCodigo());
        }
    }

    // Dos food trucks no pueden tener la misma patente
    private void validarPatenteUnica(UnidadVenta unidad) {
        String patente = unidad.getDatosDelTipo().patente();
        if (patente != null && unidadVentaRepository.existsFoodTruckConPatente(patente, unidad.getId())) {
            throw new ReglaDeNegocioException("Ya existe un food truck con la patente " + patente);
        }
    }
}
