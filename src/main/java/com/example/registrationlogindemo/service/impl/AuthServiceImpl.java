package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio centralizado de autenticación que combina
 * la funcionalidad de registro, gestión de sesiones y verificación de usuarios actuales
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ValidationAndNotificationService validationAndNotificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    
    // ======= Métodos para registro y validación =======
    
    @Override
    public boolean registerUser(UserDto userDto, BindingResult result) {
        // Si ya hay errores de validación, cancelar el registro
        if (result.hasErrors()) {
            return false;
        }
        
        // Verificar credenciales únicas
        if (!validateUniqueCredentials(userDto.getEmail(), userDto.getUsername(), result)) {
            return false;
        }
        
        // Todo está bien, guardar el usuario
        userService.saveUser(userDto);
        return true;
    }
    
    @Override
    public boolean validateUniqueCredentials(String email, String username, BindingResult result) {
        boolean isValid = true;
        
        // Validar email único
        User existingUserByEmail = userService.findByEmail(email);
        if (existingUserByEmail != null) {
            result.rejectValue("email", null, "Email existente");
            isValid = false;
        }
        
        // Validar username único
        User existingUserByUsername = userService.findByUsername(username);
        if (existingUserByUsername != null) {
            result.rejectValue("username", null, "Usuario existente");
            isValid = false;
        }
        
        return isValid;
    }
    
    // ======= Métodos para manejo de sesiones y redirección =======
    
    @Override
    public ModelAndView handleSuccessfulLogin(RedirectAttributes msg, HttpSession session, 
            HttpServletRequest request, HttpServletResponse response) {
            
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario está autenticado
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userRole = userDetails.getAuthorities().toString();

            // Solo muestra el mensaje de bienvenida una vez al iniciar sesión
            if (session.getAttribute("hasLoggedIn") == null) {
                session.setAttribute("hasLoggedIn", true);
                validationAndNotificationService.addSuccessMessage(msg, "Bienvenido, " + userDetails.getUsername() + "!");
            }

            // Recupera la URL solicitada antes de iniciar sesión, si existe
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String redirectUrl = savedRequest.getRedirectUrl();

                // Verificar si la URL contiene parámetros duplicados
                if (redirectUrl.contains("?continue")) {
                    // Limpiar URL con parámetros duplicados
                    redirectUrl = redirectUrl.split("\\?continue")[0];
                }

                // Limpiar el caché de URL
                requestCache.removeRequest(request, response);

                // Redirige a la URL solicitada originalmente (limpia)
                return new ModelAndView("redirect:" + redirectUrl);
            }

            // Si no hay URL guardada, redirige al usuario según su rol
            return getRedirectionByRole(userRole);
        }

        // Si no hay autenticación válida, redirige a una página de error
        validationAndNotificationService.addErrorMessage(msg, "Error de autenticación.");
        return new ModelAndView("redirect:/error");
    }
    
    @Override
    public ModelAndView getRedirectionByRole(String userRole) {
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        User user = authenticatedUserOpt.orElse(null);
        
        // Redirigir según el rol
        switch (userRole) {
            case "[ROLE_USER]":
                return new ModelAndView("redirect:/user/welcome");
            case "[ROLE_ADMIN]":
                return new ModelAndView("redirect:/admin/inicio");
            case "[ROLE_ORGANIZATION]":
                return new ModelAndView("redirect:/org/inicio");
            default:
                return new ModelAndView("redirect:/error");
        }
    }
    
    @Override
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("pages/error");

        // Capturar código de estado y mensaje de error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        
        // Para el estado HTTP desconocido
        if (statusCode == null) {
            statusCode = 500; // Asumimos error interno del servidor como valor predeterminado
        }

        // Personalizar mensajes de error según el código de estado
        if (errorMessage == null || errorMessage.isEmpty()) {
            switch (statusCode) {
                case 403:
                    errorMessage = "No tienes permiso para acceder a esta página.";
                    break;
                case 404:
                    errorMessage = "La página que buscas no existe.";
                    break;
                case 500:
                    errorMessage = "Ha ocurrido un error interno del servidor.";
                    break;
                default:
                    errorMessage = "Ha ocurrido un error inesperado. Por favor, vuelve a intentarlo.";
            }
        }

        // Añadir los datos básicos a la vista
        mv.addObject("status", statusCode);
        mv.addObject("error", errorMessage);
        
        // Añadir información detallada de la excepción si existe
        if (exception != null) {
            mv.addObject("exception", exception.getClass().getName());
            mv.addObject("message", exception.getMessage());
            
            // En entorno de desarrollo, podemos incluir la traza completa
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            mv.addObject("trace", sw.toString());
        }

        return mv;
    }
    
    // ======= Métodos para verificar el usuario actual y sus roles =======
    
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
