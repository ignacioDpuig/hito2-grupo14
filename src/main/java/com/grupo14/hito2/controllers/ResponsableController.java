package com.grupo14.hito2.controllers;

import com.grupo14.hito2.configuration.UnidadDelResponsable;
import com.grupo14.hito2.dtos.CrearEmpleadoDTO;
import com.grupo14.hito2.dtos.EditarEmpleadoDTO;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.dtos.FiltroEmpleadosDTO;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.EmpleadoService;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Todas las operaciones son sobre la unidad del responsable logueado, que resuelve @UnidadDelResponsable
@Controller
@RequestMapping("/responsable")
@RequiredArgsConstructor
public class ResponsableController {

    private final EmpleadoService empleadoService;
    private final UnidadVentaService unidadVentaService;

    // Inicio con los números de la unidad de un vistazo
    @GetMapping("/dashboard")
    public String dashboard(Model model, @UnidadDelResponsable UnidadVenta unidad) {
        model.addAttribute("unidad", unidad);
        model.addAttribute("empleadosActivos", empleadoService.obtenerEmpleados(unidad, new FiltroEmpleadosDTO()).size());
        return "responsable/responsableIndex";
    }

    // ─── Unidad ───────────────────────────────────────────────────────────────

    @GetMapping("/unidad")
    public String formularioEditarUnidad(Model model, @UnidadDelResponsable UnidadVenta unidad) {
        model.addAttribute("editarUnidadVentaDTO", unidadVentaService.obtenerUnidadParaEditar(unidad));
        return "responsable/editar-unidad";
    }

    @PostMapping("/unidad")
    public String editarUnidad(@Valid @ModelAttribute("editarUnidadVentaDTO") EditarUnidadVentaDTO dto, BindingResult result,
                               @UnidadDelResponsable UnidadVenta unidad, Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                unidadVentaService.modificarUnidad(unidad, dto);
                redirectAttributes.addFlashAttribute("mensaje", "Unidad modificada correctamente");
                return "redirect:/responsable/dashboard";
            } catch (ReglaDeNegocioException e) {
                model.addAttribute("error", "No se pudo modificar la unidad: " + e.getMessage());
            }
        }
        return "responsable/editar-unidad";
    }

    // ─── Empleados ────────────────────────────────────────────────────────────

    // Las bajas no se muestran; permanecen únicamente para conservar el historial.
    @GetMapping("/empleados")
    public String verEmpleados(@ModelAttribute("filtro") FiltroEmpleadosDTO filtro, Model model, @UnidadDelResponsable UnidadVenta unidad) {
        model.addAttribute("empleados", empleadoService.obtenerEmpleados(unidad, filtro));
        return "responsable/empleados";
    }

    @GetMapping("/empleados/crear")
    public String formularioCrearEmpleado(Model model) {
        model.addAttribute("createEmpleadoDTO", new CrearEmpleadoDTO());
        return "responsable/crear-empleado";
    }

    @PostMapping("/empleados/crear")
    public String crearEmpleado(@Valid @ModelAttribute("createEmpleadoDTO") CrearEmpleadoDTO dto, BindingResult result,
                                @UnidadDelResponsable UnidadVenta unidad, Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                empleadoService.crearEmpleado(dto, unidad);
                redirectAttributes.addFlashAttribute("mensaje",
                        "Empleado creado exitosamente. Credenciales enviadas a " + dto.getEmail());
                return "redirect:/responsable/empleados";
            } catch (ReglaDeNegocioException e) {
                model.addAttribute("error", "No se pudo crear el empleado: " + e.getMessage());
            }
        }
        return "responsable/crear-empleado";
    }

    @GetMapping("/empleados/{id}/editar")
    public String formularioEditarEmpleado(@PathVariable Long id, Model model, @UnidadDelResponsable UnidadVenta unidad) {
        model.addAttribute("empleadoId", id);
        model.addAttribute("editarEmpleadoDTO", empleadoService.obtenerEmpleadoParaEditar(id, unidad));
        return "responsable/editar-empleado";
    }

    @PostMapping("/empleados/{id}/editar")
    public String editarEmpleado(@PathVariable Long id,
                                 @Valid @ModelAttribute("editarEmpleadoDTO") EditarEmpleadoDTO dto, BindingResult result,
                                 @UnidadDelResponsable UnidadVenta unidad, Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                empleadoService.actualizarEmpleado(id, dto, unidad);
                redirectAttributes.addFlashAttribute("mensaje", "Empleado modificado correctamente");
                return "redirect:/responsable/empleados";
            } catch (ReglaDeNegocioException e) {
                model.addAttribute("error", "No se pudo modificar el empleado: " + e.getMessage());
            }
        }
        model.addAttribute("empleadoId", id);
        return "responsable/editar-empleado";
    }

    @PostMapping("/empleados/{id}/eliminar")
    public String eliminarEmpleado(@PathVariable Long id, @UnidadDelResponsable UnidadVenta unidad, RedirectAttributes redirectAttributes) {
        try {
            empleadoService.darDeBajaEmpleado(id, unidad);
            redirectAttributes.addFlashAttribute("mensaje", "Empleado dado de baja definitivamente");
        } catch (ReglaDeNegocioException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/responsable/empleados";
    }
}
