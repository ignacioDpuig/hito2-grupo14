package com.grupo14.hito2;

import com.grupo14.hito2.entities.Personal;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La primera contraseña que elige un usuario nuevo. */
@SpringBootTest
@Transactional
class ActivacionCuentaTests {

    @Autowired
    private CuentaService cuentaService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // El cajero de los datos de prueba, como si recién lo hubieran dado de alta
    @BeforeEach
    void cuentaSinActivar() {
        cuentaService.buscarPorEmail("cajero@cajero").setEstaActivo(false);
    }

    @Test
    void laContraseñaTieneQueTenerAlMenos8Caracteres() {
        assertThatThrownBy(() -> cuentaService.activarYCambiarPassword("cajero@cajero", "corta", "corta"))
                .hasMessageContaining("al menos 8");
        assertThatThrownBy(() -> cuentaService.activarYCambiarPassword("cajero@cajero", "        ", "        "))
                .hasMessageContaining("al menos 8");
    }

    @Test
    void laConfirmacionTieneQueCoincidir() {
        assertThatThrownBy(() -> cuentaService.activarYCambiarPassword("cajero@cajero", "segura123", "segura124"))
                .hasMessageContaining("no coinciden");
    }

    @Test
    void conUnaContraseñaValidaLaCuentaQuedaActiva() {
        cuentaService.activarYCambiarPassword("cajero@cajero", "segura123", "segura123");

        Personal cajero = cuentaService.buscarPorEmail("cajero@cajero");
        assertThat(cajero.isEstaActivo()).isTrue();
        assertThat(passwordEncoder.matches("segura123", cajero.getPass())).isTrue();
        assertThatThrownBy(() -> cuentaService.activarYCambiarPassword("cajero@cajero", "otra12345", "otra12345"))
                .isInstanceOf(ReglaDeNegocioException.class);
    }
}
