package com.grupo14.hito2.configuration;

import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.repositories.PersonalRepository;
import com.grupo14.hito2.services.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.util.Optional;

/**
 * En cada request revisa al usuario logueado:
 * - si le cambiaron el email o lo dieron de baja en todas sus unidades, cierra la sesión;
 * - si es un usuario recién creado que todavía no eligió su contraseña, lo manda a /cambiar-password.
 */
@Component
@RequiredArgsConstructor
public class CuentaActivaInterceptor implements HandlerInterceptor {

    private final PersonalRepository personalRepository;
    private final UsuarioDetailsService usuarioDetailsService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal == null) return true;

        Optional<Personal> personal = personalRepository.findByEmail(principal.getName());
        if (personal.isEmpty() || !usuarioDetailsService.tieneAccesoHabilitado(personal.get())) {
            cerrarSesion(request);
            response.sendRedirect(request.getContextPath() + "/login?sesionCerrada");
            return false;
        }
        if (!personal.get().isEstaActivo()) {
            response.sendRedirect(request.getContextPath() + "/cambiar-password");
            return false;
        }
        return true;
    }

    private void cerrarSesion(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
    }
}
