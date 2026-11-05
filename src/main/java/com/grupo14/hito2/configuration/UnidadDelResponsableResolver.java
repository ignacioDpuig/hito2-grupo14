package com.grupo14.hito2.configuration;

import com.grupo14.hito2.entities.UnidadVenta;
import com.grupo14.hito2.services.UnidadVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Completa los parámetros @UnidadDelResponsable con la unidad del usuario logueado.
 * El controller decide quién es el usuario; los services reciben la unidad y solo aplican las reglas.
 */
@Component
@RequiredArgsConstructor
public class UnidadDelResponsableResolver implements HandlerMethodArgumentResolver {

    private final UnidadVentaService unidadVentaService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UnidadDelResponsable.class)
                && UnidadVenta.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public UnidadVenta resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                       NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return unidadVentaService.buscarUnidadDelResponsable(webRequest.getUserPrincipal().getName());
    }
}
