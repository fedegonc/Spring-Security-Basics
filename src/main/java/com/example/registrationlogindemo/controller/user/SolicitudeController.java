package com.example.registrationlogindemo.controller.user;

import ch.qos.logback.core.model.Model;
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
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

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

        if (!imagen.isEmpty()) {
            try {
                byte[] bytes = imagen.getBytes();
                Path rutaImagen = Paths.get(UPLOAD_DIR + imagen.getOriginalFilename());
                Files.write(rutaImagen, bytes);
                solicitud.setImagen(imagen.getOriginalFilename());
            } catch (IOException e) {
                msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
                return "redirect:/user/welcome";
            }
        } else {
            solicitud.setImagen(null); // O establece un valor por defecto
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
        } else {
            msg.addFlashAttribute("error", "No se pudo encontrar el usuario actual.");
        }

        return "redirect:/user/welcome";
    }
    // Método para manejar la solicitud GET para editar una solicitud
    @GetMapping("/editsolicitude/{id}")
    public ModelAndView showEditSolicitudeForm(@PathVariable("id") int id) {
        ModelAndView mv = new ModelAndView("solicitude/editsolicitude");
        Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(id);
        if (solicitudeOpt.isPresent()) {
            mv.addObject("solicitude", solicitudeOpt.get());
        } else {
            mv.setViewName("redirect:/user/welcome");
        }
        return mv;
    }

    @PostMapping("/editsolicitude/{id}")
    public ModelAndView editSolicitude(@PathVariable("id") int id,
                                       @ModelAttribute("solicitude") @Valid Solicitude solicitude,
                                       BindingResult result, RedirectAttributes msg,
                                       @RequestParam("file") MultipartFile imagen) {
        ModelAndView mv = new ModelAndView();

        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar. Por favor, complete todos los campos correctamente.");
            mv.setViewName("redirect:/user/editsolicitude/" + id);
            return mv;
        }

        Optional<Solicitude> existingSolicitudeOpt = solicitudeRepository.findById(id);
        if (existingSolicitudeOpt.isPresent()) {
            Solicitude existingSolicitude = existingSolicitudeOpt.get();
            existingSolicitude.setNombre(solicitude.getNombre());
            existingSolicitude.setCategoria(solicitude.getCategoria());
            existingSolicitude.setActivo(solicitude.isActivo());
            existingSolicitude.setDescripcion(solicitude.getDescripcion());
            existingSolicitude.setDiasDisponibles(solicitude.getDiasDisponibles());
            existingSolicitude.setHoraRecoleccion(solicitude.getHoraRecoleccion());
            existingSolicitude.setCalle(solicitude.getCalle());
            existingSolicitude.setBarrio(solicitude.getBarrio());
            existingSolicitude.setNumeroDeCasa(solicitude.getNumeroDeCasa());
            existingSolicitude.setTelefono(solicitude.getTelefono());
            existingSolicitude.setPeso(solicitude.getPeso());
            existingSolicitude.setVolumen(solicitude.getVolumen());

            try {
                if (!imagen.isEmpty()) {
                    byte[] bytes = imagen.getBytes();
                    Path path = Paths.get("./src/main/resources/static/img/" + imagen.getOriginalFilename());
                    Files.write(path, bytes);
                    existingSolicitude.setImagen(imagen.getOriginalFilename());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            solicitudeRepository.save(existingSolicitude);
            msg.addFlashAttribute("exito", "Solicitud editada con éxito.");
            mv.setViewName("redirect:/user/welcome");
        } else {
            msg.addFlashAttribute("error", "No se encontró la solicitud a editar.");
            mv.setViewName("redirect:/user/welcome");
        }

        return mv;
    }

    @GetMapping("/deletesolicitude/{id}")
    public String deleteSolicitude(@PathVariable("id") long id) {
        solicitudeRepository.deleteSolicitudeById(id);
        return "redirect:/user/welcome";
    }

}
