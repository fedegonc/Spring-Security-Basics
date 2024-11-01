package com.example.registrationlogindemo.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserStatusService {

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String); // Excluye "anonymousUser"
    }

    public String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isAuthenticated()) {
            return authentication.getName(); // Devuelve el nombre de usuario
        }
        return null; // Usuario no autenticado
    }
}
