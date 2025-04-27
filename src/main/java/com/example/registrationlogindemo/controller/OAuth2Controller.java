package com.example.registrationlogindemo.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class OAuth2Controller {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @GetMapping("/init")
    public String handleInitPage(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Verificar si el usuario se autenticó con OAuth2
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            // Si no es OAuth2, simplemente redirigir según el rol
            return redirectBasedOnRole(request);
        }
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        
        // Extraer información del usuario de Google
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            // Si no hay email, no podemos procesar este usuario
            return redirectBasedOnRole(request);
        }
        
        // Buscar si el usuario ya existe
        User existingUser = userRepository.findByEmail(email);
        
        // Si el usuario no existe, crearlo
        if (existingUser == null) {
            String name = oAuth2User.getAttribute("name");
            
            // Crear un nuevo usuario
            User user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : email);
            
            // Generar un nombre de usuario basado en el email
            String username = email.substring(0, email.indexOf('@'));
            user.setUsername(username);
            
            // Asignar rol de usuario
            Role role = roleRepository.findByName("ROLE_USER");
            if (role == null) {
                // Si no existe el rol, redirigir a la página de error
                return "redirect:/error?message=Role_not_found";
            }
            
            user.setRoles(Collections.singletonList(role));
            
            // Guardar el usuario
            userRepository.save(user);
        }
        
        return redirectBasedOnRole(request);
    }
    
    // Método auxiliar para redirigir según el rol
    private String redirectBasedOnRole(HttpServletRequest request) {
        if (request.isUserInRole("ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        } else if (request.isUserInRole("ROLE_ORGANIZATION")) {
            return "redirect:/org/dashboard";
        } else {
            return "redirect:/user/inicio";
        }
    }
}
