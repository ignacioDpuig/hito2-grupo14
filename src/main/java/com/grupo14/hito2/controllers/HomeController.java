package com.grupo14.hito2.controllers;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.dtos.CrearUnidadVentaDTO;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.FestivalService;
import com.grupo14.hito2.services.UnidadVentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CuentaService cuentaService;
    private final FestivalService festivalService;
    private final UnidadVentaService unidadVentaService;

    // Página pública con los festivales, el login y el registro de unidades
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("festivales", festivalService.obtenerTodosFestivales());
        return "index";
    }

    // Registro público: el responsable carga su nombre, apellido y mail junto con los datos de su unidad
    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("registroDTO", new CrearUnidadVentaDTO());
        model.addAttribute("festivales", festivalService.obtenerFestivalesEnCursoOFuturos());
        return "registro";
    }

    // Con errores vuelve al formulario con lo cargado; si sale bien, al inicio con el aviso del mail
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroDTO") CrearUnidadVentaDTO dto, BindingResult result,
                            Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                unidadVentaService.crearUnidadVenta(dto);
                redirectAttributes.addFlashAttribute("mensaje",
                        "Unidad registrada. Enviamos una contraseña temporal a " + dto.getEmailResponsable()
                        + ": la unidad queda habilitada cuando ingresás y elegís tu contraseña.");
                return "redirect:/";
            } catch (ReglaDeNegocioException e) {
                model.addAttribute("error", "No se pudo registrar la unidad: " + e.getMessage());
            }
        }
        model.addAttribute("festivales", festivalService.obtenerFestivalesEnCursoOFuturos());
        return "registro";
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
