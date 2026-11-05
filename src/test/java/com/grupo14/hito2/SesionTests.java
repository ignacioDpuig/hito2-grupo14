package com.grupo14.hito2;

import com.grupo14.hito2.configuration.CuentaActivaInterceptor;
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
}
