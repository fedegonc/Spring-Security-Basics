package com.example.registrationlogindemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Configuración opcional para el envío de correos electrónicos
 * Si no se configuran las propiedades de correo, se utilizará un sender dummy
 */
@Configuration
public class MailConfig {

    /**
     * Crea un JavaMailSender dummy que no envía correos realmente
     * Esto permite que la aplicación funcione sin configuración de correo
     */
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        // Configuración por defecto (no funcional, solo para evitar errores)
        mailSender.setHost("localhost");
        mailSender.setPort(25);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
}
