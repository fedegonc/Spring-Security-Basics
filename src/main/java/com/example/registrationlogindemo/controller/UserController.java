package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.UserDto;
import com.example.registrationlogindemo.entity.*;
import com.example.registrationlogindemo.repository.*;
import com.example.registrationlogindemo.service.SolicitudeService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

/**
 * Controlador para la gestión de usuarios.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired UserService userService;
    @Autowired SolicitudeService solicitudeService;
    @Autowired UserRepository userRepository;
    @Autowired SolicitudeRepository solicitudeRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? userRepository.findByUsername(auth.getName()) : null;
    }

    @GetMapping("/welcome")
    public ModelAndView welcomePage() {
        ModelAndView mv = new ModelAndView("user/welcome");
        User usuario = getAuthenticatedUser();
        if (usuario != null) {
            mv.addObject("solicitude", solicitudeService.getSolicitudesByUser(usuario));
            mv.addObject("user", usuario);
            mv.addObject("username", usuario.getUsername());
        }
        return mv;
    }

    @GetMapping("/profile/{id}")
    public ModelAndView editUser(@PathVariable("id") long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser != null && currentUser.getId() != id)
            return new ModelAndView("redirect:/user/profile/" + currentUser.getId());

        User user = userRepository.findById(id).orElse(null);
        if (user != null && (user.getProfileImage() == null || user.getProfileImage().isEmpty()))
            user.setProfileImage("descargas.jpeg");

        return new ModelAndView("user/profile").addObject("user", user);
    }

    @PostMapping("/profile/{id}")
    public ModelAndView updateUser(@PathVariable("id") long id, @Valid User user, BindingResult result,
                                   @RequestParam("file") MultipartFile file, @RequestParam("currentProfileImageUrl") String currentImg,
                                   RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al editar.");
            return new ModelAndView("redirect:/user/profile/" + id);
        }

        User userEdit = userRepository.findById(id).orElse(null);
        if (userEdit != null) {
            userEdit.setUsername(user.getUsername());
            userEdit.setName(user.getName());
            userEdit.setEmail(user.getEmail());
            try {
                if (!file.isEmpty()) {
                    String filename = file.getOriginalFilename().replace(" ", "_");
                    Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());
                    userEdit.setProfileImage(filename);
                } else {
                    userEdit.setProfileImage(currentImg);
                }
            } catch (IOException e) { msg.addFlashAttribute("error", "Error con la imagen."); }
            userRepository.save(userEdit);
            msg.addFlashAttribute("success", "Usuario editado.");
        }
        return new ModelAndView("redirect:/user/profile/" + id);
    }

    @GetMapping("/delet/{id}")
    public String deleteUser(@PathVariable("id") long id) {
        userService.eliminarEntidad(id);
        return "redirect:/logout";
    }

    @GetMapping("/logout") public String logout() { return "index"; }

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "users";
    }

    @GetMapping({"/construction", "/contacto", "/informaciones", "/statistics", "/materiales"})
    public ModelAndView staticPages(HttpServletRequest req) {
        ModelAndView mv = new ModelAndView("user/" + req.getRequestURI().split("/")[2]);
        mv.addObject("user", getAuthenticatedUser());
        return mv;
    }

    @GetMapping("/view-requests")
    public ModelAndView viewRequests() {
        User user = getAuthenticatedUser();
        List<Solicitude> solicitudes = solicitudeRepository.findByUser(user);
        if (user != null && (user.getProfileImage() == null || user.getProfileImage().isEmpty()))
            user.setProfileImage("descargas.jpeg");
        return new ModelAndView("user/view-requests").addObject("solicitudes", solicitudes).addObject("user", user);
    }

    @PostMapping("/updatesolicitude/{id}")
    public ModelAndView updateSolicitude(@PathVariable("id") int id, @RequestParam String categoria,
                                         @RequestParam String barrio, @RequestParam String calle,
                                         @RequestParam String numeroDeCasa, @RequestParam MultipartFile file,
                                         @RequestParam String currentImageUrl, RedirectAttributes msg) {
        User user = getAuthenticatedUser();
        Optional<Solicitude> opt = solicitudeRepository.findById(id);
        if (opt.isPresent() && opt.get().getUser().getId() == user.getId()) {
            Solicitude s = opt.get();
            s.setCategoria(categoria); s.setBarrio(barrio); s.setCalle(calle); s.setNumeroDeCasa(numeroDeCasa);
            try {
                if (!file.isEmpty()) {
                    String filename = file.getOriginalFilename().replace(" ", "_");
                    Files.write(Paths.get(UPLOAD_DIR + filename), file.getBytes());
                    s.setImagen(filename);
                } else s.setImagen(currentImageUrl);
                solicitudeRepository.save(s);
                msg.addFlashAttribute("success", "Solicitud actualizada");
            } catch (IOException e) { msg.addFlashAttribute("error", "Error con imagen"); }
        }
        return new ModelAndView("redirect:/user/view-requests");
    }
}
