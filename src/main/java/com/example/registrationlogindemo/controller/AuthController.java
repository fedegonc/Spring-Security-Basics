package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AuthController {

    // Constructor de la clase AuthController
    // Se utiliza para inyectar el servicio UserService al controlador
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Redirecciona a la página de inicio en caso de error
    @GetMapping("/error")
    public String redirectToIndexOnError() {
        return "redirect:/index";
    }



    // Muestra el formulario de registro de usuario
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // El usuario ya está autenticado, redirigir a la página de inicio
            return "redirect:/user/welcome";
        }
        return "register";
    }

    // Maneja la solicitud POST de registro de usuario
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model){
        User existingUser = userService.findByEmail(userDto.getEmail());

        if(existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()){
            result.rejectValue("email", null,
                    "There is already an account registered with the same email");
        }

        if(result.hasErrors()){
            model.addAttribute("user", userDto);
            return "/register";
        }

        userService.saveUser(userDto);
        return "redirect:/login";
    }

    // Muestra el formulario de inicio de sesión
    @GetMapping("/login")
    public String loginForm() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // El usuario ya está autenticado, redirigir a la página de inicio
            return "redirect:/init";
        }
        return "login";
    }


    // Redirige a la página de bienvenida según el rol del usuario
    @GetMapping("/init")
    public ModelAndView welcomePage() {

        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userrole = userDetails.getAuthorities().toString();

            // Verificar el rol del usuario y redirigir en consecuencia
            if (userrole != null && userrole.contains("USER")) {
                return new ModelAndView("redirect:/user/welcome");
            } else if (userrole != null && userrole.contains("ADMIN")) {
                return new ModelAndView("redirect:/dashboard");
            }
        }

        // En caso de que no se pueda determinar el rol o no haya usuario autenticado, redirigir a una página de error o a otra página por defecto
        return new ModelAndView("redirect:/error");
    }
}
