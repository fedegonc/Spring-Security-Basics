package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    MaterialRepository materialRepository;

    @Autowired
    ImageRepository imageRepository;
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
        mv.addObject("principal", principal);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            mv.addObject("username", username);}
        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("root/edit");

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
    public String editUserBanco(@ModelAttribute("root/user_form") @Valid User user,
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

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, RedirectAttributes msg) {
        List<Solicitude> solicitudes = solicitudeRepository.findByUserId(id);
        for (Solicitude solicitude : solicitudes) {
            solicitudeRepository.delete(solicitude);
        }
        userRepository.deleteById(id);
        msg.addFlashAttribute("success", "Usuario eliminado exitosamente.");
        return "redirect:/admin/dashboard";
    }


    @GetMapping("/images")
    public ModelAndView rootImages() {
        ModelAndView mv = new ModelAndView("root/images");

        return mv;
    }

    @GetMapping("/users")
    public ModelAndView rootUsers() {
        ModelAndView mv = new ModelAndView("root/users");
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }
    @GetMapping("/solicitudes")
    public ModelAndView rootSolicitudes() {
        ModelAndView mv = new ModelAndView("root/solicitudes");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);

        return mv;
    }

    @GetMapping("/articles")
    public ModelAndView rootArticles() {
        ModelAndView mv = new ModelAndView("root/articles");
        List<Article> articulos = articleRepository.findAll();
        mv.addObject("articulos", articulos);
        return mv;
    }

    @GetMapping("/reports")
    public ModelAndView rootReports() {
        ModelAndView mv = new ModelAndView("root/reports");
        List<Report> reportes = reportRepository.findAll();
        mv.addObject("reportes", reportes);

        return mv;
    }
    @GetMapping("/roles")
    public ModelAndView rootRoles() {
        ModelAndView mv = new ModelAndView("root/roles");

        return mv;
    }

    @GetMapping("/settings")
    public ModelAndView rootSettings() {
        ModelAndView mv = new ModelAndView("root/settings");

        return mv;
    }

}
