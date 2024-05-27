package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@RequestMapping("/root")
@Controller
public class RootController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    private final UserService userService;

    // Constructor que inyecta el servicio UserService
    public RootController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ModelAndView rootDashboard() {
        ModelAndView mv = new ModelAndView("root/dashboard");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        principal.toString();
        // Obtener todas las solicitudes
        mv.addObject("principal", principal);
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        // Obtener todos los usuarios
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);
        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user_form");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Obtener la lista de roles
            List<Role> listRoles = userService.listRoles();
            // Agregar el usuario y la lista de roles al modelo
            mv.addObject("user", user);
            mv.addObject("listRoles", listRoles);
        }
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/edit/{id}")
    public String editUserBanco(@ModelAttribute("user_form") @Valid User user,
                                BindingResult result, RedirectAttributes msg) {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + user.getId();
        }

        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualizar los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            userEdit.setRoles(user.getRoles());
            // Guardar los cambios en la base de datos
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró el usuario a editar.");
        }

        return "redirect:/root/dashboard";
    }



}
