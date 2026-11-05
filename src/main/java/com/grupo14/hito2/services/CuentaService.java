package com.grupo14.hito2.services;

import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.repositories.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Usuarios del sistema: alta con contraseña generada y mail de credenciales, activación de la cuenta
 * y que email y DNI no se repitan. No sabe nada de unidades: el staff lo maneja EmpleadoService.
 */
@Service
@RequiredArgsConstructor
public class CuentaService {

    public static final int LARGO_MINIMO_PASSWORD = 8;

    private final PersonalRepository personalRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<Personal> obtenerPersonalPorEmail(String email) {
        return personalRepository.findByEmail(email);
    }

    public Personal buscarPorEmail(String email) {
        return personalRepository.findByEmail(email)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }

    // Solo para la primera contraseña: una sesión ya activa (ej. el admin) no puede pisar la suya desde acá
    @Transactional
    public void activarYCambiarPassword(String email, String nuevaPassword, String confirmacion) {
        Personal personal = buscarPorEmail(email);
        if (personal.isEstaActivo()) {
            throw new ReglaDeNegocioException("La cuenta ya está activada");
        }
        if (nuevaPassword == null || nuevaPassword.isBlank() || nuevaPassword.length() < LARGO_MINIMO_PASSWORD) {
            throw new ReglaDeNegocioException("La contraseña tiene que tener al menos " + LARGO_MINIMO_PASSWORD + " caracteres");
        }
        if (!nuevaPassword.equals(confirmacion)) {
            throw new ReglaDeNegocioException("Las contraseñas no coinciden");
        }
        personal.setPass(passwordEncoder.encode(nuevaPassword));
        personal.setEstaActivo(true);
        personalRepository.save(personal);
    }
}
