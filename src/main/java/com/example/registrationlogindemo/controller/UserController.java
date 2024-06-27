package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.ReportRepository;
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
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    ArticleRepository articleRepository;
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

            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

            // Obtener las solicitudes realizadas por el usuario autenticado
            List<Solicitude> solicitude = solicitudeRepository.findByUser(usuario);
            mv.addObject("solicitude", solicitude);
        }

        // Establecer la vista
        mv.setViewName("user/welcome");

        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/user/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user/profile");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("default-profile.jpg");
            }
            mv.addObject("user", user);
        }

        return mv;
    }

    @PostMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id,
                                 @ModelAttribute("user") @Valid User user,
                                 BindingResult result,
                                 @RequestParam("file") MultipartFile fileImage,
                                 @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                                 RedirectAttributes msg) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/user/profile/" + id);
            return mv;
        }

        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            userEdit.setUsername(user.getUsername());
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());

            try {
                if (!fileImage.isEmpty()) {
                    // Reemplazar espacios en el nombre del archivo
                    String originalFilename = fileImage.getOriginalFilename();
                    String modifiedFilename = originalFilename.replace(" ", "_");

                    byte[] bytes = fileImage.getBytes();
                    Path path = Paths.get("./src/main/resources/static/img/" + modifiedFilename);
                    Files.write(path, bytes);
                    userEdit.setProfileImage(modifiedFilename);
                } else {
                    userEdit.setProfileImage(currentProfileImageUrl);
                }


            } catch (IOException e) {
                System.out.println("Error de imagen");
            }

            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
            mv.setViewName("redirect:/user/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }
    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.eliminarEntidad(id);
        return "redirect:/logout";
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

    @GetMapping("/construction")
    public ModelAndView showConstructionPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/construction"); // Nombre de la vista
        // Aquí puedes agregar atributos al modelo si lo necesitas
        return modelAndView;
    }

    @GetMapping("/view-requests")
    public ModelAndView viewRequests() {
        ModelAndView mv = new ModelAndView();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User usuario = userRepository.findByUsername(username);

            List<Solicitude> solicitudes = solicitudeRepository.findByUser(usuario);
            mv.addObject("solicitudes", solicitudes);
        }
        mv.setViewName("user/view-requests");
        return mv;
    }

    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("/report-problem");
        return mv;
    }
    @PostMapping("/report")
    public String newReportPost(@Valid Report report,
                                    BindingResult result, RedirectAttributes msg,
                                    @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            report.setUser(user);
            reportRepository.save(report);
            msg.addFlashAttribute("rexito", "report realizada con éxito.");
        } else {
            msg.addFlashAttribute("rerror", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/user/welcome";
    }

    @GetMapping("/viewarticles")
    public ModelAndView getArticles() {

        ModelAndView mv = new ModelAndView();

        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

            // Obtener las solicitudes realizadas por el usuario autenticado
            List<Article> articles = articleRepository.findAll();
            mv.addObject("articles", articles);
        }

        // Establecer la vista
        mv.setViewName("user/viewarticles");

        return mv;
    }
    // Método para obtener imágenes
}
