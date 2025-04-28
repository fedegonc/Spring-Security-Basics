package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador para gestionar la autenticación y registro de usuarios
 */
@Controller
public class AuthController implements ErrorController {

    @Autowired private UserService userService;
    @Autowired private AuthService authService;
    @Autowired private ValidationAndNotificationService validationService;
    
    private static final String ERROR_AUTH = "Debe iniciar sesión para acceder a esta página.";
    private static final String ERROR_PERMISSION = "No tiene permisos suficientes.";

    /**
     * Maneja las solicitudes de error y las redirige a la página correspondiente
     */
    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        return authService.handleError(request);
    }

    /**
     * Muestra la página de inicio de sesión o redirige si ya está autenticado
     */
    @GetMapping("/login")
    public String login() {
        // Error First: verificar primero si el usuario está autenticado
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        // Flujo principal: mostrar la página de login
        return "pages/guest/login";
    }

    /**
     * Muestra el formulario de registro
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (isUserAuthenticated()) {
            return "redirect:/user/inicio";
        }
        
        model.addAttribute("user", new UserDto());
        return "pages/guest/register";
    }

    /**
     * Procesa el formulario de registro
     */
    @PostMapping("/register/save")
    public String registration(
            @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Guard Clause 1: Verificar si el usuario ya está autenticado
        if (isUserAuthenticated()) {
            return "redirect:/user/inicio";
        }
        
        // Guard Clause 2: Validar que la contraseña no esté vacía
        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            result.rejectValue("password", "error.user", "La contraseña no puede estar vacía");
        }
        
        // Guard Clause 3: Verificar si el registro no fue exitoso
        boolean registrationSuccessful = authService.registerUser(userDto, result);
        if (!registrationSuccessful) {
            model.addAttribute("user", userDto);
            return "pages/guest/register";
        }
        
        // Flujo principal: registro exitoso
        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Por favor, inicia sesión con tus credenciales.");
        return "redirect:/login";
    }

    /**
     * Redirige a la página correspondiente según el rol del usuario
     */
    @GetMapping("/init")
    public ModelAndView handleInitRedirect(
            RedirectAttributes msg, 
            HttpSession session,
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // Guard Clause 1: Verificar si el usuario no está autenticado
        if (!authService.isAuthenticated()) {
            validationService.addErrorMessage(msg, ERROR_AUTH);
            return new ModelAndView("redirect:/login");
        }
        
        // Guard Clause 2: Verificar rol ADMIN
        if (authService.hasRole("ADMIN")) {
            return new ModelAndView("redirect:/admin/inicio");
        }
        
        // Guard Clause 3: Verificar rol ORGANIZATION
        if (authService.hasRole("ORGANIZATION")) {
            return new ModelAndView("redirect:/org/inicio");
        }
        
        // Guard Clause 4: Verificar rol USER
        if (authService.hasRole("USER")) {
            return new ModelAndView("redirect:/user/inicio");
        }
        
        // Flujo de error: el usuario no tiene un rol reconocido
        validationService.addErrorMessage(msg, ERROR_PERMISSION);
        return new ModelAndView("redirect:/error");
    }
    
    /**
     * Verifica si el usuario actual está autenticado
     */
    private boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication instanceof AnonymousAuthenticationToken);
    }
}
