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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GuestController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private GuestService guestService;

    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("guest/index");
        
        // Verificar si el usuario está autenticado y redirigir si es necesario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (guestService.isUserAuthenticated(authentication)) {
            return new ModelAndView("redirect:/init");
        }
        
        return mv;
    }

    @GetMapping("/report")
    public ModelAndView newReport() {
        return new ModelAndView("user/report-problem");
    }

    @PostMapping("/report")
    public String newReportPost(
            @Valid Report report,
            BindingResult result,
            RedirectAttributes msg,
            @AuthenticationPrincipal UserDetails currentUser) {
            
        // Delegar la lógica de negocio al servicio
        boolean reportSubmitted = guestService.submitReport(report, result, msg);
        
        // Si hay error de validación, redirigir al formulario
        if (!reportSubmitted && result.hasErrors()) {
            return "redirect:/report/form";
        }
        
        // En cualquier otro caso, redirigir a la página principal
        return "redirect:/init";
    }
}
