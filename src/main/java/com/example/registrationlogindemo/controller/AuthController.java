package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
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

@Controller
public class AuthController implements ErrorController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        // Delegamos toda la lógica de manejo de errores al AuthService
        return authService.handleError(request);
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        
        // Verificar si el usuario ya está autenticado
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return "redirect:/user/welcome";
        }
        
        return "guest/register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
        // Delegamos la lógica de registro al AuthService
        boolean registrationSuccessful = authService.registerUser(userDto, result);
        
        if (!registrationSuccessful) {
            model.addAttribute("user", userDto);
            return "guest/register";
        }
        
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "guest/login";
    }

    @GetMapping("/init")
    public ModelAndView welcomePage(RedirectAttributes msg, HttpSession session, 
                                   HttpServletRequest request, HttpServletResponse response) {
        // Delegamos toda la lógica de redirección post-login al AuthService
        return authService.handleSuccessfulLogin(msg, session, request, response);
    }
}
