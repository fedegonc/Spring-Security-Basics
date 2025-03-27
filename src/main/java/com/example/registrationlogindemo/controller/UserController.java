package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

import java.io.File;
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

    @Autowired UserService userService;
    @Autowired SolicitudeService solicitudeService;
    @Autowired UserRepository userRepository;
    @Autowired SolicitudeRepository solicitudeRepository;
    @Autowired ReportRepository reportRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    @GetMapping("/welcome")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView("user/welcome");
        User usuario = getAuthenticatedUser();
        if (usuario != null) {
            mv.addObject("solicitude", solicitudeService.getSolicitudesByUser(usuario));
            mv.addObject("user", usuario);
            mv.addObject("username", usuario.getUsername());
        }
        return mv;
    }

    @GetMapping("/profile")
    public ModelAndView editUser() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (currentUser.getProfileImage() == null || currentUser.getProfileImage().isEmpty()) {
            currentUser.setProfileImage("descargas.jpeg");
        }

        return new ModelAndView("user/profile").addObject("user", currentUser);
    }

    @PostMapping("/profile")
    public ModelAndView updateUser(@Valid User user, BindingResult result,
                                   @RequestParam(value = "file", required = false) MultipartFile file, 
                                   @RequestParam(value = "currentProfileImageUrl", required = false, defaultValue = "descargas.jpeg") String currentImg,
                                   RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar usuario.");
            return new ModelAndView("redirect:/user/profile");
        }

        try {
            User currentUser = getAuthenticatedUser();
            if (currentUser != null) {
                currentUser.setUsername(user.getUsername());
                currentUser.setName(user.getName());
                currentUser.setEmail(user.getEmail());
                
                // Manejo de imagen de perfil
                try {
                    if (file != null && !file.isEmpty()) {
                        String filename = file.getOriginalFilename().replace(" ", "_");
                        // Aseguramos que el directorio existe
                        File uploadDir = new File(UPLOAD_DIR);
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs();
                        }
                        Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());
                        currentUser.setProfileImage(filename);
                    } else {
                        // Si no hay nueva imagen, mantener la imagen actual
                        currentUser.setProfileImage(currentImg);
                    }
                } catch (IOException e) {
                    msg.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
                    // Si hay error con la imagen, mantener la imagen actual
                    currentUser.setProfileImage(currentImg);
                }
                
                userRepository.save(currentUser);
                msg.addFlashAttribute("success", "Usuario editado correctamente.");
            } else {
                msg.addFlashAttribute("error", "Usuario no encontrado.");
            }
        } catch (Exception e) {
            msg.addFlashAttribute("error", "Error al actualizar el usuario: " + e.getMessage());
        }
        
        return new ModelAndView("redirect:/user/profile");
    }

    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.eliminarEntidad(id);
        return "redirect:/logout";
    }

    @GetMapping("/logout") public String logout() { return "index"; }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    @GetMapping({"/construction", "/contacto", "/informaciones", "/statistics", "/materiales"})
    public ModelAndView staticPages(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView("user/" + req.getRequestURI().split("/")[2]);
        mv.addObject("user", getAuthenticatedUser());
        return mv;
    }

    @GetMapping("/view-requests")
    public ModelAndView viewRequests() {
        User user = getAuthenticatedUser();
        List<Solicitude> solicitudes = solicitudeRepository.findByUser(user);
        if (user != null && (user.getProfileImage() == null || user.getProfileImage().isEmpty()))
            user.setProfileImage("descargas.jpeg");
        return new ModelAndView("user/view-requests").addObject("solicitudes", solicitudes).addObject("user", user);
    }

    @PostMapping("/updatesolicitude/{id}")
    public ModelAndView updateSolicitude(@PathVariable("id") int id, @RequestParam String categoria,
                                         @RequestParam String barrio, @RequestParam String calle,
                                         @RequestParam String numeroDeCasa, @RequestParam MultipartFile file,
                                         @RequestParam String currentImageUrl, RedirectAttributes msg) {
        User user = getAuthenticatedUser();
        Optional<Solicitude> opt = solicitudeRepository.findById(id);
        if (opt.isPresent() && opt.get().getUser().getId() == user.getId()) {
            Solicitude s = opt.get();
            s.setCategoria(categoria); s.setBarrio(barrio); s.setCalle(calle); s.setNumeroDeCasa(numeroDeCasa);
            try {
                if (!file.isEmpty()) {
                    String filename = file.getOriginalFilename().replace(" ", "_");
                    Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());
                    s.setImagen(filename);
                } else s.setImagen(currentImageUrl);
                solicitudeRepository.save(s);
                msg.addFlashAttribute("success", "Solicitud actualizada");
            } catch (IOException e) { msg.addFlashAttribute("error", "Error con imagen"); }
        }
        return new ModelAndView("redirect:/user/view-requests");
    }

    @GetMapping("/report-problem")
    public String reportProblemForm() {
        return "user/report-problem";
    }
    
    @PostMapping("/report")
    public String submitReport(@RequestParam("problema") String problema, 
                               @RequestParam("descripcion") String descripcion,
                               RedirectAttributes attributes) {
        try {
            User currentUser = getAuthenticatedUser();
            if (currentUser == null) {
                attributes.addFlashAttribute("error", "Debes iniciar sesión para reportar un problema");
                return "redirect:/login";
            }
            
            // Crear y guardar el nuevo reporte
            Report report = new Report();
            report.setProblema(problema);
            report.setDescripcion(descripcion);
            report.setUser(currentUser);
            
            reportRepository.save(report);
            
            attributes.addFlashAttribute("success", "El problema ha sido reportado correctamente");
            return "redirect:/user/welcome";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Ocurrió un error al reportar el problema: " + e.getMessage());
            return "redirect:/user/report-problem";
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
            return "redirect:/user/profile";
        }
        
        // Obtener el usuario autenticado
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            attributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/login";
        }
        
        // Cambiar la contraseña
        if (userService.changePassword(currentUser, currentPassword, newPassword)) {
            attributes.addFlashAttribute("success", "La contraseña se ha actualizado correctamente");
        } else {
            attributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
        }
        
        return "redirect:/user/profile";
    }
}
