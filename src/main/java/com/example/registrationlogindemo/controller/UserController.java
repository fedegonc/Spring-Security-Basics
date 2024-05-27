package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;

    private final UserService userService;

    // Constructor que inyecta el servicio UserService
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Método para la página de bienvenida del usuario
    @GetMapping("/welcome")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView();

        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            mv.addObject("username", username);

            User usuario = userRepository.findByUsername(username);
            mv.addObject("users", usuario);
        }

        // Obtener todas las solicitudes
        List<Solicitude> solicitudes = solicitudeRepository.findAll();
        mv.addObject("solicitudes", solicitudes);

        // Establecer la vista
        mv.setViewName("user/welcome");

        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null) {
            // Verificar si el ID en la URL coincide con el ID del usuario autenticado
            if (currentUser.getId() != id) {
                // Si el usuario intenta acceder al perfil de otro usuario, redirigirlo a su propio perfil
                return new ModelAndView("redirect:/user/profile/" + currentUser.getId());
            }
        }
        // Si el ID en la URL coincide con el ID del usuario autenticado, continuar con la lógica actual del método
        Optional<User> userOptional = userRepository.findById(id);

        System.out.println("Valor del id recibido: " + id);

        ModelAndView mv = new ModelAndView("user/profile");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<Role> listRoles = userService.listRoles();
            mv.addObject("listRoles", listRoles);
            mv.addObject("user", user);
        }

        return mv;
    }


    @PostMapping("/profile/{id}")
    public String editUserBanco(@ModelAttribute("user/profile") @Valid User user,
                                BindingResult result, RedirectAttributes msg) {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:user/profile/" + user.getId();
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

        return "redirect:/user/welcome";
    }



    @GetMapping("/delet/{id}")
    public String excluirUser(@PathVariable("id") int id) {
        // Eliminar el usuario por su ID
        userRepository.deleteById((long) id);
        // Redirigir al dashboard después de la eliminación
        return "index";
    }

    // Método para cerrar sesión
    @GetMapping("/logout")
    public String logout() {
        return "index";
    }

    @GetMapping("/users")
    public String listRegisteredUsers(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

}
