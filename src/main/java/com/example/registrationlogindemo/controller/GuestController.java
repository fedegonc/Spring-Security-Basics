package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

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
    public String getIndex() {
        ModelAndView mv = new ModelAndView("/index");
        // Obtener todas las solicitudes activas
        List<Solicitude> solicitude = solicitudeRepository.findSolicitudeByActivo("Activo");
        mv.addObject("solicitude", solicitude);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            // El usuario ya está autenticado, redirigir a la página de inicio correspondiente
            return "redirect:/init";
        }
        return "/index";
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
    @GetMapping("/css/styles.css")
    public ResponseEntity<Resource> getCss() throws IOException {
        // Cargar el archivo CSS desde el sistema de archivos
        Resource resource = (Resource) new ClassPathResource("/static/css/styles.css");

        // Devolver una respuesta con el recurso y el código de estado 200 OK
        return ResponseEntity.ok().body(resource);
    }

}
