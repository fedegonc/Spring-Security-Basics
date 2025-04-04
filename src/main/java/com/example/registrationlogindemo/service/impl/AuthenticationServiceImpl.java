package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para manejar operaciones relacionadas con la autenticación y el usuario actual
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario autenticado o null si no hay autenticación
     */
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();
            return userRepository.findByUsername(username);
        }
        return null;
    }
    
    /**
     * Verifica si hay un usuario autenticado actualmente
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication instanceof AnonymousAuthenticationToken);
    }
    
    /**
     * Obtiene el nombre de usuario actual
     * @return Nombre de usuario o null si no está autenticado
     */
    @Override
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        return null;
    }
    
    /**
     * Verifica si el usuario autenticado tiene un rol específico
     * @param role Rol a verificar (sin el prefijo "ROLE_")
     * @return true si tiene el rol, false en caso contrario
     */
    @Override
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String prefixedRole = "ROLE_" + role.toUpperCase();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(prefixedRole));
    }
    
    /**
     * Obtiene todos los roles del usuario actual
     * @return Conjunto de nombres de roles (sin el prefijo "ROLE_")
     */
    @Override
    public Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .map(authority -> {
                    String role = authority.getAuthority();
                    // Quitar el prefijo "ROLE_" si existe
                    return role.startsWith("ROLE_") ? role.substring(5) : role;
                })
                .collect(Collectors.toSet());
    }
}
