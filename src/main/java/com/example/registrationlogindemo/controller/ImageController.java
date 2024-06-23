package com.example.registrationlogindemo.controller;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

@RequestMapping("/root")
public class ImageController {
    private static final String UPLOAD_DIR = "src/main/resources/static/img/";
    @Autowired
    UserRepository userRepository;
    @Autowired
    ImageRepository imageRepository;
    @GetMapping("/newimage")
    public ModelAndView newimage() {
        return new ModelAndView("image/newimage");
    }


    @PostMapping("/newimage")
    public String uploadImage(@Valid Image image,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes msg) {
        if (result.hasErrors()) {
            msg.addFlashAttribute("error", "Error al cargar la imagen. Por favor, complete todos los campos correctamente.");
            return "redirect:/images/newimage";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String modifiedFilename = file.getOriginalFilename().replace(" ", "_"); // Replace spaces in the filename
                Path path = Paths.get(UPLOAD_DIR + modifiedFilename);
                Files.write(path, bytes);
                image.setImagen(modifiedFilename);
            } else {
                image.setImagen(null); // Optional: Set a default value if no image is uploaded
            }
        } catch (IOException e) {
            msg.addFlashAttribute("error", "Error al guardar la imagen. Inténtalo de nuevo más tarde.");
            return "redirect:/images/newimage";
        }

        image.setFecha(LocalDateTime.now()); // Set the current date
        // Here you should get the current user and assign it to the image
        User currentUser = userRepository.findByUsername(username);
        image.setUser(currentUser);

        // Save the image to the database or perform other necessary operations
        imageRepository.save(image);

        msg.addFlashAttribute("success", "Imagen cargada exitosamente.");
        return "redirect:/images/newimage";
    }
}
