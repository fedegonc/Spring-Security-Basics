package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
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

import java.util.Optional;

@Controller
public class GuestController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @GetMapping({"", "/", "/index"})
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("guest/index");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && authentication.getPrincipal().equals("anonymousUser"));
        if (isAuthenticated) {
            return new ModelAndView("redirect:/init");
        }
        return mv;
    }

    @GetMapping("/ambiental")
    public ModelAndView ambiental() {
        ModelAndView mv = new ModelAndView("guest/ambiental");
        return mv;
    }

    @GetMapping("/report")
    public ModelAndView newReport() {
        return  new ModelAndView("user/report-problem");
    }

    @PostMapping("/report")
    public String newReportPost(
            @Valid Report report,
            BindingResult result,
            RedirectAttributes msg,
            @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "El formulario contiene errores. Por favor, corrige los campos.");
            return "redirect:/report/form"; 
        }
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        if (authenticatedUserOpt.isPresent()) {
            report.setUser(authenticatedUserOpt.get());
            reportService.saveReport(report);
            msg.addFlashAttribute("exito", "Reporte realizado con éxito.");
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }
        return "redirect:/init";
    }
}
