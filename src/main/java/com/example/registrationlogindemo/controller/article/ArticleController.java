package com.example.registrationlogindemo.controller.article;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
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
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    ImageService imageService;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

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
        imageService.addFlagImages(mv);
        return mv;
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                                 BindingResult result, RedirectAttributes msg,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/user/welcome";
        }

        if (!file.isEmpty()) {
            try {
                // Crear nombre de archivo único
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.write(filePath, file.getBytes());

            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/user/welcome";
            }
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            article.setUser(user);
            articleRepository.save(article);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/user/welcome";
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
        imageService.addFlagImages(mv);
        return mv;
    }
}
