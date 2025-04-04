package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * Servicio centralizado para todas las operaciones relacionadas con autenticación y acceso de usuarios.
 * Gestiona el flujo de login, registro, validación de credenciales, redirección basada en roles,
 * verificación del usuario actual y sus permisos.
 */
public interface AuthService {
    
    // ======= Métodos para registro y validación =======
    
    /**
     * Maneja el registro de un nuevo usuario validando datos y guardando en la base de datos
     * @param userDto Datos del usuario a registrar
     * @param result Resultado de la validación del formulario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    boolean registerUser(UserDto userDto, BindingResult result);
    
    /**
     * Valida si el email o username ya existen en el sistema
     * @param email Email a validar
     * @param username Nombre de usuario a validar
     * @param result Resultado de la validación para registrar errores
     * @return true si las credenciales son únicas, false en caso contrario
     */
    boolean validateUniqueCredentials(String email, String username, BindingResult result);
    
    // ======= Métodos para manejo de sesiones y redirección =======
    
    /**
     * Procesa el login exitoso y determina la redirección adecuada
     * @param msg Para agregar mensajes flash
     * @param session Sesión HTTP actual
     * @param request Petición HTTP actual
     * @param response Respuesta HTTP actual
     * @return ModelAndView con la redirección apropiada
     */
    ModelAndView handleSuccessfulLogin(RedirectAttributes msg, HttpSession session, 
            HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Determina la redirección adecuada según el rol del usuario
     * @param userRole Rol del usuario autenticado
     * @return ModelAndView con la redirección específica para el rol
     */
    ModelAndView getRedirectionByRole(String userRole);
    
    /**
     * Procesa errores de autenticación o acceso y crea respuestas personalizadas
     * @param request Petición HTTP con información del error
     * @return ModelAndView con la vista de error y los datos del error
     */
    ModelAndView handleError(HttpServletRequest request);
    
    // ======= Métodos para verificar el usuario actual y sus roles =======
    
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
