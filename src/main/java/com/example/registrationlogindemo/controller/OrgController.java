package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.OrgService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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

@Controller
@RequestMapping("/org")
public class OrgController {

    @Autowired
    private OrgService orgService;

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
    
    @GetMapping("/profile")
    public ModelAndView viewProfile(@AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos la obtención de datos del perfil al servicio
        return orgService.getProfileData(userDetails);
    }
    
    @PostMapping("/profile")
    public String updateUser(@ModelAttribute("user") User user,
                                  @RequestParam("fileImage") MultipartFile multipartFile,
                                  @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        // Delegamos la actualización del perfil al servicio
        return orgService.updateProfile(user, multipartFile, userDetails);
    }
    
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes attributes,
                                @AuthenticationPrincipal UserDetails userDetails) {
        // Delegamos el cambio de contraseña al servicio
        return orgService.changePassword(currentPassword, newPassword, confirmPassword, attributes, userDetails);
    }
}
