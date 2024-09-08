package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class GuestController {
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;

    @Autowired
    ImageRepository imageRepository;


    // Obtiene la página de inicio y muestra las solicitudes activas
    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("index");
        Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
        Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");

        if (uruguaiImage.isPresent()) {
            mv.addObject("uruguaiImageName", uruguaiImage.get().getNombre()); // Cambié el nombre de la variable
        }
        if (brasilImage.isPresent()) {
            mv.addObject("brasilImageName", brasilImage.get().getNombre()); // Cambié el nombre de la variable
        }

        // Obtener la autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && authentication.getPrincipal().equals("anonymousUser"));

        // Agregar el estado de autenticación al modelo
        mv.addObject("isAuthenticated", isAuthenticated);

        // Obtener los artículos y agregarlos al modelo
        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Obtiene el contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            // Invalida la sesión actual
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        // Redirige a la página de inicio después de cerrar sesión
        return "redirect:/";
    }

    @GetMapping("/img/{img}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("img") String img) {
        // Primero intentamos obtener la imagen del directorio estático
        File file = new File("./src/main/resources/static/img/" + img);
        if (file.exists() && !file.isDirectory()) {
            try {
                byte[] imageBytes = Files.readAllBytes(file.toPath());
                String contentType = Files.probeContentType(file.toPath());
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(imageBytes);
            } catch (IOException e) {
                // En caso de error al leer el archivo
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Si la imagen no se encuentra en el directorio estático, buscar en la base de datos
        Optional<Image> image = imageRepository.findByNombre(img);
        if (image.isPresent()) {
            image.get();
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(String.valueOf(image.get()));
            } catch (IllegalArgumentException e) {
                // Si el tipo de contenido no es válido, usar un tipo de contenido por defecto
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(image.get().getData());
        }

        // Si la imagen no se encuentra ni en el directorio estático ni en la base de datos
        return ResponseEntity.notFound().build();
    }


    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("report-problem");
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

        return "redirect:/init";
    }

    @GetMapping("/construction")
    public ModelAndView contruction() {
        ModelAndView mv = new ModelAndView("user/construction");
        return mv;
    }

}

