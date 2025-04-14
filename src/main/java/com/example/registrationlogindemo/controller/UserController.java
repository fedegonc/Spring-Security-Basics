package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la gestión de usuarios.
 */
@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired UserService userService;
    @Autowired SolicitudeService solicitudeService;
    @Autowired UserRepository userRepository;
    @Autowired SolicitudeRepository solicitudeRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired ValidationAndNotificationService validationAndNotificationService;
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    @GetMapping("/welcome")
    public String welcomePage(Model model) {
        // Proporcionar datos básicos para mantener la consistencia con el template
        try {
            // Obtener el usuario actual de forma simplificada
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String username = authentication.getName();
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    model.addAttribute("user", user);
                }
            }
            
            // Agregar estadísticas vacías para evitar errores en la plantilla
            Map<String, Integer> stats = new HashMap<>();
            stats.put("activeSolicitudes", 0);
            stats.put("completedSolicitudes", 0);
            stats.put("totalSolicitudes", 0);
            model.addAttribute("stats", stats);
            
            // Lista vacía de solicitudes para evitar errores
            model.addAttribute("solicitudes", new ArrayList<>());
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
        }
        
        return "pages/welcome";
    }

    /**
     * Maneja las operaciones relacionadas con el perfil de usuario.
     * GET: Muestra el perfil del usuario.
     * POST: Actualiza la información del perfil.
     */
    @RequestMapping(value = "/profile", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView handleProfile(
            @Valid @ModelAttribute(binding = false) User user,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "currentProfileImageUrl", required = false, defaultValue = "descargas.jpeg") String currentImg,
            RedirectAttributes msg,
            jakarta.servlet.http.HttpServletRequest request) {
        
        // Si es una solicitud GET, mostrar el perfil
        if (request.getMethod().equalsIgnoreCase("GET")) {
            return viewProfile();
        }
        
        // Si es una solicitud POST, actualizar el perfil
        return updateProfile(user, result, file, currentImg, msg);
    }

    /**
     * Método privado para mostrar el perfil del usuario.
     */
    private ModelAndView viewProfile() {
        ModelAndView mv = new ModelAndView("pages/profile");
        
        try {
            // Obtener el usuario actual
            User user = getAuthenticatedUser();
            
            if (user != null) {
                // Si el usuario no tiene imagen de perfil, usar una por defecto
                if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                    user.setProfileImage("descargas.jpeg");
                }
                
                mv.addObject("user", user);
                
                // Establecer el nombre de la página actual para los breadcrumbs en el header
                mv.addObject("currentPage", "Perfil");
            } else {
                return new ModelAndView("redirect:/login");
            }
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el perfil: " + e.getMessage());
        }
        
        return mv;
    }

    /**
     * Método privado para actualizar el perfil del usuario.
     */
    private ModelAndView updateProfile(@Valid User user, BindingResult result,
                                  MultipartFile file, String currentImg,
                                  RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Por favor, corrija los errores en el formulario");
            return new ModelAndView("redirect:/pages/profile");
        }
        
        try {
            // Obtener el usuario actual
            User currentUser = getAuthenticatedUser();
            
            if (currentUser != null) {
                // Actualizar solo los campos permitidos
                currentUser.setName(user.getName());
                currentUser.setEmail(user.getEmail());
                
                // Manejar la subida de imagen
                if (file != null && !file.isEmpty()) {
                    try {
                        String fileName = handleImageUpload(file, currentImg);
                        currentUser.setProfileImage(fileName);
                    } catch (IOException e) {
                        msg.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                        return new ModelAndView("redirect:/pages/profile");
                    }
                }
                
                // Guardar los cambios
                userRepository.save(currentUser);
                
                msg.addFlashAttribute("success", "Perfil actualizado con éxito");
            } else {
                msg.addFlashAttribute("error", "Usuario no encontrado");
            }
        } catch (Exception e) {
            msg.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return new ModelAndView("redirect:/pages/profile");
    }

    @DeleteMapping("/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.eliminarEntidad(id);
        return "redirect:/logout";
    }

    @GetMapping("/logout")
    public String logout() { return "index"; }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    @GetMapping({"/construction", "/contacto", "/informaciones", "/statistics", "/materiales"})
    public ModelAndView staticPages(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView("user/" + req.getRequestURI().split("/")[2]);
        mv.addObject("user", getAuthenticatedUser());
        
        // Establecer el nombre de la página actual para los breadcrumbs
        String pageName = req.getRequestURI().split("/")[2];
        String displayName = Character.toUpperCase(pageName.charAt(0)) + pageName.substring(1);
        mv.addObject("currentPage", displayName);
        
        return mv;
    }

    @GetMapping("/view-requests")
    public String viewRequests(Model model) {
        // Proporcionar datos básicos para mantener la consistencia con el template
        try {
            // Obtener el usuario actual de forma simplificada
            User user = getAuthenticatedUser();
            if (user != null) {
                // Asegurar que el usuario tenga una imagen de perfil predeterminada si es null
                if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                    user.setProfileImage("descargas.jpeg");
                }
                model.addAttribute("user", user);
                
                // Obtener las solicitudes del usuario
                List<Solicitude> solicitudes = solicitudeRepository.findByUser(user);
                if (solicitudes == null) {
                    solicitudes = new ArrayList<>();
                }
                model.addAttribute("solicitudes", solicitudes);
                
                // Calcular estadísticas básicas
                Map<String, Integer> stats = new HashMap<>();
                int activeSolicitudes = 0;
                int completedSolicitudes = 0;
                
                for (Solicitude solicitude : solicitudes) {
                    if (solicitude.getEstado() != null && solicitude.getEstado().equals("COMPLETADO")) {
                        completedSolicitudes++;
                    } else if (solicitude.isActivo()) {
                        activeSolicitudes++;
                    }
                }
                
                stats.put("activeSolicitudes", activeSolicitudes);
                stats.put("completedSolicitudes", completedSolicitudes);
                stats.put("totalSolicitudes", solicitudes.size());
                model.addAttribute("stats", stats);
            }
            
            // Establecer el nombre de la página actual para los breadcrumbs en el header
            model.addAttribute("currentPage", "Mis Solicitudes");
            
        } catch (Exception e) {
            // Asegurar que siempre haya un objeto stats para evitar errores en la vista
            Map<String, Integer> emptyStats = new HashMap<>();
            emptyStats.put("activeSolicitudes", 0);
            emptyStats.put("completedSolicitudes", 0);
            emptyStats.put("totalSolicitudes", 0);
            model.addAttribute("stats", emptyStats);
            model.addAttribute("solicitudes", new ArrayList<>());
            
            model.addAttribute("error", "Error al cargar la página: " + e.getMessage());
        }
        
        return "pages/view-requests";
    }

    @PostMapping("/updatesolicitude/{id}")
    public String updateSolicitude(@PathVariable("id") int id, 
                                @RequestParam String categoria,
                                @RequestParam String barrio, 
                                @RequestParam String calle,
                                @RequestParam String numeroDeCasa, 
                                @RequestParam MultipartFile file,
                                @RequestParam String currentImageUrl, 
                                RedirectAttributes msg) throws IOException {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // Crear objeto solicitud con los datos actualizados
        Solicitude solicitude = new Solicitude();
        solicitude.setId(id);
        solicitude.setCategoria(categoria);
        solicitude.setBarrio(barrio);
        solicitude.setCalle(calle); 
        solicitude.setNumeroDeCasa(numeroDeCasa);
        
        // Delegar la actualización al servicio consolidado
        solicitudeService.updateSolicitude(id, solicitude, file, msg, userDetails);
        
        return "redirect:/pages/view-requests";
    }

    @GetMapping("/report-problem")
    public String reportProblemForm() {
        return "pages/report-problem";
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
            return "redirect:/pages/welcome";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Ocurrió un error al reportar el problema: " + e.getMessage());
            return "redirect:/pages/report-problem";
        }
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes attributes) {
        
        return super.changePassword(currentPassword, newPassword, confirmPassword, attributes, "/pages/profile");
    }
    
    @Override
    protected UserService getUserService() {
        return userService;
    }
}
