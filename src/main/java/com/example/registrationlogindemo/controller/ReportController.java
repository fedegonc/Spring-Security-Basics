package com.example.registrationlogindemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para gestionar las redirecciones de reportes.
 * Este controlador simplemente redirige a las rutas correctas del UserController.
 */
@Controller
@RequestMapping("/reportes")
public class ReportController {

    /**
     * Redirige /reportes/nuevo a /user/reportes/nuevo
     */
    @GetMapping("/nuevo")
    public String redirectToUserReportForm() {
        return "redirect:/user/reportes/nuevo";
    }
}
