package com.example.registrationlogindemo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.HandlerInterceptor;
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
        messageSource.setBasenames("language/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    // Resolver de Locale que almacena el Locale seleccionado en la sesión del usuario
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(Locale.getDefault());
        slr.setLocaleAttributeName("current.locale");
        slr.setTimeZoneAttributeName("current.timezone");
        return slr;
    }

    // Interceptor para cambiar dinámicamente el Locale basado en un parámetro de solicitud
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("language");
        return localeChangeInterceptor;
    }

    // Método para agregar los interceptores al registro de interceptores
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Agregar el interceptor de cambio de idioma
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(new CustomInterceptor()).addPathPatterns("/**");

        }

    public static class CustomInterceptor implements HandlerInterceptor {

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String currentUrl = request.getRequestURI();

            // Guardar la URL como un atributo de la solicitud para que esté disponible en las vistas
            request.setAttribute("currentUrl", currentUrl);

            // Imprimir la URL para ver en qué página estás
            System.out.println("Current URL: " + currentUrl);

            return true;
        }
    }
}
