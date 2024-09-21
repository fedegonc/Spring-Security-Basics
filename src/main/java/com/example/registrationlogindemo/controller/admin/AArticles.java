package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
        ModelAndView mv = new ModelAndView("admin/newarticle"); // Inicializa el ModelAndView
        mv.addObject("article", new Article()); // Agrega el objeto article
        return mv; // Retorna el ModelAndView
    }

    @PostMapping("/newarticle")
    public String newArticlePost(@Valid Article article,
                                 BindingResult result,
                                 @RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes msg) {

        // Validación de errores en el formulario
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/admin/newarticle";
        }

        // Procesar imagen si está presente
        if (!file.isEmpty()) {
            try {
                // Tamaño máximo en bytes (64 KB para BLOB)
                long MAX_SIZE = 64 * 1024;

                // Leer imagen original desde el archivo subido
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                String formatName = file.getContentType().split("/")[1]; // Obtener formato de la imagen

                // Comprimir y redimensionar la imagen
                bufferedImage = Thumbnails.of(bufferedImage)
                        .size(300, 300)         // Reducir a 300x300 píxeles
                        .outputQuality(0.5f)    // Comprimir calidad al 50%
                        .asBufferedImage();

                // Convertir la imagen comprimida a bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, formatName, baos);
                baos.flush();
                byte[] resizedImageBytes = baos.toByteArray();
                baos.close();

                // Validar el tamaño de la imagen después de la compresión
                if (resizedImageBytes.length > MAX_SIZE) {
                    msg.addFlashAttribute("error", "La imagen comprimida es demasiado grande. El tamaño máximo permitido es 64KB.");
                    return "redirect:/admin/newarticle";
                }

                // Guardar los datos de la imagen en el artículo
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

        // Asignar el usuario actual al artículo
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            article.setUser(user);
            article.setFechaRealizado(LocalDateTime.now()); // Establecer la fecha actual
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
                    // Procesar la imagen y guardarla en la base de datos
                    BufferedImage bufferedImage = ImageIO.read(imagen.getInputStream());
                    String formatName = imagen.getContentType().split("/")[1]; // Obtiene el formato de la imagen

                    // Redimensionar la imagen
                    bufferedImage = Thumbnails.of(bufferedImage)
                            .size(500, 500)
                            .outputQuality(0.8f)
                            .asBufferedImage();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, formatName, baos);
                    baos.flush();
                    byte[] resizedImageBytes = baos.toByteArray();
                    baos.close();

                    // Guardar los datos de la imagen en el artículo
                    articleEdit.setImagenData(resizedImageBytes); // Ajusta según tu entidad

                    articleEdit.setImagenNombre(imagen.getOriginalFilename());
                }

                // Actualizar los campos del artículo
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


    // Método para eliminar un artículo
    @GetMapping("/deletarticle/{id}")
    public String adminExcluir(@PathVariable("id") int id) {
        articleRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
