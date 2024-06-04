package com.example.registrationlogindemo.security;

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

        http.csrf().disable()
                // Configuración de las autorizaciones de las solicitudes HTTP

                .authorizeHttpRequests((authorize) ->

                        authorize.requestMatchers("/", "/register/**", "/register/save", "/index",
                                        "/favicon.ico", "/img/**","/login/**","/init","/imagem",
                                        "/imagem/**", "/static/css/**", "/css/styles.css","/favicon.*",
                                        "/error","/gracias","/img"
                                ).permitAll()

                                .requestMatchers("/user/**"
                                ).hasRole("USER")


                                .requestMatchers("/user/**","/admin/**"
                                ).hasRole("ADMIN")

                                .requestMatchers("/root/**").hasRole("ROOT")

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

//codigo recidual
                                /* .requestMatchers("/buscarPorNombre").permitAll()
                                .requestMatchers("/buscarPorCategoria").permitAll()
                                */