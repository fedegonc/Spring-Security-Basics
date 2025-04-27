package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ReportRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar reportes de problemas.
 * Este controlador maneja tanto la visualización del formulario como el procesamiento de reportes.
 */
@Controller
@RequestMapping("/reportes")
public class ReportController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReportRepository reportRepository;

    /**
     * Muestra el formulario para reportar un problema
     */
    @GetMapping("/nuevo")
    public String showReportForm(Model model) {
        // Obtener el usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
        
        // Agregar el usuario al modelo
        model.addAttribute("user", user);
        
        // Devolver directamente la vista en lugar de redirigir
        return "pages/user/report-problem";
    }
    
    /**
     * Procesa el envío de un reporte de problema
     */
    @PostMapping("/enviar")
    public String submitReport(
            @RequestParam("problema") String problema, 
            @RequestParam("descripcion") String descripcion,
            RedirectAttributes attributes) {
        
        try {
            // Obtener el usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
            
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
            
            // Redirección basada en el rol del usuario
            if (hasRole(currentUser, "ADMIN")) {
                return "redirect:/admin/inicio";
            } else if (hasRole(currentUser, "ORGANIZATION")) {
                return "redirect:/org/inicio";
            } else {
                return "redirect:/user/inicio";
            }
        } catch (Exception e) {
            attributes.addFlashAttribute("error", "Ocurrió un error al reportar el problema: " + e.getMessage());
            return "redirect:/reportes/nuevo";
        }
    }
    
    /**
     * Verifica si un usuario tiene un rol específico
     */
    private boolean hasRole(User user, String roleName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_" + roleName));
    }
}
