package com.grupo14.hito2.services;

import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.repositories.AsignacionStaffRepository;
import com.grupo14.hito2.repositories.PersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Le dice a Spring Security cómo cargar un usuario por su email para el login. */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final PersonalRepository personalRepository;
    private final AsignacionStaffRepository asignacionStaffRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Personal personal = personalRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = personal.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .toList();

        // Deshabilitado: Spring Security rechaza el login aunque la contraseña sea correcta
        return User.withUsername(personal.getEmail())
                .password(personal.getPass())
                .authorities(authorities)
                .disabled(!tieneAccesoHabilitado(personal))
                .build();
    }

    // Un empleado dado de baja en todas sus unidades no puede entrar
    public boolean tieneAccesoHabilitado(Personal personal) {
        return !personal.requiereUnidadActiva()
                || asignacionStaffRepository.existsByPersonalIdAndEliminadoFalse(personal.getId());
    }
}
