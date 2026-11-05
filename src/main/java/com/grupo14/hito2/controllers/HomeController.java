package com.grupo14.hito2.controllers;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CuentaService cuentaService;
    private final FestivalService festivalService;

    // Página pública con los festivales, el login y el registro de unidades
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("festivales", festivalService.obtenerTodosFestivales());
        return "index";
    }

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
