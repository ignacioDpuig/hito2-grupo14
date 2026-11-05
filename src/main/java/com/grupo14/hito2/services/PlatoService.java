package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.CrearPlatoDTO;
import com.grupo14.hito2.entities.Plato;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.repositories.PlatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Platos de una unidad. Dentro de una transacción los cambios a un plato se guardan solos (no hace falta save). */
@Service
@RequiredArgsConstructor
public class PlatoService {

    private final PlatoRepository platoRepository;

    // Todos los platos (activos y dados de baja) para la vista de gestión
    @Transactional(readOnly = true)
    public List<Plato> obtenerTodosLosPlatosPorUnidad(Long unidadId) {
        return platoRepository.findByUnidadVentaId(unidadId);
    }

    // Solo los activos (ej. para armar un pedido)
    @Transactional(readOnly = true)
    public List<Plato> obtenerPlatosActivosPorUnidad(Long unidadId) {
        return platoRepository.findByUnidadVentaId(unidadId).stream()
                .filter(p -> !p.isEliminado())
                .toList();
    }

    @Transactional
    public Plato crearPlato(CrearPlatoDTO dto, UnidadVenta unidad) {
        validarPrecioNoMenorAlCosto(dto);
        String nombre = dto.getNombre().trim();
        validarNombreUnico(nombre, unidad, null);
        return platoRepository.save(Plato.nuevo(nombre, dto.getCostoProduccion(), dto.getPrecioVenta(), unidad));
    }

    // Datos actuales del plato para precargar el formulario de edición
    @Transactional(readOnly = true)
    public CrearPlatoDTO obtenerPlatoParaEditar(Long id, UnidadVenta unidad) {
        Plato plato = buscarPlatoDeLaUnidad(id, unidad);
        return new CrearPlatoDTO(plato.getNombre(), plato.getCostoProduccion(), plato.getPrecioVenta());
    }

    @Transactional
    public void actualizarPlato(Long id, UnidadVenta unidad, CrearPlatoDTO dto) {
        Plato plato = buscarPlatoDeLaUnidad(id, unidad);
        validarPrecioNoMenorAlCosto(dto);
        String nombre = dto.getNombre().trim();
        validarNombreUnico(nombre, unidad, id);
        plato.actualizar(nombre, dto.getCostoProduccion(), dto.getPrecioVenta());
    }

    @Transactional
    public void darDeBajaPlato(Long id, UnidadVenta unidad) {
        buscarPlatoDeLaUnidad(id, unidad).darDeBaja();
    }

    @Transactional
    public void reactivarPlato(Long id, UnidadVenta unidad) {
        buscarPlatoDeLaUnidad(id, unidad).reactivar();
    }

    // Un responsable solo puede tocar los platos de su propia unidad
    private Plato buscarPlatoDeLaUnidad(Long id, UnidadVenta unidad) {
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("Plato no encontrado"));
        if (!unidad.ofrece(plato)) {
            throw new ReglaDeNegocioException("No tenés permiso para modificar este plato");
        }
        return plato;
    }

    // Sin contar al propio plato (platoId null en un alta). Si el repetido está dado de baja, conviene reactivarlo
    private void validarNombreUnico(String nombre, UnidadVenta unidad, Long platoId) {
        platoRepository.findFirstByUnidadVentaIdAndNombreIgnoreCase(unidad.getId(), nombre)
                .filter(otro -> !otro.getId().equals(platoId))
                .ifPresent(otro -> {
                    throw new ReglaDeNegocioException("Ya tenés un plato llamado " + otro.getNombre()
                            + (otro.isEliminado() ? " (dado de baja: podés reactivarlo)" : ""));
                });
    }

    private void validarPrecioNoMenorAlCosto(CrearPlatoDTO dto) {
        if (dto.getCostoProduccion() != null && dto.getPrecioVenta() != null
                && dto.getPrecioVenta().compareTo(dto.getCostoProduccion()) < 0) {
            throw new ReglaDeNegocioException("El precio de venta no puede ser menor al costo de producción");
        }
    }
}
