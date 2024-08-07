package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpSession;
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

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/error")
    public ModelAndView Error() {
        return new ModelAndView("error");
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return "redirect:/user/welcome";
        }
        return "register";
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
    public String loginForm() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return "redirect:/init";
        }
        return "login";
    }

    @GetMapping("/init")
    public ModelAndView welcomePage(RedirectAttributes msg, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userrole = userDetails.getAuthorities().toString();

            if (session.getAttribute("hasLoggedIn") == null) {
                session.setAttribute("hasLoggedIn", true);
                switch (userrole) {
                    case "[ROLE_USER]":
                        msg.addFlashAttribute("success", "message.user.welcome");
                        return new ModelAndView("user/welcome");
                    case "[ROLE_COOPERATIVA]":
                        msg.addFlashAttribute("success", "message.cooperativa.welcome");
                        return new ModelAndView("cooperativa/dashboard");
                    case "[ROLE_ASOCIACION]":
                        msg.addFlashAttribute("success", "message.asociacion.welcome");
                        return new ModelAndView("asociacion/dashboard");
                    case "[ROLE_ADMIN]":
                        msg.addFlashAttribute("success", "message.admin.welcome");
                        return new ModelAndView("admin/dashboard");
                    case "[ROLE_ROOT]":
                        msg.addFlashAttribute("success", "message.root.welcome");
                        return new ModelAndView("root/dashboard");
                    default:
                        break;
                }
            } else {
                switch (userrole) {
                    case "[ROLE_USER]":
                        return new ModelAndView("user/welcome");
                    case "[ROLE_COOPERATIVA]":
                        return new ModelAndView("cooperativa/dashboard");
                    case "[ROLE_ASOCIACION]":
                        return new ModelAndView("asociacion/dashboard");
                    case "[ROLE_ADMIN]":
                        return new ModelAndView("admin/dashboard");
                    case "[ROLE_ROOT]":
                        return new ModelAndView("root/dashboard");
                    default:
                        break;
                }
            }
        }
        msg.addFlashAttribute("error", "message.auth.error");
        return new ModelAndView("error");
    }


}
