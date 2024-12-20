package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
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
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ImageService imageService;



    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("guest/index");
        imageService.addFlagImages(mv);

        // Obtener la autenticación actual del usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && authentication.getPrincipal().equals("anonymousUser"));

        // Redirigir a /init si el usuario está autenticado
        if (isAuthenticated) {
            return new ModelAndView("redirect:/init");
        }


        // Convertir el String a Enum
        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("GENERICO");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);
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
        return "redirect:/";
    }

    @GetMapping("/img/{img}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable("img") String img) {

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




    @GetMapping("/construction")
    public ModelAndView contruction() {
        ModelAndView mv = new ModelAndView("user/construction");
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }


        imageService.addFlagImages(mv);
        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("GENERICO");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);
        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/ambiental")
    public ModelAndView ambiental() {
        ModelAndView mv = new ModelAndView("guest/ambiental");

        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }

        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("EDUCACION_AMBIENTAL");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);

        imageService.addFlagImages(mv);

        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/noticias")
    public ModelAndView noticias() {
        ModelAndView mv = new ModelAndView("guest/noticias");

        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }
        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("NOTICIA");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);

        imageService.addFlagImages(mv);


        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/materiales")
    public ModelAndView materiales() {
        ModelAndView mv = new ModelAndView("guest/materiales");


        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }

        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("TIPOS_DE_MATERIALES");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);

        imageService.addFlagImages(mv);


        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/alianzas")
    public ModelAndView alianzas() {
        ModelAndView mv = new ModelAndView("guest/alianzas");
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }

        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("ALIANZAS");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);

        imageService.addFlagImages(mv);


        mv.addObject("articles", articles);
        return mv;
    }
    @GetMapping("/legislacion")
    public ModelAndView legislacion() {
        ModelAndView mv = new ModelAndView("guest/legislacion");
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();

        // Si el usuario está autenticado, cargar datos en el modelo
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            mv.addObject("username", usuario.getUsername());
            mv.addObject("user", usuario);
            // Agregar imágenes personalizadas al modelo
        }

        Article.Categoria categoriaGenerica = Article.Categoria.valueOf("LEGISLACION");
        List<Article> articles = articleRepository.findByCategoria(categoriaGenerica);

        imageService.addFlagImages(mv);


        mv.addObject("articles", articles);
        return mv;
    }
    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("user/report-problem");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);
            imageService.addFlagImages(mv);
        }
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
            msg.addFlashAttribute("exito", "report realizada con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/init";
    }

}


