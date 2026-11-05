package com.grupo14.hito2.services;

import com.grupo14.hito2.events.CredencialesGeneradasEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public EmailService(JavaMailSender mailSender, @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    // Solo después del commit: si el alta hace rollback, no se manda un mail con credenciales que no existen
    @TransactionalEventListener(fallbackExecution = true)
    public void alGenerarCredenciales(CredencialesGeneradasEvent evento) {
        enviarCredenciales(evento.email(), evento.nombre(), evento.password());
    }

    // Si el mail falla el alta no se deshace: el usuario ya existe y el error queda en el log
    public void enviarCredenciales(String email, String nombre, String password) {
        enviar(email, "Credenciales de acceso - Festival Gastronómico",
                "Bienvenido " + nombre + ",\n\n" +
                "Tu cuenta ha sido creada en el sistema.\n\n" +
                "Email: " + email + "\n" +
                "Contraseña: " + password + "\n\n" +
                "Para activar tu cuenta, ingresá con estos datos y elegí tu contraseña en:\n" +
                baseUrl + "/login\n\n" +
                "Saludos,\nSistema Festival Gastronómico");
    }

    private void enviar(String email, String asunto, String texto) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(email);
            mensaje.setSubject(asunto);
            mensaje.setText(texto);
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar el mail a {}: {}", email, e.getMessage());
        }
    }
}
