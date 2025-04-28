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
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
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
            Model model) {
        
        if (isUserAuthenticated()) {
            return "redirect:/user/inicio";
        }
        
        boolean registrationSuccessful = authService.registerUser(userDto, result);
        
        if (!registrationSuccessful) {
            model.addAttribute("user", userDto);
            return "pages/guest/register";
        }
        
        return "redirect:/login?registered";
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
        
        if (!authService.isAuthenticated()) {
            validationService.addErrorMessage(msg, ERROR_AUTH);
            return new ModelAndView("redirect:/login");
        }
        
        if (authService.hasRole("ADMIN")) {
            return new ModelAndView("redirect:/admin/inicio");
        } else if (authService.hasRole("ORGANIZATION")) {
            return new ModelAndView("redirect:/org/inicio");
        } else if (authService.hasRole("USER")) {
            return new ModelAndView("redirect:/user/inicio");
        } else {
            validationService.addErrorMessage(msg, ERROR_PERMISSION);
            return new ModelAndView("redirect:/error");
        }
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
