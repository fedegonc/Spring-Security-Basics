package com.example.registrationlogindemo.util;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;

/**
 * Utilidad para verificación de permisos y roles
 */
public class PermissionUtils {
    
    /**
     * Verifica si un usuario tiene permisos para editar una solicitud
     */
    public static boolean hasPermissionToEdit(Solicitude solicitude, User usuario) {
        // El propietario de la solicitud puede editarla
        if (solicitude.getUser().getId() == usuario.getId()) {
            return true;
        }
        
        // Administradores y organizaciones también pueden editar
        return hasRolePermission(usuario);
    }
    
    /**
     * Verifica si un usuario tiene permisos para eliminar una solicitud
     */
    public static boolean hasPermissionToDelete(Solicitude solicitude, User usuario) {
        // El propietario de la solicitud puede eliminarla
        if (solicitude.getUser().getId() == usuario.getId()) {
            return true;
        }
        
        // Administradores también pueden eliminar
        return hasRolePermission(usuario);
    }
    
    /**
     * Verifica si un usuario tiene roles administrativos
     */
    public static boolean hasRolePermission(User usuario) {
        // Verificar si el usuario tiene roles administrativos 
        // usando directamente los roles del usuario en lugar de una consulta adicional
        return usuario.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN") || 
                                 role.getName().equals("ROLE_ORGANIZATION"));
    }
}
