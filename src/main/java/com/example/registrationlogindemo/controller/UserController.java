package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.management.relation.Role;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/welcome")
    public String welcomePage() {
        // Lógica para mostrar la página de bienvenida
        return "user/welcome";
    }

    @GetMapping("/logout")
    public String logout() {
        return "index";
    }

    @GetMapping("/profile")
    public String userProfile() {
        return "user/profile";
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user_form");

            User user = userOptional.get();

            mv.addObject("user", user); // Agregar el usuario como objeto al modelo

            return mv;

    }

    @PostMapping("/edit/{id}")
    public String editUserBanco(@ModelAttribute("user_form") @Valid User user,
                                BindingResult result, RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:user/profile/" + user.getId();
        }

        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualiza los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            userEdit.setRoles(user.getRoles());
            // Guarda los cambios en la base de datos
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró el usuario a editar.");
        }

        return "user/welcome";
    }


    @PostMapping("/save")
    public String saveUser(User user) {
        userService.save(user);

        return "redirect:/users";
    }

}
