package com.grupo14.hito2.services;

import com.grupo14.hito2.configuration.Roles;
import com.grupo14.hito2.dtos.CrearResponsableDTO;
import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.entities.Responsable;
import com.grupo14.hito2.entities.Rol;
import com.grupo14.hito2.events.CredencialesGeneradasEvent;
import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import com.grupo14.hito2.mappers.PersonalMapper;
import com.grupo14.hito2.repositories.PersonalRepository;
import com.grupo14.hito2.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Usuarios del sistema: alta con contraseña generada y mail de credenciales, activación de la cuenta
 * y que email y DNI no se repitan. No sabe nada de unidades: el staff lo maneja EmpleadoService.
 */
@Service
@RequiredArgsConstructor
public class CuentaService {

    public static final int LARGO_MINIMO_PASSWORD = 8;

    private final PersonalRepository personalRepository;
    private final RolRepository rolRepository;
    private final PersonalMapper personalMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public Optional<Personal> obtenerPersonalPorEmail(String email) {
        return personalRepository.findByEmail(email);
    }

    public Personal buscarPorEmail(String email) {
        return personalRepository.findByEmail(email)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }

    @Transactional
    public Responsable crearResponsable(CrearResponsableDTO dto) {
        return darDeAlta(personalMapper.toResponsable(dto), Roles.RESPONSABLE, Roles.EMPLEADO);
    }

    // Común a todas las altas: contraseña aleatoria, inactivo hasta que la cambie, roles y mail con credenciales.
    // Así se verifica que el email es de la persona: sin activar la cuenta no puede operar (ni su unidad, si es responsable)
    @Transactional
    public <T extends Personal> T darDeAlta(T personal, String... nombresRoles) {
        validarEmailLibre(personal.getEmail(), null);
        if (personal.getDni() != null) validarDniLibre(personal.getDni(), null);
        String passwordGenerada = generarPassword();
        personal.setPass(passwordEncoder.encode(passwordGenerada));
        personal.setEstaActivo(false);
        personal.setRoles(buscarRoles(nombresRoles));

        T guardado = personalRepository.save(personal);
        // La contraseña va en texto plano solo en el mail; en la base queda encriptada
        eventPublisher.publishEvent(
                new CredencialesGeneradasEvent(guardado.getEmail(), guardado.getNombre(), passwordGenerada));
        return guardado;
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

    // El email es el usuario del login y el DNI identifica a la persona: no pueden ser de otra.
    // idPropio es la persona que se modifica (null en un alta), que sí puede conservar los suyos
    public void validarEmailLibre(String email, Long idPropio) {
        if (personalRepository.findByEmail(email).filter(otro -> !otro.getId().equals(idPropio)).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe un usuario con el email " + email);
        }
    }

    public void validarDniLibre(Long dni, Long idPropio) {
        if (personalRepository.findByDni(dni).filter(otro -> !otro.getId().equals(idPropio)).isPresent()) {
            throw new ReglaDeNegocioException("Ya existe una persona con el DNI " + dni);
        }
    }

    private Set<Rol> buscarRoles(String... nombresRoles) {
        return Arrays.stream(nombresRoles)
                .map(nombre -> rolRepository.findByNombre(nombre)
                        .orElseThrow(() -> new NoEncontradoException("Rol " + nombre + " no encontrado")))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private String generarPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
