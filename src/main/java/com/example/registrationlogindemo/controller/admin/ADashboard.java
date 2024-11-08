package com.example.registrationlogindemo.controller.admin;

import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.ImageService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ADashboard {

    @Autowired
    ImageService imageService;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    UserService userService;


    // Método para mostrar el dashboard
    @GetMapping("/dashboard")
    public ModelAndView adminDashboard() {
        ModelAndView mv = new ModelAndView("admin/dashboard");

        Optional<Image> uruguaiImage = imageRepository.findByNombre("uruguai.png");
        Optional<Image> brasilImage = imageRepository.findByNombre("brasil.png");
        // Agregar imágenes de idioma al modelo usando el servicio
        imageService.addLanguageImages(mv, uruguaiImage, "uruguaiImageName");
        imageService.addLanguageImages(mv, brasilImage, "brasilImageName");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());
        return mv;
    }
}
