package com.grupo14.hito2.configuration;

import com.grupo14.hito2.repositories.PersonalRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Datos que usa el menú lateral en todas las vistas: la página donde está el usuario y quién es. */
@ControllerAdvice
@RequiredArgsConstructor
public class VistaComunAdvice {

    private final PersonalRepository personalRepository;

    /** Lo que el menú muestra del usuario logueado. */
    public record UsuarioVista(String nombre, String apellido, String iniciales, boolean operaCaja) {
    }

    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    @ModelAttribute("usuario")
    public UsuarioVista usuario(Principal principal) {
        if (principal == null) return null;
        return personalRepository.findByEmail(principal.getName())
                .map(p -> new UsuarioVista(p.getNombre(), p.getApellido(), iniciales(p.getNombre(), p.getApellido()),
                        p.puedeCargarPedidos()))
                .orElse(null);
    }

    private static String iniciales(String nombre, String apellido) {
        return inicial(nombre) + inicial(apellido);
    }

    private static String inicial(String texto) {
        return texto == null || texto.isBlank() ? "" : texto.substring(0, 1).toUpperCase();
    }
}
