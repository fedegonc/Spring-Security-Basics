package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Servicio para la gestión de roles y permisos en el sistema.
 * Centraliza todas las operaciones relacionadas con la asignación y modificación de roles.
 */
public interface RoleManagementService {

    /**
     * Crea un nuevo rol en el sistema
     * @param roleName Nombre del rol (sin el prefijo ROLE_)
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se creó correctamente, false en caso contrario
     */
    boolean createRole(String roleName, RedirectAttributes attributes);
    
    /**
     * Asigna un rol a un usuario
     * @param user Usuario al que se asignará el rol
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se asignó correctamente, false en caso contrario
     */
    boolean assignRoleToUser(User user, String roleName, RedirectAttributes attributes);
    
    /**
     * Asigna múltiples roles a un usuario (reemplaza los existentes)
     * @param user Usuario al que se asignarán los roles
     * @param roleNames Lista de nombres de roles
     * @param attributes Para mensajes de retroalimentación
     * @return true si los roles se asignaron correctamente, false en caso contrario
     */
    boolean setUserRoles(User user, List<String> roleNames, RedirectAttributes attributes);
    
    /**
     * Elimina un rol de un usuario
     * @param user Usuario del que se eliminará el rol
     * @param roleName Nombre del rol a eliminar
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se eliminó correctamente, false en caso contrario
     */
    boolean removeRoleFromUser(User user, String roleName, RedirectAttributes attributes);
    
    /**
     * Verifica si un usuario tiene un rol específico
     * @param user Usuario a verificar
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    boolean userHasRole(User user, String roleName);
    
    /**
     * Obtiene todos los roles disponibles en el sistema
     * @return Lista de roles
     */
    List<Role> getAllRoles();
    
    /**
     * Obtiene todos los usuarios que tienen un rol específico
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @return Lista de usuarios con ese rol
     */
    List<User> getUsersByRole(String roleName);
    
    /**
     * Normaliza un nombre de rol para asegurar consistencia
     * @param roleName Nombre del rol
     * @return Nombre de rol normalizado
     */
    String normalizeRoleName(String roleName);
}
