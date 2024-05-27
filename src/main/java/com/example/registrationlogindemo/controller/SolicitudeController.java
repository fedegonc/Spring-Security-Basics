package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class SolicitudeController {
    @Autowired
    SolicitudeRepository solicitudeRepository;
    @Autowired
    UserRepository userRepository;

    private UserService userService;

    // Constructor que inyecta el servicio UserService
    public void UserController(UserService userService) {
        this.userService = userService;
    }

    public SolicitudeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/newsolicitude")
    public ModelAndView newSolicitude() {
        ModelAndView mv = new ModelAndView("solicitude/newsolicitude");
        return mv;
    }

    @PostMapping("/newsolicitude")
    public String newSolicitudePost(@Valid Solicitude solicitud,
                                    BindingResult result, RedirectAttributes msg,
                                    @RequestParam("file") MultipartFile imagen,
                                    @AuthenticationPrincipal UserDetails currentUser) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al iniciar solicitud. Por favor, llenar todos los campos");
            return "redirect:/user/welcome";
        }

        try {
            if (!imagen.isEmpty()) {
                byte[] bytes = imagen.getBytes();
                Path caminho = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                Files.write(caminho, bytes);
                solicitud.setImagen(imagen.getOriginalFilename());
            }
        } catch (IOException e) {
            System.out.println("Error al salvar imagen");
        }

        User user = userRepository.findByUsername(currentUser.getUsername());

        if (user != null) {
            solicitud.setUser(user);
            solicitudeRepository.save(solicitud);
            msg.addFlashAttribute("exito", "Solicitud realizada con éxito.");
            return "redirect:/user/welcome";
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
            return "redirect:/user/welcome";
        }
    }
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView editSolicitude(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView();
        Optional<Solicitude> solicitudeOptional = solicitudeRepository.findById(id);

        if (solicitudeOptional.isPresent()) {
            Solicitude solicitude = solicitudeOptional.get();
            mv.addObject("solicitude", solicitude);
            mv.setViewName("solicitude/editsolicitude"); // Nombre de la vista de edición
        } else {
            mv.setViewName("redirect:/error"); // Redirige a la página de error si la solicitud no existe
        }

        return mv;
    }

    @PostMapping("/editsolicitude/{id}")
    public ModelAndView editSolicitudeBanco(@PathVariable("id") int id, @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                            BindingResult result, RedirectAttributes msg,
                                            @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("erro", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/solicitude/editsolicitude/" + id); // Redirige de vuelta al formulario de edición si hay errores
            return mv;
        }

        Solicitude changeSolicitude = solicitudeRepository.findById(solicitude.getId()).orElse(null);
        if (changeSolicitude != null) {
            changeSolicitude.setNombre(solicitude.getNombre());
            changeSolicitude.setCategoria(solicitude.getCategoria());

            changeSolicitude.setDescripcion(solicitude.getDescripcion());

            try {
                if (!imagen.isEmpty()) {
                    byte[] bytes = imagen.getBytes();
                    Path caminho = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                    Files.write(caminho, bytes);
                    changeSolicitude.setImagen(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                System.out.println("Error de imagen");
            }
            solicitudeRepository.save(changeSolicitude);
            msg.addFlashAttribute("sucesso", "Solicitud editada con éxito.");
            mv.setViewName("redirect:/user/welcome"); // Redirige al dashboard después de editar la solicitud
        } else {
            mv.setViewName("redirect:/error"); // Redirige a la página de error si la solicitud no se encuentra
        }

        return mv;
    }

    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") int id) {
        solicitudeRepository.deleteById(id);
        return "redirect:/user/welcome";
    }
/*
    @GetMapping("/modifystate/{id}")
    public String modifyStateSolicitude(@PathVariable("id") int id) {
        Solicitude solicitude = solicitudeRepository.findById(id).orElse(null);
        if (solicitude != null) {
            if ("Activo".equals(solicitude.getActivo())) {
                solicitude.setActivo("Inactivo");
            } else {
                solicitude.setActivo("Activo");
            }
            solicitudeRepository.save(solicitude);
        }
        return "redirect:/user/welcome";
    }*/
}
