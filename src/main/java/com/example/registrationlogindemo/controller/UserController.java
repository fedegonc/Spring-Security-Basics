package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        String username = null;
        String userrole = null;
        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
            // Puedes agregar más lógica aquí para trabajar con los detalles del usuario según tus necesidades
            mv.addObject("user", username);

        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        principal.toString();
        // Obtener todas las solicitudes
        mv.addObject("principal", principal);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        username = userDetails.getUsername();
        userrole = userDetails.getAuthorities().toString();
        // Verificar si el usuario tiene el rol de ADMIN
        if (userrole != null && userrole.contains("ADMIN")) {
            return new ModelAndView("dashboard");
        }
        // Si el usuario no es un administrador, continuar con la lógica original
        User usuario = userRepository.findByUsername(username);
        mv.addObject("users", usuario);
        // Establecer la vista
        mv.setViewName("user/welcome");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);

        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null) {
            // Verificar si la ID en la URL coincide con la ID del usuario autenticado
            if (currentUser.getId() != id) {
                // Si el usuario intenta acceder al perfil de otro usuario, redirigirlo a su propio perfil
                return new ModelAndView("redirect:/user/profile/" + currentUser.getId());
            }
        }
        // Si la ID en la URL coincide con la ID del usuario autenticado, continuar con la lógica actual del método
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

    @GetMapping("/newsolicitude")
    public ModelAndView newSolicitude() {
        ModelAndView mv = new ModelAndView("solicitude/newsolicitude");
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String newSolicitudePost(@Valid Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen,
                                    @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/user/newsolicitude";
        }

        // Verificar si se ha seleccionado un archivo y si es una imagen
        if (imagen.isEmpty() || !isImage(imagen)) {
            msg.addFlashAttribute("error", "Por favor, seleccione un archivo de imagen válido.");
            return "redirect:/user/newsolicitude";
        }

        try {
            byte[] bytes = imagen.getBytes();
            Path caminho = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
            Files.write(caminho, bytes);
            solicitud.setImagen(imagen.getOriginalFilename());
        } catch (IOException e) {
            System.out.println("Error al salvar imagen");
        }

        User user = userRepository.findByUsername(currentUser.getUsername());

        if (user != null) {
            solicitud.setUser(user);
            solicitudeRepository.save(solicitud);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
            return "redirect:/user/welcome";
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/user/newsolicitude";
        }
    }

    // Método auxiliar para verificar si el archivo es una imagen
    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image");
    }


    @GetMapping("/delet/{id}")
    public String excluirSolicitude(@PathVariable("id") int id) {
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
