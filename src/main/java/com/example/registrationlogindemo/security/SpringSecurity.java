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
    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);

        http.csrf().disable()
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/register/**").permitAll()
                                .requestMatchers("/**").permitAll()
                                .requestMatchers("/index").permitAll()
                                .requestMatchers("/imagem/**").permitAll()
                                .requestMatchers("/buscarPorNombre").permitAll()
                                .requestMatchers("/buscarPorCategoria").permitAll()
                                .requestMatchers("/gracias").permitAll()
                                .requestMatchers("/favicon.*").permitAll()
                                .requestMatchers("/error").permitAll()

                                .requestMatchers("/modifysolicitude/**").hasRole("ADMIN")
                                .requestMatchers("/dashboard/**").hasRole("ADMIN")
                                .requestMatchers("/users/**").hasRole("ADMIN")

                                .requestMatchers("/newsolicitude/**").hasRole("ADMIN")
                                .requestMatchers("/solicitude/**").hasRole("ADMIN")
                                .requestMatchers("/editsolicitude/**").hasRole("ADMIN")
                                .requestMatchers("/deletsolicitude/**").hasRole("ADMIN")
                                .requestMatchers("/img/**").hasRole("ADMIN")
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .defaultSuccessUrl("/index")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                ).requestCache((cache) -> cache
                        .requestCache(requestCache)
                );
        return http.build();
    }
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}
