package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Organization;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@Controller
public class AuthController implements ErrorController {

    @Autowired
    UserService userService;
    private final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();

    @GetMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("guest/error"); // "error" es el nombre de la vista

        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario); // Solo se agrega si el usuario está autenticado
            mv.addObject("isAuthenticatedUser", true); // Agregar la variable solo si el usuario está autenticado
        } else {
            // Usuario no autenticado: agregar mensaje de error
            mv.addObject("error", "No tienes acceso a esta página. Por favor, inicia sesión.");
            mv.addObject("isAuthenticatedUser", false); // Se asegura de que isAuthenticatedUser sea false
        }

        // Capturar código de estado y mensaje de error
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");

        // Mensaje de error amigable
        errorMessage = (errorMessage != null) ? errorMessage : "Ha ocurrido un error inesperado. Por favor, vuelve a intentarlo.";
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
        return "guest/register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
        User existingUserByEmail = userService.findByEmail(userDto.getEmail());
        User existingUserByUsername = userService.findByUsername(userDto.getUsername());

        if (existingUserByEmail != null) {
            result.rejectValue("email", null, "Email existente");
        }

        if (existingUserByUsername != null) {
            result.rejectValue("username", null, "Usuario existente");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "/guest/register";
        }

        userService.saveUser(userDto);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "guest/login";
    }

    @GetMapping("/init")
    public ModelAndView welcomePage(RedirectAttributes msg, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verifica si el usuario está autenticado
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userRole = userDetails.getAuthorities().toString();

            // Solo muestra el mensaje de bienvenida una vez al iniciar sesión
            if (session.getAttribute("hasLoggedIn") == null) {
                session.setAttribute("hasLoggedIn", true);
                msg.addFlashAttribute("success", "Bienvenido/a al sistema.");
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
            return redirectByUserRole(userRole);
        }

        // Si no hay autenticación válida, redirige a una página de error
        msg.addFlashAttribute("error", "Error de autenticación.");
        return new ModelAndView("redirect:/error");
    }

    // Redirige al usuario a la vista correspondiente según su rol
    private ModelAndView redirectByUserRole(String userRole) {
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        User user = authenticatedUserOpt.orElse(null);
        
        // Si el usuario tiene organizaciones propias O tiene un tipo de organización, 
        // redirigir al dashboard de organizaciones
        if (user != null && 
            (!user.getOwnedOrganizations().isEmpty() || user.getOrganizationType() != null)) {
            return new ModelAndView("redirect:/user/org/dashboard");
        }
        
        // De lo contrario, redirigir según el rol
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
}
