package com.example.registrationlogindemo.controller.cooperativa;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Message;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.Estado;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.MessageRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.MessageService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import net.coobird.thumbnailator.Thumbnails;
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

@Controller
@RequestMapping("/cooperativa")
public class CooperativaController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    MessageService messageService;
    @Autowired
    MessageRepository messageRepository;
    @Autowired
    ImageService imageService;
    @Autowired
    UserService userService;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboardCooperativa() {
        ModelAndView mv = new ModelAndView("cooperativa/dashboard");
        
        // Obtener información del usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            User usuario = userRepository.findByUsername(username);
            mv.addObject("username", username);
            mv.addObject("user", usuario);
        }

        // Obtener solicitudes dirigidas a cooperativa
        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("cooperativa");
        mv.addObject("solicitudes", solicitudes);
        
        // Estadísticas de solicitudes con manejo seguro
        long totalSolicitudes = solicitudes != null ? solicitudes.size() : 0;
        long pendientes = 0;
        long aceptadas = 0;
        long rechazadas = 0;
        
        if (solicitudes != null) {
            pendientes = solicitudes.stream()
                    .filter(s -> s.getEstado() != null && s.getEstado() == Estado.EN_ESPERA)
                    .count();
            aceptadas = solicitudes.stream()
                    .filter(s -> s.getEstado() != null && s.getEstado() == Estado.ACEPTADA)
                    .count();
            rechazadas = solicitudes.stream()
                    .filter(s -> s.getEstado() != null && s.getEstado() == Estado.RECHAZADA)
                    .count();
        }
        
        mv.addObject("totalSolicitudes", totalSolicitudes);
        mv.addObject("pendientes", pendientes);
        mv.addObject("aceptadas", aceptadas);
        mv.addObject("rechazadas", rechazadas);

        // Obtener usuarios y artículos
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        List<Article> articles = articleRepository.findAll(); 
        mv.addObject("articles", articles);

        return mv;
    }


    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/cooperativa/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("cooperativa/profile");
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
            mv.setViewName("redirect:/cooperativa/profile/" + id);
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
            mv.setViewName("redirect:/cooperativa/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }

    @GetMapping("/solicitudes")
    public ModelAndView Solicitudes() {
        ModelAndView mv = new ModelAndView("cooperativa/solicitudes");

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("cooperativa");
        mv.addObject("solicitudes", solicitudes);


        return mv;
    }


    @GetMapping("/reviewsolicitude/{id}")
    public ModelAndView showEditSolicitudeForm(@PathVariable("id") int id,
                                               @AuthenticationPrincipal UserDetails currentUser) {
        ModelAndView mv = new ModelAndView("cooperativa/reviewsolicitude");
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

        }
        if (solicitudeOpt.isPresent()) {
            Solicitude solicitude = solicitudeOpt.get();
            mv.addObject("solicitude", solicitude);

            // Obtener todos los mensajes asociados a esta solicitud
            List<Message> messages = messageService.findMessagesBySolicitude(solicitude);
            mv.addObject("messages", messages);
        } else {
            mv.setViewName("redirect:/cooperativa/dashboard");
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
            existingSolicitude.setEstado(Estado.valueOf(estado));

            solicitudeRepository.save(existingSolicitude);
            msg.addFlashAttribute("exito", "Estado de la solicitud editado con éxito.");
            mv.setViewName("redirect:/cooperativa/dashboard");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
            mv.setViewName("redirect:/cooperativa/dashboard");
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
            return "redirect:/cooperativa/dashboard/" + id;
        }

        Solicitude solicitude = solicitudeOpt.get();

        // Buscar al usuario actual
        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/cooperativa/dashboard/" + id;
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
        return "redirect:/cooperativa/reviewsolicitude/" + id;
    }

    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") long id) {
        solicitudeRepository.deleteSolicitudeById(id);

        return "redirect:/cooperativa/dashboard";
    }

    @GetMapping("/articles")
    public ModelAndView getArticles() {
        ModelAndView mv = new ModelAndView("cooperativa/articles");

        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);

        return mv;
    }

    @GetMapping("/deletearticle/{id}")
    public String deleteArticle(@PathVariable("id") long id) {
        articleRepository.deleteById(id);

        return "redirect:/cooperativa/dashboard";
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
            msg.addFlashAttribute("error", "Error al cargar el artículo. Por favor, complete todos los campos correctamente.");
            return "redirect:/cooperativa/dashboard";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        try {
            if (!file.isEmpty()) {
                // Procesar la imagen y guardarla en la base de datos
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                String formatName = file.getContentType().split("/")[1]; // Obtiene el formato de la imagen

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
                article.setImagenData(resizedImageBytes); // Ajusta según tu entidad

                article.setImagenNombre(file.getOriginalFilename());
            } else {
                // Establecer valores predeterminados si no se carga imagen
                article.setImagenData(null);

                article.setImagenNombre(null);
            }
        } catch (IOException e) {
            msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
            return "redirect:/cooperativa/newarticles";
        }

        User currentUser = userRepository.findByUsername(username);
        article.setUser(currentUser);

        // Guardar el artículo en la base de datos
        articleRepository.save(article);

        msg.addFlashAttribute("success", "Artículo cargado exitosamente.");
        return "redirect:/cooperativa/newarticles";
    }
}
