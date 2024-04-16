package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

@Controller
public class GuestController {
    @Autowired
    SolicitudeRepository solicitudeRepository;

    // Redirecciona a la página de inicio
    @GetMapping("")
    public String redirectToIndex() {
        return "index";
    }

    // Redirecciona a la página de inicio
    @GetMapping("/")
    public String redirect() {
        return "index";
    }

    // Obtiene la página de inicio y muestra las solicitudes activas
    @GetMapping(value = "/index")
    public ModelAndView getIndex() {
        ModelAndView mv = new ModelAndView("/index");
        // Obtener todas las solicitudes activas
        List<Solicitude> solicitude = solicitudeRepository.findSolicitudeByActivo("Activo");
        mv.addObject("solicitude", solicitude);
        return mv;
    }

    // Obtiene la imagen según el nombre de archivo proporcionado
    @GetMapping(value = "/imagem/{imagem}")
    @ResponseBody
    public byte[] getImagens(@PathVariable("imagem") String imagem) throws IOException {
        // Obtener la imagen de la ubicación especificada
        File caminho = new File("./src/main/resources/static/img/" + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(caminho.toPath());
        }
        return null;
    }

    @GetMapping("/international")
    public String getInternationalPage() {
        return "index";
    }
}
