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
import java.util.List;

/**
 * Controlador para gestionar las operaciones de organizaciones
 */
@Controller
@RequestMapping("/org")
public class OrgController extends BaseController {

    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    private static final String ERROR_CARGAR_PAGINA = "Error al cargar la página: ";
    private static final String ERROR_ACTUALIZAR_PERFIL = "Error al actualizar el perfil: ";
    private static final String SUCCESS_PERFIL = "Perfil actualizado con éxito";
    
    @Autowired private OrgService orgService;
    @Autowired private UserService userService;
    @Autowired private SolicitudeService solicitudeService;
    @Autowired private UserRepository userRepository;
    @Autowired private SolicitudeRepository solicitudeRepository;
    @Autowired private ReportRepository reportRepository;
    @Autowired private ValidationAndNotificationService validationService;

    /**
     * Obtiene el usuario autenticado actualmente
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    /**
     * Página de inicio para organizaciones
     * Muestra estadísticas y solicitudes relevantes
     */
    @GetMapping("/inicio")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView("pages/org/inicio");

        try {
            User currentUser = getAuthenticatedUser();
            
            if (currentUser != null) {
                List<Solicitude> solicitudes = solicitudeService.getSolicitudesByUser(currentUser);
                
                // Agregar solicitudes y estadísticas al modelo
                mv.addObject("solicitudes", solicitudes);
                addSolicitudeStatistics(mv, solicitudes);
            }
        } catch (Exception e) {
            mv.addObject("error", ERROR_CARGAR_PAGINA + e.getMessage());
        }

        return mv;
    }
    
    /**
     * Muestra las solicitudes de la organización actual
     */
    @GetMapping("/solicitudes")
    public ModelAndView verSolicitudes(@AuthenticationPrincipal UserDetails userDetails) {
        return orgService.getSolicitudes(userDetails);
    }

    /**
     * Muestra el formulario para editar una solicitud
     */
    @GetMapping("/solicitudes/editar/{id}")
    public ModelAndView editarSolicitud(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return orgService.prepareEditSolicitud(id, userDetails);
    }

    /**
     * Elimina una solicitud
     */
    @GetMapping("/solicitudes/eliminar/{id}")
    public String eliminarSolicitud(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, 
                                   @AuthenticationPrincipal UserDetails userDetails) {
        return orgService.deleteSolicitud(id, redirectAttributes, userDetails);
    }

    /**
     * Procesa la edición de una solicitud
     */
    @PostMapping("/solicitudes/editar")
    public String procesarEditarSolicitud(@ModelAttribute("solicitude") @Validated Solicitude solicitude,
                                      BindingResult result, RedirectAttributes msg,
                                      @RequestParam("file") MultipartFile imagen,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, "Por favor corrija los errores en el formulario");
            return "redirect:/org/solicitudes/editar/" + solicitude.getId();
        }
        
        try {
            return orgService.updateSolicitud(solicitude, imagen, msg, userDetails);
        } catch (IOException e) {
            validationService.addErrorMessage(msg, "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/org/solicitudes/editar/" + solicitude.getId();
        }
    }

    /**
     * Maneja las operaciones de perfil (GET: mostrar, POST: actualizar)
     */
    @RequestMapping(value = "/profile", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView handleProfile(
            @Valid @ModelAttribute(binding = false) User user,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "currentProfileImageUrl", required = false, defaultValue = "descargas.jpeg") String currentImg,
            RedirectAttributes msg,
            jakarta.servlet.http.HttpServletRequest request) {

        return request.getMethod().equalsIgnoreCase("GET") 
               ? viewProfile() 
               : updateProfile(user, result, file, currentImg, msg);
    }

    /**
     * Muestra el perfil del usuario
     */
    private ModelAndView viewProfile() {
        ModelAndView mv = new ModelAndView("user/profile");

        try {
            User user = getAuthenticatedUser();

            if (user == null) {
                return new ModelAndView("redirect:/login");
            }
            
            // Asegurar que siempre haya una imagen de perfil
            if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
                user.setProfileImage("descargas.jpeg");
            }

            mv.addObject("user", user);
            mv.addObject("currentPage", "Perfil");
            
        } catch (Exception e) {
            mv.addObject("error", "Error al cargar el perfil: " + e.getMessage());
        }

        return mv;
    }

    /**
     * Actualiza el perfil del usuario
     */
    private ModelAndView updateProfile(
            @Valid User user, 
            BindingResult result,
            MultipartFile file, 
            String currentImg,
            RedirectAttributes msg) {
            
        if (result.hasErrors()) {
            validationService.addErrorMessage(msg, "Por favor, corrija los errores en el formulario");
            return new ModelAndView("redirect:/user/profile");
        }

        try {
            User currentUser = getAuthenticatedUser();

            if (currentUser == null) {
                validationService.addErrorMessage(msg, "Usuario no encontrado");
                return new ModelAndView("redirect:/login");
            }

            // Actualizar datos básicos
            currentUser.setName(user.getName());
            currentUser.setEmail(user.getEmail());

            // Procesar imagen si se proporcionó una nueva
            processProfileImage(file, currentImg, currentUser);

            // Guardar cambios
            userRepository.save(currentUser);
            validationService.addSuccessMessage(msg, SUCCESS_PERFIL);
            
        } catch (Exception e) {
            validationService.addErrorMessage(msg, ERROR_ACTUALIZAR_PERFIL + e.getMessage());
        }

        return new ModelAndView("redirect:/user/profile");
    }
    
    /**
     * Procesa la imagen de perfil
     */
    private void processProfileImage(MultipartFile file, String currentImg, User user) {
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = handleImageUpload(file, currentImg);
                user.setProfileImage(fileName);
            } catch (IOException e) {
                // La excepción se propaga hacia arriba para ser manejada por el método llamador
                throw new RuntimeException("Error al procesar la imagen: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Agrega estadísticas de solicitudes al modelo
     */
    private void addSolicitudeStatistics(ModelAndView mv, List<Solicitude> solicitudes) {
        long pendientes = solicitudes.stream().filter(s -> "EN_ESPERA".equals(s.getEstado())).count();
        long aprobadas = solicitudes.stream().filter(s -> "ACEPTADA".equals(s.getEstado())).count();
        long rechazadas = solicitudes.stream().filter(s -> "RECHAZADA".equals(s.getEstado())).count();
        
        mv.addObject("totalSolicitudes", solicitudes.size());
        mv.addObject("pendientes", pendientes);
        mv.addObject("aprobadas", aprobadas);
        mv.addObject("rechazadas", rechazadas);
    }

    @Override
    protected UserService getUserService() {
        return userService;
    }
}
