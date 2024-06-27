package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/asociacion")
public class AsociacionController {

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

    public AsociacionController(UserService userService) {
        this.userService = userService;
    }
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboardAsociacion() {
        ModelAndView mv = new ModelAndView("asociacion/dashboard");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

        }


        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("asociacion");
        mv.addObject("solicitudes", solicitudes);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        List<Article> articles = articleRepository.findAll(); // Obtener la lista de artículos
        mv.addObject("articles", articles); // Agr

        return mv;
    }

    @GetMapping("/reviewsolicitude/{id}")
    public ModelAndView reviewSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("asociacion/reviewsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById( id);

        if (solicitudeOptional.isPresent()) {
            mv.addObject("solicitude", solicitudeOptional.get());
        } else {
            mv.setViewName("redirect:/asociacion/dashboard");
        }
        return mv;
    }
    @PostMapping("/reviewsolicitude/{id}")
    public ModelAndView editSolicitude(@PathVariable("id") int id,
                                       @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, RedirectAttributes msg,
                                       @RequestParam("estado") String estado) {
        ModelAndView mv = new ModelAndView();

        Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(id);
        if (existingSolicitudeOpt.isPresent()) {
            Solicitude existingSolicitude = existingSolicitudeOpt.get();
            existingSolicitude.setEstado(Solicitude.Estado.valueOf(estado));

            solicitudeRepository.save(existingSolicitude);
            msg.addFlashAttribute("exito", "Estado de la solicitud editado con éxito.");
            mv.setViewName("redirect:/asociacion/dashboard");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
            mv.setViewName("redirect:/asociacion/dashboard");
        }

        return mv;
    }

    @GetMapping("/articles")
    public ModelAndView getArticles() {
        ModelAndView mv = new ModelAndView("asociacion/articles");
        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        articleRepository.deleteById(id);
        return "redirect:/asociacion";
    }
    @GetMapping("/newarticle")
    public ModelAndView newarticle() {
        return new ModelAndView("asociacion/newarticle");
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                                 BindingResult result, RedirectAttributes msg,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/asociacion/newarticle";
        }

        if (!file.isEmpty()) {
            try {
                // Crear nombre de archivo único
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.write(filePath, file.getBytes());
                article.setImagen(uniqueFileName);
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/asociacion/newarticle";
            }
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            article.setUser(user);
            articleRepository.save(article);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/asociacion/newarticle";
        }

        return "redirect:/asociacion/dashboard";
    }


}
