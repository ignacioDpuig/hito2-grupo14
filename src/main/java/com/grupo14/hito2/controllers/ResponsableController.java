package com.grupo14.hito2.controllers;

import com.grupo14.hito2.configuration.UnidadDelResponsable;
import com.grupo14.hito2.dtos.EditarUnidadVentaDTO;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Todas las operaciones son sobre la unidad del responsable logueado, que resuelve @UnidadDelResponsable
@Controller
@RequestMapping("/responsable")
@RequiredArgsConstructor
public class ResponsableController {

    private final UnidadVentaService unidadVentaService;

    // Inicio con los números de la unidad de un vistazo
    @GetMapping("/dashboard")
    public String dashboard(Model model, @UnidadDelResponsable UnidadVenta unidad) {
        model.addAttribute("unidad", unidad);
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
}
