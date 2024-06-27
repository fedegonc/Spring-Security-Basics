package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/cooperativa")
public class CooperativaController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ArticleRepository articleRepository;
    private UserService userService;

    // Constructor que inyecta el servicio UserService
    public void UserController(UserService userService) {
        this.userService = userService;
    }

    public CooperativaController(UserService userService) {
        this.userService = userService;
    }
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboardCooperativa() {
        ModelAndView mv = new ModelAndView("cooperativa/dashboard");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("cooperativa");
        mv.addObject("solicitudes", solicitudes);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        List<Article> articles = articleRepository.findAll(); // Obtener la lista de artículos
        mv.addObject("articles", articles); // Agr

        return mv;
    }

    @GetMapping("/articles")
    public ModelAndView getArticles() {
        ModelAndView mv = new ModelAndView("cooperativa/articles");

        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        articleRepository.deleteById(id);
        return "redirect:/logout";
    }

    @GetMapping("/newarticle")
    public ModelAndView newarticle() {
        return new ModelAndView("cooperativa/newarticle");
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al cargar la imagen. Por favor, complete todos los campos correctamente.");
            return "redirect:/cooperativa/dashboard";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String modifiedFilename = file.getOriginalFilename().replace(" ", "_"); // Replace spaces in the filename
                Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                Files.write(path, bytes);
                article.setImagen(modifiedFilename);
            } else {
                article.setImagen(null); // Optional: Set a default value if no image is uploaded
            }
        } catch (IOException e) {
            msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
            return "redirect:/cooperativa/newarticles";
        }


        User currentUser = userRepository.findByUsername(username);
        article.setUser(currentUser);

        // Save the image to the database or perform other necessary operations
        articleRepository.save(article);

        msg.addFlashAttribute("success", "Imagen cargada exitosamente.");
        return "redirect:/cooperativa/newarticles";
    }
}
