package com.example.registrationlogindemo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    @Autowired
    private UserDetailsService userDetailsService;

    // Método para configurar el encriptador de contraseñas
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Método para configurar la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Configuración del almacenamiento de solicitudes en caché
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);
        requestCache.setCreateSessionAllowed(false);

        http.csrf().disable()
                // Configuración de las autorizaciones de las solicitudes HTTP

                .authorizeHttpRequests((authorize) ->

                        authorize.requestMatchers("/", "/register/**", "/register/save", "/index",
                                        "/favicon.ico","/login/**","/init/**","/static/**",
                                        "/imagem/**", "/static/css/**", "/css/**","/favicon.*",
                                        "/error","/gracias","/article/**","/img/**","/construction"
                                        ,"/sostenibilidad","/scripts/**","/ambiental","/noticias"
                                        ,"/materiales","/legislacion","/alianzas","/base","/error",
                                        "/resources/**", "/templates/fragments/**"
                                                ).permitAll()



                                .requestMatchers("/user/**","/index/**","/init/**","/report/**"
                                                ).hasRole("USER")

                                .requestMatchers("/org/**","/report/**"
                                                ).hasAnyRole("ORGANIZATION")

                                .requestMatchers("/admin/**","/report/**"
                                                ).hasRole("ADMIN")



                )
                // Configuración de inicio de sesión
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/init")
                                .permitAll()
                )
                // Configuración de cierre de sesión
                .logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessUrl("/index")
                                .permitAll()
                )
                // Configuración del almacenamiento de solicitudes en caché
                .requestCache(cache -> cache
                        .requestCache(requestCache)
                );

        return http.build();
    }

    // Método para configurar la autenticación global
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}
