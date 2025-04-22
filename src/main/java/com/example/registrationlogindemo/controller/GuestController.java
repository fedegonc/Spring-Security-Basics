package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.service.GuestService;
import com.example.registrationlogindemo.service.ReportService;
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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para gestionar las páginas públicas accesibles sin autenticación
 */
@Controller
public class GuestController {

    @Autowired private ReportService reportService;
    @Autowired private UserService userService;
    @Autowired private GuestService guestService;

    /**
     * Página de inicio para visitantes
     * Redirige a usuarios autenticados a su página correspondiente
     */
    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        // Verificar si el usuario está autenticado y redirigir si es necesario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (guestService.isUserAuthenticated(authentication)) {
            return new ModelAndView("redirect:/init");
        }
        
        return new ModelAndView("pages/guest/index");
    }

    /**
     * Página de demostración del sistema de layouts
     */
    @GetMapping("/layoutdemo")
    public String layoutDemo(Model model) {
        // Usar el enfoque simple para indicar la página actual
        model.addAttribute("currentPage", "Demo de Layout");
        
        // Agregar mensaje de ejemplo para probar alertas
        model.addAttribute("successMessage", "Esta es una demostración de mensaje de éxito");
        
        return "pages/guest/demo-layout";
    }

    /**
     * Muestra el formulario para reportar problemas
     */
    @GetMapping("/report")
    public ModelAndView newReport() {
        ModelAndView mv = new ModelAndView("pages/report-problem");
        mv.addObject("report", new Report());
        return mv;
    }

    /**
     * Procesa el envío de un reporte de problema
     */
    @PostMapping("/report")
    public String newReportPost(
            @Valid @ModelAttribute("report") Report report,
            BindingResult result,
            RedirectAttributes msg,
            @AuthenticationPrincipal UserDetails currentUser) {
            
        boolean reportSubmitted = guestService.submitReport(report, result, msg);
        
        if (!reportSubmitted && result.hasErrors()) {
            return "redirect:/report";
        }
        
        msg.addFlashAttribute("success", "Su reporte ha sido enviado correctamente. Gracias por su colaboración.");
        return "redirect:/";
    }
}
