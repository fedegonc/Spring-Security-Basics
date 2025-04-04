package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.NotificationService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Implementación del servicio de autenticación
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
    
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
                notificationService.addSuccessMessage(msg, "Bienvenido/a al sistema.");
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
        notificationService.addErrorMessage(msg, "Error de autenticación.");
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
                return new ModelAndView("redirect:/admin/dashboard");
            case "[ROLE_ORGANIZATION]":
                return new ModelAndView("redirect:/org/dashboard");
            default:
                return new ModelAndView("redirect:/error");
        }
    }
    
    @Override
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("guest/error");

        // Capturar código de estado y mensaje de error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        
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

        // Añadir los datos a la vista
        mv.addObject("status", statusCode);
        mv.addObject("error", errorMessage);

        return mv;
    }
}
