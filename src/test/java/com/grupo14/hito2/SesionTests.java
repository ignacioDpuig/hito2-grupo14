package com.grupo14.hito2;

import com.grupo14.hito2.configuration.CuentaActivaInterceptor;
import com.grupo14.hito2.dtos.EditarEmpleadoDTO;
import com.grupo14.hito2.services.CuentaService;
import com.grupo14.hito2.services.EmpleadoService;
import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.UnidadVentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** El interceptor corta la sesión de quien ya no puede operar. */
@SpringBootTest
@Transactional
class SesionTests {

    @Autowired
    private CuentaActivaInterceptor interceptor;
    @Autowired
    private CuentaService cuentaService;
    @Autowired
    private EmpleadoService empleadoService;
    @Autowired
    private UnidadVentaService unidadVentaService;

    private MockHttpServletResponse request(String email) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/empleado/perfil");
        request.setUserPrincipal(() -> email);
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        interceptor.preHandle(request, response, null);
        return response;
    }

    @Test
    void unUsuarioHabilitadoSigue() throws Exception {
        assertThat(request("cajero@cajero").getRedirectedUrl()).isNull();
    }

    @Test
    void siLoDanDeBajaSeCierraLaSesion() throws Exception {
        long cajeroId = cuentaService.buscarPorEmail("cajero@cajero").getId();
        empleadoService.darDeBajaEmpleado(cajeroId, unidadDe("sofia@email.com"));

        assertThat(request("cajero@cajero").getRedirectedUrl()).isEqualTo("/login?sesionCerrada");
    }

    @Test
    void siLeCambianElEmailSeCierraLaSesion() throws Exception {
        long cajeroId = cuentaService.buscarPorEmail("cajero@cajero").getId();
        EditarEmpleadoDTO dto = empleadoService.obtenerEmpleadoParaEditar(cajeroId, unidadDe("sofia@email.com"));
        dto.setEmail("lucas.nuevo@email.com");
        empleadoService.actualizarEmpleado(cajeroId, dto, unidadDe("sofia@email.com"));

        assertThat(request("cajero@cajero").getRedirectedUrl()).isEqualTo("/login?sesionCerrada");
        assertThat(request("lucas.nuevo@email.com").getRedirectedUrl()).isNull();
    }

    private UnidadVenta unidadDe(String emailResponsable) {
        return unidadVentaService.buscarUnidadDelResponsable(emailResponsable);
    }
}
