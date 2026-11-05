package com.grupo14.hito2.controllers;

import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PasswordController {

    private final CuentaService cuentaService;

    // Una cuenta ya activada no tiene nada que activar: vuelve a su pantalla
    @GetMapping("/cambiar-password")
    public String formulario(Principal principal) {
        if (cuentaService.buscarPorEmail(principal.getName()).isEstaActivo()) return "redirect:/dashboard";
        return "cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiar(@RequestParam String password, @RequestParam String confirmacion,
                          Principal principal, Model model) {
        if (cuentaService.buscarPorEmail(principal.getName()).isEstaActivo()) return "redirect:/dashboard";
        try {
            cuentaService.activarYCambiarPassword(principal.getName(), password, confirmacion);
            return "redirect:/dashboard";
        } catch (ReglaDeNegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "cambiar-password";
        }
    }

    @ModelAttribute("largoMinimo")
    public int largoMinimo() {
        return CuentaService.LARGO_MINIMO_PASSWORD;
    }
}