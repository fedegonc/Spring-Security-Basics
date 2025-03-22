package com.example.registrationlogindemo.controller.user;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    SolicitudeService solicitudeService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ReportRepository reportRepository;

    @Autowired
    UserService userService;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    ImageService imageService;


    // Método para la página de bienvenida del usuario
    @GetMapping("/welcome")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView("user/welcome");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario y sus datos
                User usuario = userRepository.findByUsername(username);
                
                // Obtener las solicitudes del usuario
                List<Solicitude> solicitude = solicitudeService.getSolicitudesByUser(usuario);
                
                mv.addObject("solicitude", solicitude);
                mv.addObject("user", usuario);
                mv.addObject("username", username);
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar los datos del usuario: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/user/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("user/profile");
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }
            mv.addObject("user", user);

            // Agregar imágenes de idioma (si las hubiera)
            Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
            Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");

            imageService.addLanguageImages(mv, uruguaiImage, "uruguaiImageName");
            imageService.addLanguageImages(mv, brasilImage, "brasilImageName");
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
            mv.setViewName("redirect:/user/profile/" + id);
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
            mv.setViewName("redirect:/user/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }
    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.eliminarEntidad(id);
        return "redirect:/logout";
    }

    // Método para cerrar sesión
    @GetMapping("/logout")
    public String logout() {
        return "index";
    }

    @GetMapping("/users")
    public String listRegisteredUsers(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/construction")
    public ModelAndView showConstructionPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user/construction"); // Nombre de la vista
        // Aquí puedes agregar atributos al modelo si lo necesitas
        return modelAndView;
    }

    @GetMapping("/view-requests")
    public ModelAndView viewRequests() {
        ModelAndView mv = new ModelAndView();

        // Obtener la autenticación actual
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Obtener el usuario autenticado
            User usuario = userRepository.findByUsername(username);
            if (usuario != null) {
                // Cargar las solicitudes del usuario
                List<Solicitude> solicitudes = solicitudeRepository.findByUser(usuario);
                mv.addObject("solicitudes", solicitudes);

                // Verificar y cargar la imagen de perfil
                if (usuario.getProfileImage() == null || usuario.getProfileImage().isEmpty()) {
                    usuario.setProfileImage("descargas.jpeg");
                }
                mv.addObject("user", usuario);

                // Agregar imágenes de idioma (si las hubiera)
                Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
                Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");

                imageService.addLanguageImages(mv, uruguaiImage, "uruguaiImageName");
                imageService.addLanguageImages(mv, brasilImage, "brasilImageName");
            }
        }

        mv.setViewName("user/view-requests");
        return mv;
    }



  /*  @GetMapping("/viewarticles")
    public ModelAndView getArticles() {

        ModelAndView mv = new ModelAndView();

        // Obtener el usuario autenticado actualmente
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

            // Obtener las solicitudes realizadas por el usuario autenticado
            List<Article> articles = articleRepository.findAll();
            mv.addObject("articles", articles);
        }

        // Establecer la vista
        mv.setViewName("user/viewarticles");
        return mv;
    }*/
    @GetMapping("/informaciones")
    public ModelAndView informaciones() {
        ModelAndView mv = new ModelAndView("user/informaciones");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();

            // Agregar el nombre de usuario al modelo para dar la bienvenida
            mv.addObject("username", username);

            // Obtener el usuario de la base de datos
            User usuario = userRepository.findByUsername(username);
            mv.addObject("user", usuario);

            // Agregar imágenes de idioma (si las hubiera)
            Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
            Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");

            imageService.addLanguageImages(mv, uruguaiImage, "uruguaiImageName");
            imageService.addLanguageImages(mv, brasilImage, "brasilImageName");

        }
        return mv;
    }

    @GetMapping("/contacto")
    public ModelAndView contacto() {
        ModelAndView mv = new ModelAndView("user/noticias");
        return mv;

    }

    @GetMapping("/statistics")
    public ModelAndView estadisticas() {
        ModelAndView mv = new ModelAndView("user/statistics");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario y sus solicitudes
                User usuario = userRepository.findByUsername(username);
                List<Solicitude> solicitudes = solicitudeService.getSolicitudesByUser(usuario);

                // Calcular estadísticas básicas
                mv.addObject("totalSolicitudes", solicitudes.size());
                mv.addObject("completadas", solicitudes.stream()
                    .filter(s -> s.getEstado() == Estado.ACEPTADA)
                    .count());
                mv.addObject("pendientes", solicitudes.stream()
                    .filter(s -> s.getEstado() == Estado.EN_ESPERA)
                    .count());
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar las estadísticas: " + e.getMessage());
        }
        return mv;
    }

    // Método para redireccionar a la página de perfil del usuario actual
    @GetMapping("/editprofile")
    public String editProfileRedirect() {
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        
        // Si el usuario está autenticado, redirigir a su página de perfil
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            return "redirect:/user/profile/" + usuario.getId();
        } else {
            // Si no hay usuario autenticado, redirigir a la página de login
            return "redirect:/login";
        }
    }

    @PostMapping("/updatesolicitude/{id}")
    public ModelAndView updateSolicitude(@PathVariable("id") int id,
                                       @RequestParam("categoria") String categoria,
                                       @RequestParam("barrio") String barrio,
                                       @RequestParam("calle") String calle,
                                       @RequestParam("numeroDeCasa") String numeroDeCasa,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("currentImageUrl") String currentImageUrl,
                                       RedirectAttributes msg) {
        
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        
        if (authenticatedUserOpt.isPresent()) {
            User usuario = authenticatedUserOpt.get();
            
            // Obtener la solicitud
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
            
            if (solicitudeOpt.isPresent()) {
                Solicitude solicitude = solicitudeOpt.get();
                
                // Verificar que la solicitud pertenece al usuario actual
                if (solicitude.getUser().getId() == usuario.getId()) {
                    // Actualizar datos
                    solicitude.setCategoria(categoria);
                    solicitude.setBarrio(barrio);
                    solicitude.setCalle(calle);
                    solicitude.setNumeroDeCasa(numeroDeCasa);
                    
                    // Manejar la imagen
                    try {
                        if (!file.isEmpty()) {
                            String originalFilename = file.getOriginalFilename();
                            String modifiedFilename = originalFilename.replace(" ", "_");
                            
                            byte[] bytes = file.getBytes();
                            Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                            Files.write(path, bytes);
                            
                            solicitude.setImagen(modifiedFilename);
                        } else {
                            solicitude.setImagen(currentImageUrl);
                        }
                    } catch (IOException e) {
                        msg.addFlashAttribute("error", "Error al procesar la imagen");
                        return new ModelAndView("redirect:/user/editsolicitude/" + id);
                    }
                    
                    // Guardar cambios
                    solicitudeRepository.save(solicitude);
                    msg.addFlashAttribute("success", "Solicitud actualizada exitosamente");
                    return new ModelAndView("redirect:/user/view-requests");
                }
            }
        }
        
        msg.addFlashAttribute("error", "No se pudo actualizar la solicitud");
        return new ModelAndView("redirect:/user/view-requests");
    }

    @GetMapping("/materiales")
    public ModelAndView materiales() {
        ModelAndView mv = new ModelAndView("user/materiales");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);
            }
            
            // No necesitamos agregar materiales aquí ya que están directamente
            // en la plantilla HTML con estructura completa
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la información de materiales: " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/informaciones-recursos")
    public ModelAndView informacionesRecursos() {
        ModelAndView mv = new ModelAndView("user/informaciones-recursos");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);
            }
            
            // Agregar puntos de reciclaje para el mapa
            List<Map<String, Object>> puntosReciclaje = new ArrayList<>();
            
            // Punto 1 - Centro de reciclaje principal
            Map<String, Object> punto1 = new HashMap<>();
            punto1.put("id", 1);
            punto1.put("nombre", "Centro de Reciclaje Municipal");
            punto1.put("direccion", "Av. Italia 3242, Montevideo");
            punto1.put("lat", -34.882759);
            punto1.put("lng", -56.156124);
            punto1.put("horario", "Lunes a Viernes: 8:00 - 18:00, Sábados: 9:00 - 13:00");
            punto1.put("telefono", "+598 2345 6789");
            punto1.put("materiales", "Plástico, papel, cartón, vidrio, metal");
            puntosReciclaje.add(punto1);
            
            // Punto 2 - Centro comunitario
            Map<String, Object> punto2 = new HashMap<>();
            punto2.put("id", 2);
            punto2.put("nombre", "Centro Comunitario de Reciclaje");
            punto2.put("direccion", "Bulevar Artigas 1825, Montevideo");
            punto2.put("lat", -34.902591);
            punto2.put("lng", -56.184623);
            punto2.put("horario", "Lunes a Viernes: 9:00 - 17:00");
            punto2.put("telefono", "+598 2467 8901");
            punto2.put("materiales", "Plástico, papel, pilas, electrónicos pequeños");
            puntosReciclaje.add(punto2);
            
            // Punto 3 - Punto de recolección de electrónicos
            Map<String, Object> punto3 = new HashMap<>();
            punto3.put("id", 3);
            punto3.put("nombre", "Punto Verde - Electrónicos");
            punto3.put("direccion", "Av. 18 de Julio 1453, Montevideo");
            punto3.put("lat", -34.906287);
            punto3.put("lng", -56.179257);
            punto3.put("horario", "Miércoles y Sábados: 10:00 - 16:00");
            punto3.put("telefono", "+598 2589 0123");
            punto3.put("materiales", "Electrónicos, baterías, pilas");
            puntosReciclaje.add(punto3);
            
            // Punto 4 - Punto de reciclaje en centro comercial
            Map<String, Object> punto4 = new HashMap<>();
            punto4.put("id", 4);
            punto4.put("nombre", "Punto de Reciclaje Montevideo Shopping");
            punto4.put("direccion", "Luis Alberto de Herrera 1290, Montevideo");
            punto4.put("lat", -34.905302);
            punto4.put("lng", -56.143389);
            punto4.put("horario", "Todos los días: 10:00 - 22:00");
            punto4.put("telefono", "+598 2623 4567");
            punto4.put("materiales", "Plástico, papel, cartón, tetra pak");
            puntosReciclaje.add(punto4);
            
            // Punto 5 - Cooperativa de reciclaje
            Map<String, Object> punto5 = new HashMap<>();
            punto5.put("id", 5);
            punto5.put("nombre", "Cooperativa ReciclaUY");
            punto5.put("direccion", "Av. General Rivera 3456, Montevideo");
            punto5.put("lat", -34.900923);
            punto5.put("lng", -56.161849);
            punto5.put("horario", "Lunes a Viernes: 8:30 - 16:30");
            punto5.put("telefono", "+598 2789 0123");
            punto5.put("materiales", "Todo tipo de materiales reciclables");
            puntosReciclaje.add(punto5);
            
            mv.addObject("puntosReciclaje", puntosReciclaje);
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la información: " + e.getMessage());
        }
        return mv;
    }
}
