package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para centralizar operaciones relacionadas con autenticación y registro de usuarios.
 * Gestiona el flujo de login, registro, validación de credenciales y redirección basada en roles.
 */
public interface AuthService {
    
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
}
