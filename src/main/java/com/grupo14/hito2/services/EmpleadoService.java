package com.grupo14.hito2.services;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.EmpleadoDeUnidad;
import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.entities.Empleado;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.enums.TipoPersonal;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.mappers.PersonalMapper;
import com.grupo14.hito2.repositories.EmpleadoRepository;
import com.grupo14.hito2.repositories.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    // Solo devuelve empleados activos; las bajas se conservan únicamente en los registros históricos
    @Transactional(readOnly = true)
    public List<EmpleadoDeUnidad> obtenerEmpleados(UnidadVenta unidad, FiltroEmpleadosDTO filtro) {
        String texto = (filtro.getTexto() == null || filtro.getTexto().isBlank())
                ? null : "%" + filtro.getTexto().trim().toLowerCase() + "%";
        Class<? extends Empleado> tipo = (filtro.getTipoPersonal() == null || filtro.getTipoPersonal().isBlank())
                ? null : TipoPersonal.desde(filtro.getTipoPersonal()).getClase();
        return empleadoRepository.filtrar(unidad.getId(), texto, tipo);
    }
}
