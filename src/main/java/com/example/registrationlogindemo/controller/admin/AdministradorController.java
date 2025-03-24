package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdministradorController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RoleRepository roleRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    @GetMapping("/dashboard")
    public ModelAndView getDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");
        List<User> users = userRepository.findAll();
        mv.addObject("users", users);
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
            mv.addObject("organizationTypes", OrganizationType.values());
        }

        return mv;
    }

    @PostMapping("/profile/{id}")
    public ModelAndView editUser(@ModelAttribute("user") @Valid User user,
                                 BindingResult result,
                                 @PathVariable("id") long id,
                                 @RequestParam("fileImage") MultipartFile fileImage,
                                 @RequestParam("currentProfileImageUrl") String currentProfileImageUrl,
                                 @RequestParam(value = "organizationType", required = false) String organizationType,
                                 @RequestParam(value = "organizationName", required = false) String organizationName,
                                 RedirectAttributes msg) {

        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/admin/profile/" + id);
            return mv;
        }

        Optional<User> userEditOpt = userRepository.findById(id);

        if (userEditOpt.isPresent()) {
            User userEdit = userEditOpt.get();
            userEdit.setUsername(user.getUsername());
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());

            // Actualizar tipo y nombre de organización
            if (organizationType != null && !organizationType.isEmpty()) {
                userEdit.setOrganizationType(OrganizationType.valueOf(organizationType));
            }
            userEdit.setOrganizationName(organizationName);

            try {
                // Si se proporciona una nueva imagen de perfil
                if (!fileImage.isEmpty()) {
                    String originalFilename = fileImage.getOriginalFilename();
                    if (originalFilename != null) {
                        String modifiedFilename = originalFilename.replace(" ", "_");

                        byte[] bytes = fileImage.getBytes();
                        Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                        Files.write(path, bytes);
                        userEdit.setProfileImage(modifiedFilename);
                    }
                } else {
                    // Mantener la imagen de perfil actual si no se proporciona una nueva
                    userEdit.setProfileImage(currentProfileImageUrl);
                }
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al procesar la imagen de perfil: " + e.getMessage());
                mv.setViewName("redirect:/admin/profile/" + id);
                return mv;
            }

            // Guardar el usuario editado
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
            mv.setViewName("redirect:/admin/profile/" + user.getId());
        } else {
            mv.setViewName("redirect:/error");
        }

        return mv;
    }

    @GetMapping("/reports")
    public ModelAndView adminReports() {
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

    @GetMapping("/editereport/{id}")
    public ModelAndView adminEditReport(@PathVariable("id") long id) {
        ModelAndView mv = new ModelAndView("admin/editreport");
        Optional<Report> image = reportRepository.findById(id);
        if (image.isPresent()) {
            mv.addObject("image", image.get());
        } else {
            mv.setViewName("redirect:/admin/dashboard");
        }
        return mv;
    }

    @GetMapping("/solicitudes")
    public ModelAndView adminSolicitudes() {
        ModelAndView mv = new ModelAndView("admin/solicitudes");
        List<Solicitude> solicitude = solicitudeRepository.findAll();
        mv.addObject("solicitudes", solicitude);
        return mv;
    }

    // Método para mostrar el formulario de edición de solicitud
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView adminEditSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("admin/editsolicitude");
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
    @PostMapping("/editsolicitude/{id}")
    public String editSolicitudeBanco(@ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            // Si hay errores de validación, redirige con un mensaje de error
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/admin/editsolicitude/" + solicitude.getId();
        }
        // Obtener la solicitud que se va a editar
        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            // Actualizar los campos de la solicitud con los nuevos valores
            changeSolicitude.setCategoria(solicitude.getCategoria());
            changeSolicitude.setDescripcion(solicitude.getDescripcion());

            // Actualizar campos de ubicación
            changeSolicitude.setBarrio(solicitude.getBarrio());
            changeSolicitude.setCalle(solicitude.getCalle());
            changeSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());

            try {
                // Guardar la imagen si se proporciona una
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get(UPLOAD_DIR + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            }
            // Guardar la solicitud editada en la base de datos
            solicitudeRepository.save(changeSolicitude);
            // Redirigir con un mensaje de éxito
            msg.addFlashAttribute("success", "Solicitud editada exitosamente.");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
        }

        return "redirect:/admin/solicitudes";
    }

    // Método para eliminar una solicitud
    @GetMapping("/deletsolicitude/{id}")
    public String adminExcluirSolicitud(@PathVariable("id") int id) {
        solicitudeRepository.deleteSolicitudeById((long) id);
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

    // Método para editar un usuario
    @GetMapping("/edit/{id}")
    public ModelAndView adminEditUser(@PathVariable("id") long id) {
        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("admin/edit");

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Obtener la lista de roles
            List<Role> listRoles = userService.listRoles();
            // Agregar el usuario y la lista de roles al modelo
            mv.addObject("user", user);
            mv.addObject("listRoles", listRoles);
            // Agregar los tipos de organización al modelo
            mv.addObject("organizationTypes", OrganizationType.values());
        }
        return mv;
    }

    // Método para procesar la edición de un usuario
    @PostMapping("/edit/{id}")
    public String adminEditUserBanco(@ModelAttribute("user") User user,
                                    @RequestParam(value = "organizationType", required = false) String organizationType,
                                    @RequestParam(value = "organizationName", required = false) String organizationName,
                                    @RequestParam(value = "roles", required = false) String roleValue,
                                    BindingResult result, RedirectAttributes msg) {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/admin/edit/" + user.getId();
        }
        User userEdit = userRepository.findById(user.getId()).orElse(null);

        if (userEdit != null) {
            // Actualizar los datos del usuario con los nuevos valores
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            
            // Gestionar roles - enfoque simplificado
            if (roleValue != null) {
                // Limpiar roles existentes
                userEdit.getRoles().clear();
                
                if ("ROLE_ORGANIZATION".equals(roleValue)) {
                    // Manejar específicamente el rol ORGANIZATION
                    Role organizationRole = roleRepository.findByName("ROLE_ORGANIZATION");
                    if (organizationRole == null) {
                        // Crear el rol si no existe
                        organizationRole = new Role();
                        organizationRole.setName("ROLE_ORGANIZATION");
                        organizationRole = roleRepository.save(organizationRole);
                    }
                    userEdit.getRoles().add(organizationRole);
                } else {
                    try {
                        // Manejar otros roles por ID
                        Long roleId = Long.parseLong(roleValue);
                        Role role = roleRepository.findById(roleId).orElse(null);
                        if (role != null) {
                            userEdit.getRoles().add(role);
                        }
                    } catch (NumberFormatException e) {
                        // Si no es un número, ignorarlo silenciosamente
                    }
                }
            }
            
            // Actualizar tipo y nombre de organización
            if (organizationType != null && !organizationType.isEmpty()) {
                userEdit.setOrganizationType(OrganizationType.valueOf(organizationType));
                userEdit.setOrganizationName(organizationName);
            } else {
                // Si no se selecciona un tipo de organización, establecer ambos campos como nulos
                userEdit.setOrganizationType(null);
                userEdit.setOrganizationName(null);
            }

            // Guardar el usuario actualizado
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado exitosamente.");
            return "redirect:/admin/users";
        }
        
        msg.addFlashAttribute("error", "Usuario no encontrado.");
        return "redirect:/admin/users";
    }

    // Método para eliminar una solicitud
    @GetMapping("/deletuser/{id}")
    public String adminExcluirUser(@PathVariable("id") int id) {
        userService.deleteUserById((long) id);
        return "redirect:/admin/dashboard";
    }
}
