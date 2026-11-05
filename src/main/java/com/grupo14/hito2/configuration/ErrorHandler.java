package com.grupo14.hito2.configuration;

import com.grupo14.hito2.exceptions.NoEncontradoException;
import com.grupo14.hito2.exceptions.ReglaDeNegocioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Errores que llegan hasta el controller sin manejar: muestran la página de error con el mensaje
 * y el código HTTP que corresponde. Los formularios siguen atrapando sus errores para mostrarlos en la misma vista.
 */
@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarNoEncontrado(NoEncontradoException e, Model model) {
        return mostrarError(model, HttpStatus.NOT_FOUND, e.getMessage());
    }

    // Solo las reglas de negocio muestran su mensaje: un error del programa sigue a la página 500 sin exponer detalles
    @ExceptionHandler(ReglaDeNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String manejarReglaDeNegocio(ReglaDeNegocioException e, Model model) {
        log.warn("Operación rechazada: {}", e.getMessage());
        return mostrarError(model, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private String mostrarError(Model model, HttpStatus status, String mensaje) {
        model.addAttribute("status", status.value());
        model.addAttribute("mensaje", mensaje);
        return "error";
    }
}
