package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {
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
    private final ImageService imageService;

    // Constructor que inyecta el servicio UserService
    public AdminController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    // Método para mostrar el dashboard
    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());

        List<Solicitude> solicitudes = solicitudeRepository.findAll();
        mv.addObject("solicitudes", solicitudes);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }

    // Método para revisar una solicitud específica
    @GetMapping("/reviewsolicitude/{id}")
    public ModelAndView reviewSolicitude(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("reviewsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById((int) id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
        } else {
            mv.setViewName("redirect:/admin/dashboard");
        }

        return mv;
    }

    // Método para editar un usuario
    @GetMapping("/edit/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user_form");

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
    public String editUserBanco(@ModelAttribute("user_form") @Valid User user,
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

        return "redirect:/dashboard";
    }

    // Método para guardar un usuario
    @PostMapping("/users/save")
    public String saveUser(User user) {
        userService.save(user);
        return "redirect:/users";
    }

    // Método para obtener imágenes
    @RequestMapping(value = "/img/{imagem}", method = RequestMethod.GET)
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }

    // Método para mostrar el formulario de nueva solicitud
    @RequestMapping(value = "/newsolicitude", method = RequestMethod.GET)
    public String newSolicitude() {
        return "solicitude/newsolicitude";
    }

    // Método para procesar la creación de una nueva solicitud
    @RequestMapping(value = "/newsolicitude", method = RequestMethod.POST)
    public String newSolicitudePost(@Valid Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/dashboard";
        }
        try {
            if (!imagen.isEmpty()) {
                byte[] bytes = imagen.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                Files.write(caminho, bytes);
                solicitud.setImagen(imagen.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Error al salvar imagen");
        }
        solicitudeRepository.save(solicitud);
        msg.addFlashAttribute("Exito", "Solicitud realizada con éxito.");
        return "redirect:/dashboard";
    }

    // Método para eliminar una solicitud
    @RequestMapping(value = "/deletsolicitude/{id}", method = RequestMethod.GET)
    public String excluirSolicitud(@PathVariable("id") int id) {
        solicitudeRepository.deleteSolicitudeById((long) id);
        return "redirect:/admin/dashboard";
    }

    // Método para modificar el estado de una solicitud
  /*  @RequestMapping(value = "/modifyestate/{id}", method = RequestMethod.GET)
    public String modifyEstateSolicitud(@PathVariable("id") int id) {
        Solicitude solicitude = solicitudeRepository.findById(id).orElse(null);
        if (solicitude != null) {
            if ("Activo".equals(solicitude.getActivo())) {
                solicitude.setActivo("Inactivo");
            } else {
                solicitude.setActivo("Activo");
            }
            solicitudeRepository.save(solicitude);
        }
        return "redirect:/dashboard";
    }
*/
    // Método para mostrar el formulario de edición de solicitud
    @RequestMapping(value = "/editsolicitude/{id}", method = RequestMethod.GET)
    public ModelAndView editSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
        } else {
            mv.setViewName("redirect:/error");
        }
        return mv;
    }


    // Método para procesar la edición de una solicitud
    @RequestMapping(value = "/editsolicitude/{id}", method = RequestMethod.POST)
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            // Si hay errores de validación, redirige con un mensaje de error
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/editar/" + solicitude.getId();
        }
        // Obtener la solicitud que se va a editar
        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            // Actualizar los campos de la solicitud con los nuevos valores
            changeSolicitude.setNombre(solicitude.getNombre());
            changeSolicitude.setCategoria(solicitude.getCategoria());

            changeSolicitude.setDescripcion(solicitude.getDescripcion());

            try {
                // Guardar la imagen si se proporciona una
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }
            // Guardar la solicitud editada en la base de datos
            solicitudeRepository.save(changeSolicitude);
            // Redirigir con un mensaje de éxito
            msg.addFlashAttribute("sucesso", "Alimento editado com sucesso.");
        }

        return "redirect:/dashboard";
    }
    @GetMapping("/images")
    public ModelAndView rootImages() {
        ModelAndView mv = new ModelAndView("admin/images");
        List<Image> images = imageRepository.findAll();
        mv.addObject("images", images);

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
            return "redirect:/admin/dashboard"; // Cambia esta URL según la estructura de tu aplicación
        }

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path imagePath = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
                Files.write(imagePath, bytes);
                image.setImagen(file.getOriginalFilename());
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/admin/images"; // Cambia esta URL según la estructura de tu aplicación
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

        return "redirect:/admin/images"; // Cambia esta URL según la estructura de tu aplicación
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
            mv.setViewName("redirect:/admin/editimage/" + id);
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
                mv.setViewName("redirect:/admin/editimage/" + id);
                return mv;
            }

            imageRepository.save(imageEdit);
            msg.addFlashAttribute("exito", "Imagen editada con éxito.");
            mv.setViewName("redirect:/admin/images");
        } else {
            msg.addFlashAttribute("error", "No se encontró la imagen a editar.");
            mv.setViewName("redirect:/admin/images");
        }

        return mv;
    }


    @GetMapping("/deletimage/{id}")
    public String deleteiMAUser(@PathVariable("id") long id) {
        imageService.eliminarEntidad(id);
        return "redirect:/root/images";
    }

    @GetMapping("/reports")
    public ModelAndView newReports() {
        ModelAndView mv = new ModelAndView("admin/reports");
        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

        }

        List<Report> reports = reportRepository.findAll();
        mv.addObject("reports", reports);
        return mv;
    }
    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("/report-problem");
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

        return "redirect:/admin/dashboard";
    }
    @GetMapping("/users")
    public ModelAndView rootUsers() {
        ModelAndView mv = new ModelAndView("admin/users");
        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

        }
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);


        return mv;
    }
}