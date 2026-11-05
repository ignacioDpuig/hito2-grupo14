package com.grupo14.hito2.controllers;

import com.grupo14.hito2.dtos.LiquidacionDTO;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Controller
@RequestMapping("/empleado")
@RequiredArgsConstructor
public class EmpleadoController {

    private final CuentaService cuentaService;
    private final LiquidacionService liquidacionService;
    private final UnidadVentaService unidadVentaService;
    private final PDFService pdfService;

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        Personal personal = cuentaService.buscarPorEmail(principal.getName());
        model.addAttribute("empleado", personal);
        model.addAttribute("puedeCargarPedidos", personal.puedeCargarPedidos());
        model.addAttribute("unidadActual", unidadVentaService.obtenerUnidadEnFestivalEnCurso(personal.getId()).orElse(null));
        model.addAttribute("recibos", liquidacionService.obtenerRecibos(principal.getName()));
        return "empleado/empleadoIndex";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model, Principal principal) {
        long personalId = cuentaService.buscarPorEmail(principal.getName()).getId();
        model.addAttribute("unidades", unidadVentaService.obtenerHistorial(personalId));
        return "empleado/historial";
    }

    @GetMapping("/recibos")
    public String verRecibos(Model model, Principal principal) {
        model.addAttribute("recibos", liquidacionService.obtenerRecibos(principal.getName()));
        return "empleado/recibos";
    }

    // Solo de festivales donde trabajó: si no, ErrorHandler muestra el 404
    @GetMapping("/recibos/descargar/{festivalId}")
    public ResponseEntity<byte[]> descargarReciboPDF(@PathVariable long festivalId, Principal principal) throws IOException {
        LiquidacionDTO recibo = liquidacionService.calcularLiquidacion(principal.getName(), festivalId);
        return DescargaPdf.de(pdfService.generarReciboPDF(recibo), "recibo_" + festivalId + ".pdf");
    }
}
