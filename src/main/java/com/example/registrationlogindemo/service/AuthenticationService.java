package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.User;

import java.util.Set;

/**
 * Servicio para manejar operaciones relacionadas con la autenticación y el usuario actual
 */
public interface AuthenticationService {

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario autenticado o null si no hay autenticación
     */
    User getCurrentUser();
    
    /**
     * Verifica si hay un usuario autenticado actualmente
     * @return true si hay un usuario autenticado, false en caso contrario
     */
    boolean isAuthenticated();
    
    /**
     * Obtiene el nombre de usuario actual
     * @return Nombre de usuario o null si no está autenticado
     */
    String getCurrentUsername();
    
    /**
     * Verifica si el usuario autenticado tiene un rol específico
     * @param role Rol a verificar (sin el prefijo "ROLE_")
     * @return true si tiene el rol, false en caso contrario
     */
    boolean hasRole(String role);
    
    /**
     * Obtiene todos los roles del usuario actual
     * @return Conjunto de nombres de roles (sin el prefijo "ROLE_")
     */
    Set<String> getCurrentUserRoles();
}
