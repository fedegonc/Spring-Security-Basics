package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;

    // Constructor que inyecta el servicio UserService
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Método para redirigir a la página de inicio en caso de error
    @GetMapping("/error")
    public String redirectToIndexOnError() {
        return "error";
    }

    // Método para mostrar el formulario de inicio de sesión
    @GetMapping("/login")
    public String loginForm() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // Si el usuario ya está autenticado, redirigir a la página de inicio del usuario
            return "user/welcome";
        }
        return "login";
    }

    // Método para mostrar el formulario de registro
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // Si el usuario ya está autenticado, redirigir a la página de inicio del usuario
            return "redirect:/user/welcome";
        }
        return "register";
    }

    // Método para procesar la solicitud de registro de usuario
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto user,
                               BindingResult result,
                               Model model) {
        User existing = userService.findByEmail(user.getEmail());
        if (existing != null) {
            result.rejectValue("email", null, "Ya hay un usuario con ese email");
        }
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            return "register";
        }
        try {
            userService.saveUser(user);
        } catch (Exception e) {
            result.rejectValue(null, null, "A ocurrido un error temporal, intente registrarse más tarde.");
            return "register";
        }
        return "user/welcome";
    }

    // Método para mostrar la lista de usuarios registrados
    @GetMapping("/users")
    public String listRegisteredUsers(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

}
