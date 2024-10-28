package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController implements ErrorController {

    private final UserService userService;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageService imageService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("guest/error"); // "error" es el nombre de la vista
        imageService.addFlagImages(mv);
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        } else {
            // Redirigir a una página de error si no hay usuario autenticado
            mv.setViewName("guest/error");
            mv.addObject("error", "No tienes acceso a esta página. Por favor, inicia sesión.");
        }
        // Capturar código de estado y mensaje de error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        // Mensaje de error amigable
        if (errorMessage == null) {
            errorMessage = "Ha ocurrido un error inesperado. Por favor, vuelve a intentarlo.";
        }

        // Añadir los detalles al ModelAndView usando addObject
        mv.addObject("status", statusCode != null ? statusCode : "Error desconocido");
        mv.addObject("error", errorMessage);

        return mv;
    }


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();

        model.addAttribute("user", user);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return "redirect:/user/welcome";
        }
        // Buscar las imágenes para los idiomas
        Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
        Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");

        // Agregar el nombre de la imagen al modelo si se encuentra
        uruguaiImage.ifPresent(image -> {
            model.addAttribute("uruguaiImageName", image.getNombre());
        });
        brasilImage.ifPresent(image -> {
            model.addAttribute("brasilImageName", image.getNombre());
        });
        return "guest/register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
        User existingUserByEmail = userService.findByEmail(userDto.getEmail());
        User existingUserByUsername = userService.findByUsername(userDto.getUsername());

        if (existingUserByEmail != null && existingUserByEmail.getEmail() != null && !existingUserByEmail.getEmail().isEmpty()) {
            result.rejectValue("email", null, "Email existente");
        }

        if (existingUserByUsername != null && existingUserByUsername.getUsername() != null && !existingUserByUsername.getUsername().isEmpty()) {
            result.rejectValue("username", null, "Usuario existente");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/register";
        }

        userService.saveUser(userDto);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public ModelAndView loginForm() {
        ModelAndView mv = new ModelAndView();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            mv.setViewName("redirect:/init");
        } else {
            mv.setViewName("guest/login"); // Establecer la vista del formulario de login
        }
        imageService.addFlagImages(mv);
        return mv;
    }

    // Método welcomePage refactorizado
    @GetMapping("/init")
    public ModelAndView welcomePage(RedirectAttributes msg, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userRole = userDetails.getAuthorities().toString();

            // Solo muestra el mensaje de bienvenida al inicio de sesión
            if (session.getAttribute("hasLoggedIn") == null) {
                session.setAttribute("hasLoggedIn", true);
                msg.addFlashAttribute("success", "message.welcome");
            }

            // Redirige al usuario según su rol
            return redirectByUserRole(userRole);
        }

        // Redirige a error si no hay autenticación válida
        msg.addFlashAttribute("error", "message.auth.error");
        return new ModelAndView("redirect:error");
    }

    // Redirige al usuario según su rol
    private ModelAndView redirectByUserRole(String userRole) {
        switch (userRole) {
            case "[ROLE_USER]":
                return new ModelAndView("redirect:user/welcome");
            case "[ROLE_COOPERATIVA]":
                return new ModelAndView("redirect:cooperativa/dashboard");
            case "[ROLE_ASOCIACION]":
                return new ModelAndView("redirect:asociacion/dashboard");
            case "[ROLE_ADMIN]":
                return new ModelAndView("redirect:admin/dashboard");
            case "[ROLE_ROOT]":
                return new ModelAndView("redirect:root/dashboard");
            default:
                return new ModelAndView("redirect:error");
        }
    }

}
