package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.RoleManagementService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio para la gestión de roles y permisos en el sistema.
 * Centraliza todas las operaciones relacionadas con la asignación y modificación de roles.
 */
@Service
public class RoleManagementServiceImpl implements RoleManagementService {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;
    
    /**
     * Crea un nuevo rol en el sistema
     * @param roleName Nombre del rol (sin el prefijo ROLE_)
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se creó correctamente, false en caso contrario
     */
    @Override
    public boolean createRole(String roleName, RedirectAttributes attributes) {
        if (roleName == null || roleName.trim().isEmpty()) {
            validationAndNotificationService.addErrorMessage(attributes, "El nombre del rol no puede estar vacío");
            return false;
        }
        
        // Normalizar el nombre del rol (mayúsculas y prefijo ROLE_)
        String normalizedRoleName = normalizeRoleName(roleName);
        
        // Verificar si el rol ya existe
        if (roleRepository.findByName(normalizedRoleName) != null) {
            validationAndNotificationService.addErrorMessage(attributes, "El rol " + roleName + " ya existe");
            return false;
        }
        
        // Crear el nuevo rol
        Role newRole = new Role();
        newRole.setName(normalizedRoleName);
        roleRepository.save(newRole);
        
        validationAndNotificationService.addSuccessMessage(attributes, "Rol " + roleName + " creado correctamente");
        return true;
    }
    
    /**
     * Asigna un rol a un usuario
     * @param user Usuario al que se asignará el rol
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se asignó correctamente, false en caso contrario
     */
    @Override
    public boolean assignRoleToUser(User user, String roleName, RedirectAttributes attributes) {
        if (user == null) {
            validationAndNotificationService.addErrorMessage(attributes, "Usuario no encontrado");
            return false;
        }
        
        if (roleName == null || roleName.trim().isEmpty()) {
            validationAndNotificationService.addErrorMessage(attributes, "El nombre del rol no puede estar vacío");
            return false;
        }
        
        // Normalizar el nombre del rol
        String normalizedRoleName = normalizeRoleName(roleName);
        
        // Buscar el rol
        Role role = roleRepository.findByName(normalizedRoleName);
        if (role == null) {
            validationAndNotificationService.addErrorMessage(attributes, "El rol " + roleName + " no existe");
            return false;
        }
        
        // Agregar el rol al usuario
        List<Role> roles = user.getRoles();
        if (roles == null) {
            roles = new ArrayList<>();
        }
        
        // Verificar si el usuario ya tiene este rol
        if (roles.stream().anyMatch(r -> r.getName().equals(normalizedRoleName))) {
            validationAndNotificationService.addInfoMessage(attributes, "El usuario ya tiene asignado el rol " + roleName);
            return true;
        }
        
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);
        
        validationAndNotificationService.addSuccessMessage(attributes, "Rol " + roleName + " asignado correctamente");
        return true;
    }
    
    /**
     * Asigna múltiples roles a un usuario (reemplaza los existentes)
     * @param user Usuario al que se asignarán los roles
     * @param roleNames Lista de nombres de roles
     * @param attributes Para mensajes de retroalimentación
     * @return true si los roles se asignaron correctamente, false en caso contrario
     */
    @Override
    public boolean setUserRoles(User user, List<String> roleNames, RedirectAttributes attributes) {
        if (user == null) {
            validationAndNotificationService.addErrorMessage(attributes, "Usuario no encontrado");
            return false;
        }
        
        if (roleNames == null || roleNames.isEmpty()) {
            validationAndNotificationService.addErrorMessage(attributes, "Debe proporcionar al menos un rol");
            return false;
        }
        
        // Crear una nueva lista de roles
        List<Role> roles = new ArrayList<>();
        
        // Obtener los roles por nombre
        for (String roleName : roleNames) {
            String normalizedRoleName = normalizeRoleName(roleName);
            Role role = roleRepository.findByName(normalizedRoleName);
            
            if (role != null) {
                roles.add(role);
            }
        }
        
        if (roles.isEmpty()) {
            validationAndNotificationService.addErrorMessage(attributes, "Ninguno de los roles especificados existe en el sistema");
            return false;
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        validationAndNotificationService.addSuccessMessage(attributes, "Roles actualizados correctamente");
        return true;
    }
    
    /**
     * Elimina un rol de un usuario
     * @param user Usuario del que se eliminará el rol
     * @param roleName Nombre del rol a eliminar
     * @param attributes Para mensajes de retroalimentación
     * @return true si el rol se eliminó correctamente, false en caso contrario
     */
    @Override
    public boolean removeRoleFromUser(User user, String roleName, RedirectAttributes attributes) {
        if (user == null) {
            validationAndNotificationService.addErrorMessage(attributes, "Usuario no encontrado");
            return false;
        }
        
        if (roleName == null || roleName.trim().isEmpty()) {
            validationAndNotificationService.addErrorMessage(attributes, "El nombre del rol no puede estar vacío");
            return false;
        }
        
        // Normalizar el nombre del rol
        String normalizedRoleName = normalizeRoleName(roleName);
        
        List<Role> roles = user.getRoles();
        if (roles == null || roles.isEmpty()) {
            validationAndNotificationService.addInfoMessage(attributes, "El usuario no tiene roles asignados");
            return false;
        }
        
        boolean roleRemoved = roles.removeIf(role -> role.getName().equals(normalizedRoleName));
        if (!roleRemoved) {
            validationAndNotificationService.addInfoMessage(attributes, "El usuario no tenía asignado el rol " + roleName);
            return false;
        }
        
        // Siempre asegurar que el usuario tenga al menos el rol ROLE_USER
        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName("ROLE_USER");
            if (defaultRole != null) {
                roles.add(defaultRole);
            }
        }
        
        user.setRoles(roles);
        userRepository.save(user);
        
        validationAndNotificationService.addSuccessMessage(attributes, "Rol " + roleName + " eliminado correctamente del usuario");
        return true;
    }
    
    /**
     * Verifica si un usuario tiene un rol específico
     * @param user Usuario a verificar
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    @Override
    public boolean userHasRole(User user, String roleName) {
        if (user == null || roleName == null || user.getRoles() == null) {
            return false;
        }
        
        String normalizedRoleName = normalizeRoleName(roleName);
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(normalizedRoleName));
    }
    
    /**
     * Obtiene todos los roles disponibles en el sistema
     * @return Lista de roles
     */
    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Obtiene todos los usuarios que tienen un rol específico
     * @param roleName Nombre del rol (con o sin el prefijo ROLE_)
     * @return Lista de usuarios con ese rol
     */
    @Override
    public List<User> getUsersByRole(String roleName) {
        String normalizedRoleName = normalizeRoleName(roleName);
        return userRepository.findByRoleName(normalizedRoleName);
    }
    
    /**
     * Normaliza un nombre de rol para asegurar consistencia
     * @param roleName Nombre del rol
     * @return Nombre de rol normalizado
     */
    @Override
    public String normalizeRoleName(String roleName) {
        if (roleName == null) {
            return "ROLE_USER";
        }
        
        String normalized = roleName.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        
        return normalized;
    }
}
