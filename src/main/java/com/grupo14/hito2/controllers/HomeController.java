package com.grupo14.hito2.controllers;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.services.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CuentaService cuentaService;

    // Post-login dashboard — redirects by role
    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        Personal personal = cuentaService.buscarPorEmail(principal.getName());

        if (personal.tieneRol(Roles.ADMIN)) return "redirect:/admin/festivales";
        if (personal.tieneRol(Roles.RESPONSABLE)) return "redirect:/responsable/dashboard";
        return "redirect:/empleado/perfil";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
