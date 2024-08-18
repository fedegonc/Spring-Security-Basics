package com.example.registrationlogindemo.controller.asociacion;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.MessageRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.MessageService;
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

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageService messageService;
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

        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);
            // Obtener los artículos del usuario actual
            List<Article> articles = articleRepository.findByUser(usuario);
            mv.addObject("articles", articles);
            // Otros datos que puedan ser útiles para el dashboard
            mv.addObject("username", username);
            mv.addObject("principal", authentication.getPrincipal().toString());
            List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("asociacion");
            mv.addObject("solicitudes", solicitudes);
            List<User> users = userRepository.findAll();
            mv.addObject("users", users);
        }

        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/asociacion/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("asociacion/profile");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
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
            mv.setViewName("redirect:/asociacion/profile/" + id);
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
            mv.setViewName("redirect:/asociacion/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }

    @GetMapping("/solicitudes")
    public ModelAndView Solicitudes() {
        ModelAndView mv = new ModelAndView("asociacion/solicitudes");

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("asociacion");
        mv.addObject("solicitudes", solicitudes);
        return mv;

    }

    @GetMapping("/reviewsolicitude/{id}")
    public ModelAndView showEditSolicitudeForm(@PathVariable("id") int id,
                                               @AuthenticationPrincipal UserDetails currentUser) {
        ModelAndView mv = new ModelAndView("asociacion/reviewsolicitude");
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);

        if (solicitudeOpt.isPresent()) {
            Solicitude solicitude = solicitudeOpt.get();
            mv.addObject("solicitude", solicitude);

            // Obtener todos los mensajes asociados a esta solicitud
            List<Message> messages = messageService.findMessagesBySolicitude(solicitude);
            mv.addObject("messages", messages);
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

    @PostMapping("/solicitude/{id}/messages")
    public String sendMessage(@PathVariable("id") int id,
                              @RequestParam("messageInput") String messageContent,
                              @AuthenticationPrincipal UserDetails currentUser,
                              RedirectAttributes redirectAttributes) {

        // Buscar la solicitud por ID
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud.");
            return "redirect:/asociacion/reviewsolicitude/" + id;
        }

        Solicitude solicitude = solicitudeOpt.get();

        // Buscar al usuario actual
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/asociacion/editsolicitude/" + id;
        }

        // Crear y guardar el nuevo mensaje
        Message newMessage = new Message();
        newMessage.setSolicitud(solicitude);
        newMessage.setUser(user);
        newMessage.setContenido(messageContent);
        newMessage.setFechaEnvio(LocalDateTime.now());

        messageRepository.save(newMessage);
        redirectAttributes.addFlashAttribute("exito", "Mensaje enviado con éxito.");

        // Redirigir de vuelta a la página de edición de la solicitud
        return "redirect:/asociacion/reviewsolicitude/" + id;
    }
    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") long id) {
        solicitudeRepository.deleteSolicitudeById(id);
        return "redirect:/asociacion/dashboard";
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
        return "redirect:/asociacion/dashboard";
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
    @GetMapping("/editarticle/{id}")
    public ModelAndView editArticle(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView();
        Optional<Article> articleOptional = articleRepository.findById((long) id);

        if (articleOptional.isPresent()) {
            Article article = articleOptional.get();
            mv.addObject("article", article);
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }
    @GetMapping("/deletearticle/{id}")
    public String deletearticle(@PathVariable("id") long id) {
        articleRepository.deleteById(id);
        return "redirect:/asociacion/dashboard";
    }


}
