package com.grupo14.hito2.controllers;

import com.grupo14.hito2.services.FestivalService;
import com.grupo14.hito2.services.UnidadVentaService;
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
}
