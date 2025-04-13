package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.OrgService;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/org")
public class OrgController extends BaseController {


    @Autowired
    private OrgService orgService;

    @Autowired
    UserService userService;
    @Autowired
    SolicitudeService solicitudeService;
    @Autowired UserRepository userRepository;
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    ValidationAndNotificationService validationAndNotificationService;
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    @GetMapping("/dashboard")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView("user/welcome");

        try {
            // Obtener el usuario actual
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

        } catch (Exception e) {
            mv.addObject("error", "Error al cargar la página: " + e.getMessage());
        }

        return mv;
    }
    
    @GetMapping("/solicitudes")
    public ModelAndView verSolicitudes(@AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la obtención de solicitudes al servicio
        return orgService.getSolicitudes(userDetails);
    }
    
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView editarSolicitud(@PathVariable("id") int id, @AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la preparación de la solicitud para edición al servicio
        return orgService.prepareEditSolicitud(id, userDetails);
    }
    
    @GetMapping("/deletsolicitude/{id}")
    public String eliminarSolicitud(@PathVariable("id") int id, RedirectAttributes redirectAttributes, 
                                   @AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la eliminación de la solicitud al servicio
        return orgService.deleteSolicitud(id, redirectAttributes, userDetails);
    }
    
    @PostMapping("/editsolicitude/{id}")
    public String procesarEditarSolicitud(@ModelAttribute("solicitude") @Validated Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagen,
                                      @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        // Verificar errores de validación
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Por favor, corrija los errores en el formulario.");
            return "redirect:/org/editsolicitude/" + solicitude.getId();
        }
        
        // Delegamos la actualización de la solicitud al servicio
        return orgService.updateSolicitud(solicitude, imagen, msg, userDetails);
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
        ModelAndView mv = new ModelAndView("user/profile");

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
            return new ModelAndView("redirect:/user/profile");
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
                        return new ModelAndView("redirect:/user/profile");
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

        return new ModelAndView("redirect:/user/profile");
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }
}
