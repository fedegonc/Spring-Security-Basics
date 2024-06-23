package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
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

@RequestMapping("/root")
@Controller
public class RootController {
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";


    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    MaterialRepository materialRepository;

    @Autowired
    ImageRepository imageRepository;
    private final UserService userService;

    // Constructor que inyecta el servicio UserService
    public RootController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ModelAndView rootDashboard() {
        ModelAndView mv = new ModelAndView("root/dashboard");
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        principal.toString();
        mv.addObject("principal", principal);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            mv.addObject("username", username);
        }
        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("root/edit");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Obtener la lista de roles
            List<Role> listRoles = userService.listRoles();
            // Agregar el usuario y la lista de roles al modelo
            mv.addObject("user", user);
            mv.addObject("listRoles", listRoles);
        }
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/edit/{id}")
    public String editUserBanco(@ModelAttribute("root/user_form") @Valid User user,
                                BindingResult result, RedirectAttributes msg) {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + user.getId();
        }

        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualizar los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            userEdit.setRoles(user.getRoles());
            // Guardar los cambios en la base de datos
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró el usuario a editar.");
        }

        return "redirect:/root/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") long id, RedirectAttributes msg) {
        List<Solicitude> solicitudes = solicitudeRepository.findByUserId(id);
        for (Solicitude solicitude : solicitudes) {
            solicitudeRepository.delete(solicitude);
        }
        userRepository.deleteById(id);
        msg.addFlashAttribute("success", "Usuario eliminado exitosamente.");
        return "redirect:/admin/dashboard";
    }


    @GetMapping("/images")
    public ModelAndView rootImages() {
        ModelAndView mv = new ModelAndView("root/images");
        List<Image> images = imageRepository.findAll();
        mv.addObject("images", images);

        return mv;
    }

    @GetMapping("/users")
    public ModelAndView rootUsers() {
        ModelAndView mv = new ModelAndView("root/users");
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }
    @GetMapping("/solicitudes")
    public ModelAndView rootSolicitudes() {
        ModelAndView mv = new ModelAndView("root/solicitudes");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitude", solicitude);
        return mv;
    }

    @GetMapping("/articles")
    public ModelAndView rootArticles() {
        ModelAndView mv = new ModelAndView("root/articles");
        List<Article> articles = articleRepository.findAll();
        mv.addObject("articles", articles);
        return mv;
    }

    @GetMapping("/reports")
    public ModelAndView rootReports() {
        ModelAndView mv = new ModelAndView("root/reports");
        List<Report> reports = reportRepository.findAll();
        mv.addObject("reports", reports);

        return mv;
    }
    @GetMapping("/roles")
    public ModelAndView rootRoles() {
        ModelAndView mv = new ModelAndView("root/roles");
        List<Role> roles = roleRepository.findAll();
        mv.addObject("roles", roles);

        return mv;
    }
    @GetMapping("/newimage")
    public ModelAndView newimage() {
        ModelAndView mv = new ModelAndView("image/newimage");
        return mv;
    }

    @PostMapping("/newimage")
    public String uploadImage(@Valid Image image,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              @RequestParam("file") MultipartFile file,
                              @AuthenticationPrincipal UserDetails currentUser) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos.");
            return "redirect:/user/welcome"; // Cambia esta URL según la estructura de tu aplicación
        }

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path imagePath = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
                Files.write(imagePath, bytes);
                image.setImagen(file.getOriginalFilename());
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/user/welcome"; // Cambia esta URL según la estructura de tu aplicación
            }
        } else {
            image.setImagen(null); // O establece un valor por defecto
        }

        User user = userRepository.findByUsername(currentUser.getUsername());
        if (user != null) {
            image.setUser(user);
            image.setFecha(LocalDateTime.now()); // Establece la fecha actual
            imageRepository.save(image);
            redirectAttributes.addFlashAttribute("exito", "Imagen subida con éxito.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/user/welcome"; // Cambia esta URL según la estructura de tu aplicación
    }


    @GetMapping("/editimage/{id}")
    public ModelAndView editImage(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("image/editimage");
        Optional<Image> image = imageRepository.findById(id);
        if (image.isPresent()) {
            mv.addObject("image", image.get());
        } else {
            mv.setViewName("redirect:/user/welcome");
        }
        return mv;
    }

    @PostMapping("/editimage/{id}")
    public ModelAndView editImage(@PathVariable("id") long id,
                                  @ModelAttribute("image") @Valid Image image,
                                  BindingResult result, RedirectAttributes msg,
                                  @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/editimage/" + id);
            return mv;
        }

        Optional<Image> imageOptional = imageRepository.findById(id);
        if (imageOptional.isPresent()) {
            Image imageEdit = imageOptional.get();

            try {
                if (!imagen.isEmpty()) {
                    byte[] bytes = imagen.getBytes();
                    Path path = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                    Files.write(path, bytes);
                    imageEdit.setImagen(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                e.printStackTrace();
                msg.addFlashAttribute("error", "Error al cargar el archivo de imagen.");
                mv.setViewName("redirect:/editimage/" + id);
                return mv;
            }

            imageRepository.save(imageEdit);
            msg.addFlashAttribute("exito", "Imagen editada con éxito.");
            mv.setViewName("redirect:/root/images");
        } else {
            msg.addFlashAttribute("error", "No se encontró la imagen a editar.");
            mv.setViewName("redirect:/root/images");
        }

        return mv;
    }

}
