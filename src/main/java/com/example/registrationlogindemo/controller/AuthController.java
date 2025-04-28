package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.PasswordResetService;
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
    @Autowired private PasswordResetService passwordResetService;
    
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
    
    /**
     * Muestra el formulario para solicitar recuperación de contraseña
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        return "pages/guest/forgot-password";
    }
    
    /**
     * Procesa la solicitud de recuperación de contraseña
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {
        
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        // Generar código de recuperación
        String code = passwordResetService.generateResetCode(email);
        
        // Siempre mostrar el mismo mensaje por seguridad, incluso si el email no existe
        redirectAttributes.addFlashAttribute("success", 
            "Si el email existe en nuestro sistema, recibirás un código de recuperación.");
        
        return "redirect:/verify-code?email=" + email;
    }
    
    /**
     * Muestra el formulario para verificar el código de recuperación
     */
    @GetMapping("/verify-code")
    public String showVerifyCodeForm(
            @RequestParam("email") String email,
            Model model) {
        
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        model.addAttribute("email", email);
        return "pages/guest/verify-code";
    }
    
    /**
     * Procesa la verificación del código de recuperación
     */
    @PostMapping("/verify-code")
    public String processVerifyCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            RedirectAttributes redirectAttributes) {
        
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        // Validar el código
        boolean isValid = passwordResetService.validateResetCode(email, code);
        
        if (!isValid) {
            redirectAttributes.addFlashAttribute("error", 
                "El código es inválido o ha expirado. Por favor, solicita un nuevo código.");
            return "redirect:/forgot-password";
        }
        
        return "redirect:/reset-password?email=" + email + "&code=" + code;
    }
    
    /**
     * Muestra el formulario para restablecer la contraseña
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        // Validar el código nuevamente
        boolean isValid = passwordResetService.validateResetCode(email, code);
        
        if (!isValid) {
            redirectAttributes.addFlashAttribute("error", 
                "El código es inválido o ha expirado. Por favor, solicita un nuevo código.");
            return "redirect:/forgot-password";
        }
        
        model.addAttribute("email", email);
        model.addAttribute("code", code);
        return "pages/guest/reset-password";
    }
    
    /**
     * Procesa el restablecimiento de contraseña
     */
    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes) {
        
        // Guard Clause: Si el usuario ya está autenticado, redirigir al inicio
        if (isUserAuthenticated()) {
            return "redirect:/inicio";
        }
        
        // Validar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden.");
            return "redirect:/reset-password?email=" + email + "&code=" + code;
        }
        
        // Restablecer la contraseña
        boolean success = passwordResetService.resetPassword(email, code, password);
        
        if (!success) {
            redirectAttributes.addFlashAttribute("error", 
                "No se pudo restablecer la contraseña. Por favor, solicita un nuevo código.");
            return "redirect:/forgot-password";
        }
        
        redirectAttributes.addFlashAttribute("success", 
            "Tu contraseña ha sido restablecida exitosamente. Ya puedes iniciar sesión con tu nueva contraseña.");
        return "redirect:/login";
    }
}
