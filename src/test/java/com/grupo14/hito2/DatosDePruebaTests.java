package com.grupo14.hito2;

import com.grupo14.hito2.services.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Los casos que data.sql deja listos para mostrar en la defensa. */
@SpringBootTest
@Transactional
class DatosDePruebaTests {

    @Autowired
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void laCajeraDadaDeBajaNoEntra() {
        assertThat(usuarioDetailsService.loadUserByUsername("rocio@email.com").isEnabled()).isFalse();
    }
}
