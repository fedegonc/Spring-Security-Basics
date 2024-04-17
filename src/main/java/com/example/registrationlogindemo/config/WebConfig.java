package com.example.registrationlogindemo.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Bean para cargar los mensajes en diferentes idiomas
    @Bean("messageSource")
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        // Ubicación de los archivos de mensajes
        messageSource.setBasenames("language/messages");
        // Codificación de los archivos de mensajes
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    // Resolver de Locale que almacena el Locale seleccionado en la sesión del usuario
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        // Establece el Locale predeterminado en el Locale del sistema
        slr.setDefaultLocale(Locale.getDefault());
        // Atributos de sesión para el Locale y la zona horaria
        slr.setLocaleAttributeName("current.locale");
        slr.setTimeZoneAttributeName("current.timezone");
        return slr;
    }

    // Interceptor para cambiar dinámicamente el Locale basado en un parámetro de solicitud
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        // Nombre del parámetro que indica el idioma seleccionado
        localeChangeInterceptor.setParamName("language");
        return localeChangeInterceptor;
    }

    // Método para agregar el interceptor al registro de interceptores
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
