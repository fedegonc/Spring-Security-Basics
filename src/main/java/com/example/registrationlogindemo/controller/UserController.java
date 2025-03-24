package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.OrganizationService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

/**
 * Controlador para la gestión de usuarios.
 */
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
    private OrganizationService organizationService;

    @Autowired
    private RoleRepository roleRepository;

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
                msg.addFlashAttribute("error", "Error al procesar la imagen de perfil.");
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


            }
        }

        mv.setViewName("user/view-requests");
        return mv;
    }


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


            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar las estadísticas: " + e.getMessage());
        }
        return mv;
    }


    // Dashboard para usuarios con organización
    @GetMapping("/org/dashboard")
    public ModelAndView orgDashboard() {
        ModelAndView mv = new ModelAndView("organization/dashboard");

        try {
            // Obtener el usuario autenticado actualmente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario de la base de datos
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);

                // Obtener organizaciones propias y a las que pertenece el usuario
                List<Organization> ownedOrganizations = organizationService.getOrganizationsByOwner(usuario.getId());
                mv.addObject("ownedOrganizations", ownedOrganizations);
                mv.addObject("memberOrganizations", usuario.getMemberOrganizations());

                // Obtener las solicitudes pendientes
                try {
                    List<Solicitude> solicitudesPendientes = solicitudeService.getSolicitudesPendientes();
                    mv.addObject("solicitudesPendientes", solicitudesPendientes);
                } catch (Exception e) {
                    mv.addObject("errorSolicitudes", "Error al cargar las solicitudes: " + e.getMessage());
                }

                // Otros datos para la vista
                mv.addObject("username", username);
            }
        } catch (Exception e) {
            mv.addObject("error", "Ha ocurrido un error: " + e.getMessage());
        }

        return mv;
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


}
