package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

@Controller
@RequestMapping("/org")
public class OrgController {

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
    private RoleRepository roleRepository;

    @GetMapping("/dashboard")
    public ModelAndView organizationDashboard() {
        ModelAndView mv = new ModelAndView("org/dashboard");

        try {
            // Obtener el usuario actualmente autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();

                // Obtener el usuario de la base de datos
                User usuario = userRepository.findByUsername(username);
                mv.addObject("user", usuario);

                // Obtener solicitudes pendientes para la organización
                List<Solicitude> solicitudesPendientes = solicitudeService.getSolicitudesPendientes();
                mv.addObject("solicitudesPendientes", solicitudesPendientes);

                // Agregar información adicional a la vista
                mv.addObject("username", username);
            }
        } catch (Exception e) {
            mv.addObject("error", "Ha ocurrido un error: " + e.getMessage());
        }

        return mv;
    }
    
    @GetMapping("/solicitudes")
    public ModelAndView verSolicitudes() {
        ModelAndView mv = new ModelAndView("admin/solicitudes");
        
        try {
            // Obtener el usuario actualmente autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String username = userDetails.getUsername();
                
                // Obtener todas las solicitudes
                List<Solicitude> solicitudes = solicitudeService.findAll();
                mv.addObject("solicitudes", solicitudes);
                mv.addObject("username", username);
                // Indicar que estamos en el rol de organización para ajustar los enlaces en la plantilla
                mv.addObject("isOrganizacion", true);
            }
        } catch (Exception e) {
            mv.addObject("error", "Ha ocurrido un error al cargar las solicitudes: " + e.getMessage());
        }
        
        return mv;
    }
    
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView editarSolicitud(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("admin/editsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
            // Indicar que estamos en el rol de organización
            mv.addObject("isOrganizacion", true);
        } else {
            mv.setViewName("redirect:/error");
        }
        return mv;
    }
    
    @GetMapping("/deletsolicitude/{id}")
    public String eliminarSolicitud(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);
            if (solicitudeOptional.isPresent()) {
                // Eliminar la solicitud
                solicitudeRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("success", "Solicitud eliminada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se encontró la solicitud con ID: " + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la solicitud: " + e.getMessage());
        }
        
        return "redirect:/org/solicitudes";
    }
    
    @PostMapping("/editsolicitude/{id}")
    public String procesarEditarSolicitud(@ModelAttribute("solicitude") @Validated Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagem) {
        if (result.hasErrors()) {
            // Si hay errores de validación, redirige con un mensaje de error
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            return "redirect:/org/editsolicitude/" + solicitude.getId();
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
            
            // Actualizar el estado si es necesario
            changeSolicitude.setEstado(solicitude.getEstado());

            try {
                // Guardar la imagen si se proporciona una
                if (!imagem.isEmpty()) {
                    byte[] bytes = imagem.getBytes();
                    Path caminho = Paths.get("src/main/resources/static/img/" + imagem.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagem.getOriginalFilename());
                }
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen: " + e.getMessage());
            }
            // Guardar la solicitud editada en la base de datos
            solicitudeRepository.save(changeSolicitude);
            // Redirigir con un mensaje
            msg.addFlashAttribute("success", "Solicitud actualizada correctamente");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud con ID: " + solicitude.getId());
        }
        
        return "redirect:/org/solicitudes";
    }

    @GetMapping("/profile")
    public ModelAndView viewProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null) {
            return new ModelAndView("redirect:/org/profile/" + currentUser.getId());
        } else {
            return new ModelAndView("redirect:/error");
        }
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/org/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        ModelAndView mv = new ModelAndView("org/profile");
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
    public ModelAndView updateUser(@PathVariable("id") long id,
                                  @ModelAttribute("user") User user,
                                  @RequestParam("fileImage") MultipartFile multipartFile) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser != null && currentUser.getId() != id) {
            return new ModelAndView("redirect:/org/profile/" + currentUser.getId());
        }

        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Actualizar datos básicos
            existingUser.setUsername(user.getUsername());
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());

            // Procesar imagen si se ha subido una nueva
            if (!multipartFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
                existingUser.setProfileImage(fileName);

                // Guardar la imagen en el sistema de archivos
                String uploadDir = "src/main/resources/static/img/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (java.io.InputStream inputStream = multipartFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new IOException("No se pudo guardar el archivo de imagen: " + fileName, e);
                }
            }

            // Guardar los cambios
            userRepository.save(existingUser);

            // Redirigir con mensaje de éxito
            ModelAndView mv = new ModelAndView("redirect:/org/profile/" + id);
            mv.addObject("successMessage", "Perfil actualizado correctamente");
            return mv;
        } else {
            // Usuario no encontrado
            return new ModelAndView("redirect:/error");
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes attributes) {
        
        // Verificar que las contraseñas nuevas coincidan
        if (!newPassword.equals(confirmPassword)) {
            attributes.addFlashAttribute("error", "La nueva contraseña y la confirmación no coinciden");
            return "redirect:/org/profile";
        }
        
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            attributes.addFlashAttribute("error", "Usuario no autenticado");
            return "redirect:/login";
        }
        
        User currentUser = userRepository.findByUsername(authentication.getName());
        if (currentUser == null) {
            attributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/login";
        }
        
        // Cambiar la contraseña
        boolean success = userService.changePassword(currentUser, currentPassword, newPassword);
        
        if (success) {
            attributes.addFlashAttribute("success", "Contraseña actualizada correctamente");
        } else {
            attributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
        }
        
        return "redirect:/org/profile";
    }
}
