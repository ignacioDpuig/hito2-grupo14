package com.grupo14.hito2.controllers;

import com.grupo14.hito2.services.FestivalService;
import com.grupo14.hito2.services.UnidadVentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

// El admin solo consulta: las unidades las registra cada responsable en /registro
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FestivalService festivalService;
    private final UnidadVentaService unidadVentaService;

    @GetMapping("/festivales")
    public String verFestivales(Model model) {
        model.addAttribute("festivales", festivalService.obtenerTodosFestivales());
        model.addAttribute("resumen", unidadVentaService.obtenerResumenDelPredio());
        return "admin/adminIndex";
    }

    @GetMapping("/unidades/{festivalId}")
    public String verUnidadesPorFestival(@PathVariable long festivalId, Model model) {
        model.addAttribute("festival", festivalService.buscarPorId(festivalId));
        model.addAttribute("unidades", unidadVentaService.obtenerUnidadesPorFestival(festivalId));
        return "admin/unidades";
    }

    @GetMapping("/canon-reporte")
    public String verCanonReporte(
            @RequestParam(required = false) Long festivalId,
            @RequestParam(defaultValue = "3") int top,
            Model model) {
        model.addAttribute("festivales", festivalService.obtenerTodosFestivales());
        model.addAttribute("top", top);
        if (festivalId != null) {
            model.addAttribute("festivalSeleccionado", festivalId);
            try {
                model.addAttribute("reporte", unidadVentaService.obtenerMayoresCanon(festivalId, top));
            } catch (ReglaDeNegocioException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        return "admin/canon-reporte";
    }
}
