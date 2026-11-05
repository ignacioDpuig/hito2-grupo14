package com.grupo14.hito2.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfiguration implements WebMvcConfigurer {

    private final CuentaActivaInterceptor cuentaActivaInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cuentaActivaInterceptor)
                .excludePathPatterns("/cambiar-password", "/login", "/logout", "/", "/registro",
                        "/error", "/css/**", "/js/**", "/imgs/**", "/vendor/**");
    }
}
