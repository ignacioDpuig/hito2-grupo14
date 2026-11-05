package com.grupo14.hito2.services;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.EditarEmpleadoDTO;
import com.grupo14.hito2.dtos.EmpleadoDeUnidad;
import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.enums.TipoPersonal;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.mappers.PersonalMapper;
import com.grupo14.hito2.repositories.EmpleadoRepository;
import com.grupo14.hito2.repositories.PersonalRepository;
import com.grupo14.hito2.repositories.UnidadVentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * El staff de una unidad: alta, baja definitiva, edición y filtros.
 * Crear el usuario (contraseña, roles, mail) lo delega en CuentaService.
 */
@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final CuentaService cuentaService;
    private final PersonalRepository personalRepository;
    private final EmpleadoRepository empleadoRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final PersonalMapper personalMapper;

    // El empleado siempre queda en la unidad del responsable que lo da de alta.
    // Los DNI registrados no se pueden volver a usar, incluso si el empleado fue dado de baja.
    @Transactional
    public Empleado crearEmpleado(CrearEmpleadoDTO dto, UnidadVenta unidad) {
        Empleado nuevo = personalMapper.toEmpleado(dto);
        Optional<Personal> conEseDni = personalRepository.findByDni(dto.getDni());
        if (conEseDni.isPresent()) {
            // Se consulta al repositorio de empleados en lugar de preguntar por la clase
            String motivo = empleadoRepository.existsById(conEseDni.get().getId())
                    ? "ya está registrado y no se puede volver a incorporar"
                    : "ya está registrado por otra persona";
            throw new ReglaDeNegocioException("El DNI " + dto.getDni() + " " + motivo);
        }
        Empleado empleado = cuentaService.darDeAlta(nuevo, Roles.EMPLEADO);
        unidad.agregarAlStaff(empleado);
        return empleado;
    }

    // Baja definitiva del acceso operativo: se conservan las asignaciones para el historial
    @Transactional
    public void darDeBajaEmpleado(long id, UnidadVenta unidad) {
        Empleado empleado = buscarEmpleadoDeLaUnidad(id, unidad);
        unidadVentaRepository.findByStaffId(id).forEach(unidadConEmpleado ->
                unidadConEmpleado.darDeBajaDelStaff(empleado));
        empleado.setDni(-empleado.getId());
        empleado.setEmail("baja-" + empleado.getId() + "-" + UUID.randomUUID() + "@bajas.invalid");
    }

    // Solo devuelve empleados activos; las bajas se conservan únicamente en los registros históricos
    @Transactional(readOnly = true)
    public List<EmpleadoDeUnidad> obtenerEmpleados(UnidadVenta unidad, FiltroEmpleadosDTO filtro) {
        String texto = (filtro.getTexto() == null || filtro.getTexto().isBlank())
                ? null : "%" + filtro.getTexto().trim().toLowerCase() + "%";
        Class<? extends Empleado> tipo = (filtro.getTipoPersonal() == null || filtro.getTipoPersonal().isBlank())
                ? null : TipoPersonal.desde(filtro.getTipoPersonal()).getClase();
        return empleadoRepository.filtrar(unidad.getId(), texto, tipo);
    }

    @Transactional(readOnly = true)
    public EditarEmpleadoDTO obtenerEmpleadoParaEditar(long id, UnidadVenta unidad) {
        return personalMapper.toEditarDTO(buscarEmpleadoDeLaUnidad(id, unidad));
    }

    // Modificación: solo empleados de la propia unidad; email y DNI no pueden quedar repetidos con otra persona
    @Transactional
    public Empleado actualizarEmpleado(long id, EditarEmpleadoDTO dto, UnidadVenta unidad) {
        Empleado empleado = buscarEmpleadoDeLaUnidad(id, unidad);
        cuentaService.validarEmailLibre(dto.getEmail(), empleado.getId());
        cuentaService.validarDniLibre(dto.getDni(), empleado.getId());
        personalMapper.actualizar(empleado, dto);
        return empleado;
    }

    // Busca solo entre empleados: un responsable o el administrador no se encuentran y no se pueden dar de baja
    private Empleado buscarEmpleadoDeLaUnidad(long empleadoId, UnidadVenta unidad) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new NoEncontradoException("Empleado no encontrado"));
        if (!unidad.tieneActivoEnStaff(empleado)) {
            throw new ReglaDeNegocioException("No tenés permiso para modificar este empleado");
        }
        return empleado;
    }

}
