package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/asociacion")
public class AsociacionController {

    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;

    private UserService userService;

    // Constructor que inyecta el servicio UserService
    public void UserController(UserService userService) {
        this.userService = userService;
    }

    public AsociacionController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getDashboardAsociacion() {
        ModelAndView mv = new ModelAndView("asociacion/dashboard");

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mv.addObject("principal", principal.toString());

        List<Solicitude> solicitudes = solicitudeRepository.findByDestinoContaining("asociacion");
        mv.addObject("solicitudes", solicitudes);

        List<User> users = userRepository.findAll();
        mv.addObject("users", users);

        return mv;
    }

    @GetMapping("/reviewsolicitude/{id}")
    public ModelAndView reviewSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("asociacion/reviewsolicitude");
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById( id);

        if (solicitudeOptional.isPresent()) {
            mv.addObject("solicitude", solicitudeOptional.get());
        } else {
            mv.setViewName("redirect:/asociacion/dashboard");
        }
        return mv;
    }
    @PostMapping("/reviewsolicitude/{id}")
    public ModelAndView editSolicitude(@PathVariable("id") int id,
                                       @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, RedirectAttributes msg,
                                       @RequestParam("estado") String estado) {
        ModelAndView mv = new ModelAndView();

        Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(id);
        if (existingSolicitudeOpt.isPresent()) {
            Solicitude existingSolicitude = existingSolicitudeOpt.get();
            existingSolicitude.setEstado(Solicitude.Estado.valueOf(estado));

            solicitudeRepository.save(existingSolicitude);
            msg.addFlashAttribute("exito", "Estado de la solicitud editado con éxito.");
            mv.setViewName("redirect:/asociacion/dashboard");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
            mv.setViewName("redirect:/asociacion/dashboard");
        }

        return mv;
    }

}
