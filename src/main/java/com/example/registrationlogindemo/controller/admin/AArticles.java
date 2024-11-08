package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AArticles {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    ImageService imageService;


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
    public ModelAndView newArticle() {
        ModelAndView mv = new ModelAndView("admin/newarticle");
        mv.addObject("article", new Article());
        return mv;
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                                 BindingResult result,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes msg) {

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/admin/newarticle";
        }

        if (!file.isEmpty()) {
            try {
                long MAX_SIZE = 64 * 1024; // 64 KB

                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                String formatName = file.getContentType().split("/")[1];

                bufferedImage = Thumbnails.of(bufferedImage)
                        .size(300, 300)
                        .outputQuality(0.5f)
                        .asBufferedImage();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, formatName, baos);
                baos.flush();
                byte[] resizedImageBytes = baos.toByteArray();
                baos.close();

                if (resizedImageBytes.length > MAX_SIZE) {
                    msg.addFlashAttribute("error", "La imagen comprimida es demasiado grande. El tamaño máximo permitido es 64KB.");
                    return "redirect:/admin/newarticle";
                }

                article.setImagenData(resizedImageBytes);
                article.setImagenNombre(file.getOriginalFilename());
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al procesar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/admin/newarticle";
            }
        } else {
            article.setImagenData(null);
            article.setImagenNombre(null);
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            article.setUser(user);
            article.setFechaRealizado(LocalDateTime.now());
            articleRepository.save(article);
            msg.addFlashAttribute("exito", "Artículo creado con éxito.");
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
                                         BindingResult result,
                                         RedirectAttributes msg,
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
                    BufferedImage bufferedImage = ImageIO.read(imagen.getInputStream());
                    String formatName = imagen.getContentType().split("/")[1];

                    bufferedImage = Thumbnails.of(bufferedImage)
                            .size(500, 500)
                            .outputQuality(0.8f)
                            .asBufferedImage();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, formatName, baos);
                    baos.flush();
                    byte[] resizedImageBytes = baos.toByteArray();
                    baos.close();

                    articleEdit.setImagenData(resizedImageBytes);
                    articleEdit.setImagenNombre(imagen.getOriginalFilename());
                }

                articleEdit.setTitulo(article.getTitulo());
                articleEdit.setDescripcion(article.getDescripcion());

                articleRepository.save(articleEdit);
                msg.addFlashAttribute("exito", "Artículo editado con éxito.");
                mv.setViewName("redirect:/admin/dashboard");
            } catch (IOException e) {
                e.printStackTrace();
                msg.addFlashAttribute("error", "Error al cargar el archivo de imagen.");
                mv.setViewName("redirect:/admin/editarticle/" + id);
            }
        } else {
            msg.addFlashAttribute("error", "No se encontró el artículo a editar.");
            mv.setViewName("redirect:/admin/editarticle/" + id);
        }

        return mv;
    }

    @GetMapping("/deletarticle/{id}")
    public String adminExcluir(@PathVariable("id") int id) {
        articleRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
