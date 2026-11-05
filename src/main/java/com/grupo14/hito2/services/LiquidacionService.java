package com.grupo14.hito2.services;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.*;
import com.grupo14.hito2.mappers.LiquidacionMapper;
import com.grupo14.hito2.repositories.EmpleadoRepository;
import com.grupo14.hito2.repositories.UnidadVentaRepository;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiquidacionService {

    private final EmpleadoRepository empleadoRepository;
    private final UnidadVentaRepository unidadVentaRepository;
    private final LiquidacionMapper liquidacionMapper;

    // Responsables y administrador no cobran sueldo: para ellos la lista queda vacía
    public List<LiquidacionDTO> obtenerRecibos(String email) {
        return empleadoRepository.findByEmail(email)
                .map(this::obtenerRecibosDelEmpleado)
                .orElseGet(List::of);
    }

    // Un recibo por cada unidad (y por lo tanto festival) donde trabajó
    public List<LiquidacionDTO> obtenerRecibosDelEmpleado(Empleado empleado) {
        return unidadVentaRepository.findByStaffId(empleado.getId()).stream()
                .map(unidad -> calcularLiquidacion(empleado, unidad))
                .toList();
    }

    // Recibo de un festival donde el empleado trabajó; de otro festival no hay recibo
    public LiquidacionDTO calcularLiquidacion(String email, long festivalId) {
        Empleado empleado = empleadoRepository.findByEmail(email)
                .orElseThrow(() -> new ReglaDeNegocioException("Solo los empleados reciben liquidación de haberes"));
        UnidadVenta unidad = unidadVentaRepository.findByStaffId(empleado.getId()).stream()
                .filter(u -> u.getFestival().getId() == festivalId)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("No trabajaste en ese festival"));
        return calcularLiquidacion(empleado, unidad);
    }

    // Liquidación de cada empleado de la unidad, en el festival de la unidad
    public List<LiquidacionDTO> obtenerLiquidacionesDeLaUnidad(UnidadVenta unidad) {
        return empleadoRepository.findByUnidadId(unidad.getId()).stream()
                .map(empleado -> calcularLiquidacion(empleado, unidad))
                .toList();
    }

    public LiquidacionDTO calcularLiquidacionDeEmpleadoDeLaUnidad(UnidadVenta unidad, long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .filter(unidad::tieneEnStaff)
                .orElseThrow(() -> new NoEncontradoException("Ese empleado no es de tu unidad"));
        return calcularLiquidacion(empleado, unidad);
    }

    // El cálculo lo hace el empleado (Empleado.liquidar); acá solo se arma el recibo
    public LiquidacionDTO calcularLiquidacion(Empleado empleado, UnidadVenta unidad) {
        return liquidacionMapper.toDTO(empleado, unidad, empleado.liquidar(unidad));
    }
}
