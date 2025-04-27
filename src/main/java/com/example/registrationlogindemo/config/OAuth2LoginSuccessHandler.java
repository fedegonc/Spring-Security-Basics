package com.example.registrationlogindemo.config;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        // Verificar si la autenticación es de tipo OAuth2
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            // Si no es OAuth2, continuar con el flujo normal
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        
        // Extraer información del usuario de Google
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            // Si no hay email, no podemos procesar este usuario
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        
        // Buscar si el usuario ya existe
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            // Usuario ya existe, continuar con el flujo normal
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        
        // Crear un nuevo usuario
        String name = oAuth2User.getAttribute("name");
        User user = new User();
        user.setEmail(email);
        user.setName(name != null ? name : email);
        
        // Generar un nombre de usuario basado en el email
        String username = email.substring(0, email.indexOf('@'));
        user.setUsername(username);
        
        // Asignar rol de usuario
        Role role = roleRepository.findByName("ROLE_USER");
        if (role == null) {
            // Si no existe el rol, continuar con el flujo normal
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        
        user.setRoles(Collections.singletonList(role));
        
        // Guardar el usuario
        userRepository.save(user);
        
        // Continuar con el manejo de éxito predeterminado
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
