package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/admin")
public class AArticles {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;



    private UserService userService;
    private ImageService imageService;

    // Constructor que inyecta el servicio UserService
    public void AUser(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping("/articles")
    public ModelAndView adminViewArticles() {
        ModelAndView mv = new ModelAndView("admin/articles");


        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        return mv;
    }
    @GetMapping("/viewarticle/{id}")
    public ModelAndView getArticle(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("article/viewarticle");
        Optional<Article> articleOptional = articleRepository.findById((long) id);

        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            mv.addObject("article", article);
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }
    @GetMapping("/newarticle")
    public ModelAndView newarticle() {
        return new ModelAndView("admin/newarticle");
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                                 BindingResult result, RedirectAttributes msg,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/admin/newarticle";
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
                return "redirect:/admin/newarticle";
            }
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            article.setUser(user);
            articleRepository.save(article);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/admin/newarticle";
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/editarticle/{id}")
    public ModelAndView editArticle(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView();
        Optional<Article> articleOptional = articleRepository.findById((long) id);

        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            mv.addObject("article", article);
            mv.setViewName("article/editarticle");
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }
    @PostMapping("/editarticle/{id}")
    public ModelAndView adminEditArticle(@PathVariable("id") long id,
                                         @ModelAttribute("article") @Valid Article article,
                                         BindingResult result, RedirectAttributes msg,
                                         @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/admin/editarticle/" + id);
            return mv;
        }

        Optional<Article> articleOptional = articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            Article articleEdit = articleOptional.get();

            try {
                if (!imagen.isEmpty()) {
                    byte[] bytes = imagen.getBytes();
                    Path path = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                    Files.write(path, bytes);
                    articleEdit.setImagen(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                e.printStackTrace();
                msg.addFlashAttribute("error", "Error al cargar el archivo de imagen.");
                mv.setViewName("redirect:/admin/editarticle/" + id);
                return mv;
            }

            // Actualizar los campos del artículo
            articleEdit.setTitulo(article.getTitulo());
            articleEdit.setDescripcion(article.getDescripcion());

            articleRepository.save(articleEdit);
            msg.addFlashAttribute("exito", "Artículo editado con éxito.");
            mv.setViewName("redirect:/admin/dashboard");
        } else {
            msg.addFlashAttribute("error", "No se encontró el artículo a editar.");
            mv.setViewName("redirect:/admin/editarticle/" + id);
        }

        return mv;
    }

    // Método para eliminar un artículo
    @GetMapping("/deletarticle/{id}")
    public String adminExcluir(@PathVariable("id") int id) {
        articleRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
