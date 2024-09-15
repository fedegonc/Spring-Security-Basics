package com.example.registrationlogindemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SostenibilidadController {

    @GetMapping("/sostenibilidad")
    public String showSostenibilidadPage(Model model) {
        // Puedes agregar atributos al modelo aquí si lo necesitas
        return "sostenibilidad";  // Nombre del archivo HTML en /templates/sostenibilidad.html
    }
}
